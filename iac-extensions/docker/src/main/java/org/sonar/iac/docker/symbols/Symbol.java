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
package org.sonar.iac.docker.symbols;

import java.util.ArrayList;
import java.util.List;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.HasSymbol;

public class Symbol {

  private final String name;

  private final List<Usage> usages;

  public Symbol(String name) {
    this.name = name;
    this.usages = new ArrayList<>();
  }

  public Symbol(Symbol orgSymbol) {
    this.name = orgSymbol.name;
    this.usages = new ArrayList<>(orgSymbol.usages);
  }

  public void addUsage(Scope scope, DockerTree tree, Usage.Kind kind) {
    var usage = new Usage(scope, tree, kind);
    usages.add(usage);
    if (tree instanceof HasSymbol treeWithSymbol) {
      // Each symbol usage only references previous usages
      // This is done to easily to compute findLastAccessibleAssignedValue in ArgumentResolution
      treeWithSymbol.setSymbol(new Symbol(this));
    }
  }

  public String name() {
    return name;
  }

  public List<Usage> usages() {
    return usages;
  }

  public Scope.Kind lastDeclarationScope() {
    Scope.Kind declarationScope = Scope.Kind.IMAGE;
    for (int i = usages.size() - 1; i >= 0; i--) {
      var usage = usages.get(i);
      if (Usage.Kind.ASSIGNMENT == usage.kind()) {
        declarationScope = usage.scope().kind();
        // we stop at the first assignment we find, as we store usages in the same order they appear in Dockerfile
        break;
      }
    }
    return declarationScope;
  }
}
