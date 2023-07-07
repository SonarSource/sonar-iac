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

import java.util.List;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.bicep.MetadataDeclaration;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class MetadataDeclarationImpl extends AbstractArmTreeImpl implements MetadataDeclaration {

  private final SyntaxToken keyword;
  private final Identifier identifier;
  private final SyntaxToken equals;
  private final Expression expression;
  private final SyntaxToken newLine;

  public MetadataDeclarationImpl(SyntaxToken keyword, Identifier identifier, SyntaxToken equals, Expression expression, SyntaxToken newLine) {
    this.keyword = keyword;
    this.identifier = identifier;
    this.equals = equals;
    this.expression = expression;
    this.newLine = newLine;
  }

  @Override
  public List<Tree> children() {
    return List.of(keyword, identifier, equals, expression, newLine);
  }

  @Override
  public Kind getKind() {
    return Kind.METADATA_DECLARATION;
  }

  @Override
  public Identifier name() {
    return identifier;
  }

  @Override
  public Expression value() {
    return expression;
  }
}
