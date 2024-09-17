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
package org.sonar.iac.arm.tree.impl.bicep;

import java.util.ArrayList;
import java.util.List;
import org.sonar.iac.arm.tree.api.bicep.CompileTimeImportDeclaration;
import org.sonar.iac.arm.tree.api.bicep.Decorator;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.importdecl.CompileTimeImportFromClause;
import org.sonar.iac.arm.tree.api.bicep.importdecl.CompileTimeImportTarget;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class CompileTimeImportDeclarationImpl extends AbstractArmTreeImpl implements CompileTimeImportDeclaration {
  private final List<Decorator> decorators;
  private final SyntaxToken keyword;
  private final CompileTimeImportTarget target;
  private final CompileTimeImportFromClause fromClause;

  public CompileTimeImportDeclarationImpl(
    List<Decorator> decorators,
    SyntaxToken keyword,
    CompileTimeImportTarget target,
    CompileTimeImportFromClause fromClause) {
    this.decorators = decorators;
    this.keyword = keyword;
    this.target = target;
    this.fromClause = fromClause;
  }

  @Override
  public List<Decorator> decorators() {
    return decorators;
  }

  @Override
  public SyntaxToken keyword() {
    return keyword;
  }

  @Override
  public List<Tree> children() {
    var result = new ArrayList<Tree>(decorators);
    result.add(keyword);
    result.add(target);
    result.add(fromClause);
    return result;
  }
}
