/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.arm.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import org.sonar.iac.arm.symbols.SymbolTable;
import org.sonar.iac.arm.symbols.Usage;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.ParameterDeclaration;
import org.sonar.iac.arm.tree.api.Variable;
import org.sonar.iac.arm.tree.api.VariableDeclaration;
import org.sonar.iac.arm.tree.api.bicep.Declaration;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;

/**
 * Class used to visit a {@link ArmTree} and build {@link org.sonar.iac.arm.symbols.Symbol} and their usages for variables.
 * Those Symbol/Usage can later be used in checks to  report issues in the variable flow.
 */
public class ArmSymbolVisitor extends TreeVisitor<InputFileContext> {
  private final List<ConsumerFilter<InputFileContext, ?>> consumersAfter = new ArrayList<>();
  private SymbolTable currentSymbolTable = new SymbolTable();

  /**
   * Registers the different types of nodes that are visited.
   * The Visitor will take two passes through the Tree.
   * The first pass contains all nodes with registered in the "register" method,
   * the second pass all nodes registered with the "registerAfter" method.
   * This is done because the declaration of a variable doesn't always occur before the access in the Tree.
   */
  public ArmSymbolVisitor() {
    register(File.class, (ctx, file) -> visitFile(file));
    register(VariableDeclaration.class, (ctx, variableDeclaration) -> visitVariableDeclaration(variableDeclaration));
    register(ParameterDeclaration.class, (ctx, parameterDeclaration) -> visitVariableDeclaration(parameterDeclaration));
    registerAfter(Variable.class, (ctx, variable) -> visitVariable(variable));
  }

  @Override
  protected void after(InputFileContext ctx, @Nullable Tree node) {
    if (node != null) {
      ctx.enter(node);
      for (ConsumerFilter<InputFileContext, ?> consumer : consumersAfter) {
        consumer.accept(ctx, node);
      }
      node.children().forEach(child -> after(ctx, child));
    }
  }

  public final <T extends Tree> void registerAfter(Class<T> cls, BiConsumer<InputFileContext, T> visitor) {
    consumersAfter.add(new ConsumerFilter<>(cls, visitor));
  }

  void visitFile(File file) {
    currentSymbolTable = new SymbolTable();
    file.setSymbolTable(currentSymbolTable);
  }


  private void visitVariableDeclaration(Declaration declaration) {
    var symbol = currentSymbolTable.addSymbol(declaration.declaratedName().value());
    symbol.addUsage(declaration, Usage.Kind.ASSIGNMENT);
  }

  void visitVariable(Variable variable) {
    if (variable.symbol() != null) {
      return;
    }

    Expression identifier = variable.identifier();
    if (identifier instanceof Identifier identifierTree) {
      var symbol = currentSymbolTable.getSymbol(identifierTree.value());
      if (symbol != null) {
        symbol.addUsage(variable, Usage.Kind.ACCESS);
      }
    } else {
      currentSymbolTable.foundUnresolvableVariableAccess(variable);
    }
  }
}
