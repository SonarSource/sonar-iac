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
import org.sonar.iac.docker.symbols.Scope;
import org.sonar.iac.docker.symbols.Symbol;
import org.sonar.iac.docker.symbols.Usage;
import org.sonar.iac.docker.tree.api.ArgInstruction;
import org.sonar.iac.docker.tree.api.Body;
import org.sonar.iac.docker.tree.api.DockerImage;
import org.sonar.iac.docker.tree.api.EnvInstruction;
import org.sonar.iac.docker.tree.api.FromInstruction;
import org.sonar.iac.docker.tree.api.KeyValuePair;
import org.sonar.iac.docker.tree.api.Variable;
import org.sonar.iac.docker.utils.ArgumentUtils;

import static org.sonar.iac.docker.utils.ArgumentUtils.ArgumentResolution.Status.RESOLVED;

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
    register(Variable.class, this::visitVariable);
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

  public void visitAssignmentInstruction(List<KeyValuePair> assignments) {
    for (KeyValuePair keyValuePair : assignments) {
      ArgumentUtils.ArgumentResolution identifier = ArgumentUtils.resolve(keyValuePair.key());
      if (identifier.is(RESOLVED) && !identifier.value().isBlank()) {
        Symbol symbol = currentScope.addSymbol(identifier.value());
        symbol.addUsage(currentScope, keyValuePair, Usage.Kind.ASSIGNMENT);
      }
    }
  }

  private void visitVariable(InputFileContext ctx, Variable variable) {
    Symbol symbol = currentScope.getSymbol(variable.identifier());
    if (symbol != null) {
      symbol.addUsage(currentScope, variable, Usage.Kind.ACCESS);
    }
  }
}
