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
package org.sonar.iac.arm.tree.impl.bicep;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.Statement;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class FileImpl extends AbstractArmTreeImpl implements File {

  private final List<Statement> statements;
  private final SyntaxToken eof;

  public FileImpl(List<Statement> statements, SyntaxToken eof) {
    this.statements = statements;
    this.eof = eof;
  }

  @Override
  public List<Tree> children() {
    ArrayList<Tree> trees = new ArrayList<>(statements);
    trees.add(eof);
    return trees;
  }

  @Override
  public Kind getKind() {
    return Kind.FILE;
  }

  @Override
  public Scope targetScope() {
    // TODO fix it in SONARIAC-932
    return Scope.RESOURCE_GROUP;
  }

  @CheckForNull
  @Override
  public Expression targetScopeLiteral() {
    // TODO fix it in SONARIAC-932
    return null;
  }

  @Override
  public List<Statement> statements() {
    return statements;
  }
}
