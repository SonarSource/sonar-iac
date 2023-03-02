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

import java.util.List;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.docker.symbols.Scope;
import org.sonar.iac.docker.symbols.Symbol;
import org.sonar.iac.docker.symbols.Usage;
import org.sonar.iac.docker.tree.api.ArgInstruction;
import org.sonar.iac.docker.tree.api.Body;
import org.sonar.iac.docker.tree.api.DockerImage;
import org.sonar.iac.docker.tree.api.EnvInstruction;
import org.sonar.iac.docker.tree.api.KeyValuePair;
import org.sonar.iac.docker.tree.api.Variable;
import org.sonar.iac.docker.utils.ArgumentUtils;

public class DockerSymbolVisitor extends TreeVisitor<InputFileContext> {

  private final Scope globalScope = new Scope(Scope.Kind.GLOBAL);
  private Scope currentScope = globalScope;

  public DockerSymbolVisitor() {
    register(Body.class, this::setGlobalScope);
    register(DockerImage.class, this::setImageScope);
    register(ArgInstruction.class, this::visitArgInstruction);
    register(EnvInstruction.class, this::visitEnvInstruction);
    register(Variable.class, this::visitVariable);
  }

  public void setGlobalScope(InputFileContext ctx, Body body) {
    body.setScope(globalScope);
  }

  public void setImageScope(InputFileContext ctx, DockerImage dockerImage) {
    currentScope = new Scope(Scope.Kind.IMAGE, globalScope);
    dockerImage.setScope(currentScope);
  }

  public void visitArgInstruction(InputFileContext ctx, ArgInstruction argInstruction) {
    analyzeSymbolDeclarations(argInstruction.keyValuePairs());
  }

  public void visitEnvInstruction(InputFileContext ctx, EnvInstruction envInstruction) {
    analyzeSymbolDeclarations(envInstruction.environmentVariables());
  }

  public void analyzeSymbolDeclarations(List<KeyValuePair> declarations) {
    for (KeyValuePair declaration : declarations) {
      String identifier = ArgumentUtils.resolve(declaration.key()).value();
      if (identifier != null) {
        Symbol symbol = currentScope.addSymbol(identifier);
        symbol.addUsage(currentScope, declaration, Usage.Kind.ASSIGNMENT);
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
