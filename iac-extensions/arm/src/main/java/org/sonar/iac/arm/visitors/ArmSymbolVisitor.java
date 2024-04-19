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
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.arm.symbols.SymbolTable;
import org.sonar.iac.arm.symbols.Usage;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.HasIdentifier;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.Parameter;
import org.sonar.iac.arm.tree.api.ParameterDeclaration;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.Variable;
import org.sonar.iac.arm.tree.api.VariableDeclaration;
import org.sonar.iac.arm.tree.api.bicep.Declaration;
import org.sonar.iac.arm.tree.impl.json.IdentifierImpl;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;

/**
 * Class used to visit a {@link ArmTree} and build {@link org.sonar.iac.arm.symbols.Symbol} and their usages for variables.
 * Those Symbol/Usage can later be used in checks to  report issues in the variable flow.
 */
public class ArmSymbolVisitor extends TreeVisitor<InputFileContext> {
  private static final Pattern ASSIGNED_IDENTITIES_PATTERN = Pattern.compile("(?U)variables\\('(?<variableName>[\\p{L}_]\\w*)'\\)");
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
    register(VariableDeclaration.class, (ctx, variableDeclaration) -> visitDeclaration(variableDeclaration));
    register(ParameterDeclaration.class, (ctx, parameterDeclaration) -> visitDeclaration(parameterDeclaration));
    registerAfter(Variable.class, (ctx, variable) -> visitAccessUsage(variable));
    registerAfter(Parameter.class, (ctx, parameter) -> visitAccessUsage(parameter));
    registerAfter(IdentifierImpl.class, (ctx, identifier) -> visitIdentifierJson(identifier));
  }

  @Override
  protected void after(InputFileContext ctx, @Nullable Tree node) {
    if (node != null) {
      ctx.enter(node);
      for (ConsumerFilter<InputFileContext, ?> consumer : consumersAfter) {
        consumer.accept(ctx, node);
      }
      node.children().forEach(child -> after(ctx, child));
      ctx.leave();
    }
  }

  public final <T extends Tree> void registerAfter(Class<T> cls, BiConsumer<InputFileContext, T> visitor) {
    consumersAfter.add(new ConsumerFilter<>(cls, visitor));
  }

  void visitFile(File file) {
    currentSymbolTable = new SymbolTable();
    file.setSymbolTable(currentSymbolTable);
  }

  private void visitDeclaration(Declaration declaration) {
    var symbol = currentSymbolTable.addSymbol(declaration.declaratedName().value().toLowerCase(Locale.ROOT));
    symbol.addUsage(declaration, Usage.Kind.ASSIGNMENT);
  }

  void visitAccessUsage(HasIdentifier tree) {
    if (tree.symbol() != null) {
      return;
    }

    Expression identifier = tree.identifier();
    if (identifier instanceof Identifier identifierTree) {
      addAccessUsageIfSymbolExists(tree, identifierTree.value());
    } else {
      currentSymbolTable.foundUnresolvableSymbolAccess(tree);
    }
  }

  private void addAccessUsageIfSymbolExists(ArmTree tree, String symbolName) {
    var symbol = currentSymbolTable.getSymbol(symbolName.toLowerCase(Locale.ROOT));
    if (symbol != null) {
      symbol.addUsage(tree, Usage.Kind.ACCESS);
    }
  }

  /**
   * We visit the identifier to find use cases where a variable is used inside a key of a {@link Property}.
   * One example would be in userAssignedIdentities where the key contains the following:
   * "userAssignedIdentities": {
   *   "[resourceID('Microsoft.ManagedIdentity/userAssignedIdentities/',variables('usedInsideUserAssignedIdentities'))]": {},
   * }
   */
  private void visitIdentifierJson(IdentifierImpl identifier) {
    String variableName = containsMentionOfVariable(identifier.value());
    if (variableName != null) {
      addAccessUsageIfSymbolExists(identifier, variableName);
    }
  }

  @CheckForNull
  private static String containsMentionOfVariable(String value) {
    var matcher = ASSIGNED_IDENTITIES_PATTERN.matcher(value);
    if (matcher.find()) {
      return matcher.group("variableName");
    }
    return null;
  }
}
