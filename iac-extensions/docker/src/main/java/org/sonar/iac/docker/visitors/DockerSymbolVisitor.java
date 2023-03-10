/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
package org.sonar.iac.docker.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.symbols.Scope;
import org.sonar.iac.docker.symbols.Symbol;
import org.sonar.iac.docker.symbols.Usage;
import org.sonar.iac.docker.tree.api.ArgInstruction;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.Body;
import org.sonar.iac.docker.tree.api.DockerImage;
import org.sonar.iac.docker.tree.api.EnvInstruction;
import org.sonar.iac.docker.tree.api.FromInstruction;
import org.sonar.iac.docker.tree.api.KeyValuePair;
import org.sonar.iac.docker.tree.api.Variable;

import static org.sonar.iac.docker.symbols.ArgumentResolution.Status.RESOLVED;

/**
 * The purpose of this class is to visit a DockerTree and build symbols and their usages for variables.
 * Those Symbol/Usage can later be used in checks to resolve Argument or to report issue variable flow.
 */
public class DockerSymbolVisitor extends TreeVisitor<InputFileContext> {

  private final List<ConsumerFilter<InputFileContext, ?>> consumersAfter = new ArrayList<>();
  private final Scope globalScope = new Scope(Scope.Kind.GLOBAL);
  private Scope currentScope = globalScope;

  public DockerSymbolVisitor() {
    register(Body.class, this::setGlobalScope);
    register(FromInstruction.class, this::restoreGlobalScope);
    registerAfter(FromInstruction.class, this::setImageScope);
    register(ArgInstruction.class, (ctx, argInstruction) -> visitAssignmentInstruction(argInstruction.keyValuePairs()));
    register(EnvInstruction.class, (ctx, envInstruction) -> visitAssignmentInstruction(envInstruction.environmentVariables()));
    register(Variable.class, (ctx, variable) -> visitVariable(variable));
  }

  @Override
  protected void visit(InputFileContext ctx, @Nullable Tree node) {
    if (node != null) {
      ctx.enter(node);
      callConsumers(ctx, node, consumers);
      node.children().forEach(child -> visit(ctx, child));
      callConsumers(ctx, node, consumersAfter);
      ctx.leave();
    }
  }

  private static void callConsumers(InputFileContext ctx, Tree node, List<ConsumerFilter<InputFileContext, ?>> consumerList) {
    for (ConsumerFilter<InputFileContext, ?> consumer : consumerList) {
      consumer.accept(ctx, node);
    }
  }

  public <T extends Tree> TreeVisitor<InputFileContext> registerAfter(Class<T> cls, BiConsumer<InputFileContext, T> visitor) {
    consumersAfter.add(new ConsumerFilter<>(cls, visitor));
    return this;
  }

  public void setGlobalScope(InputFileContext ctx, Body body) {
    body.setScope(globalScope);
  }

  public void restoreGlobalScope(InputFileContext ctx, FromInstruction from) {
    currentScope = globalScope;
  }

  public void setImageScope(InputFileContext ctx, FromInstruction from) {
    DockerImage dockerImage = (DockerImage) from.parent();
    currentScope = new Scope(Scope.Kind.IMAGE, globalScope);
    dockerImage.setScope(currentScope);
  }

  private void visitAssignmentInstruction(List<KeyValuePair> assignments) {
    for (KeyValuePair keyValuePair : assignments) {
      Argument identifier = keyValuePair.key();
      visitPossibleVariablesInIdentifier(identifier);
      ArgumentResolution resolution = ArgumentResolution.of(identifier);
      if (resolution.is(RESOLVED) && !resolution.value().isBlank()) {
        Symbol symbol = currentScope.addSymbol(resolution.value());
        symbol.addUsage(currentScope, keyValuePair, Usage.Kind.ASSIGNMENT);
      }
    }
  }

  private void visitPossibleVariablesInIdentifier(Argument identifier) {
    identifier.expressions().stream()
      .filter(Variable.class::isInstance)
      .map(Variable.class::cast)
      .forEach(this::visitVariable);
  }

  private void visitVariable(Variable variable) {
    if (variable.symbol() != null) {
      // Variable is already visited
      return;
    }

    Symbol symbol = currentScope.getSymbol(variable.identifier());
    if (symbol != null) {
      symbol.addUsage(currentScope, variable, Usage.Kind.ACCESS);
    }
  }
}
