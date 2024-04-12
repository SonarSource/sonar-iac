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
package org.sonar.iac.arm.tree.impl.json;

import java.util.Arrays;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.bicep.MemberExpression;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;

public class MemberExpressionImpl extends ExpressionImpl implements MemberExpression {
  @Nullable
  private final Expression expression;
  private final SyntaxToken separatingToken;
  private final Expression memberAccess;

  public MemberExpressionImpl(YamlTreeMetadata metadata, @Nullable Expression expression, SyntaxToken separatingToken,
    Expression memberAccess) {
    super(metadata);
    this.expression = expression;
    this.separatingToken = separatingToken;
    this.memberAccess = memberAccess;
  }

  @CheckForNull
  @Override
  public Expression expression() {
    return expression;
  }

  @Override
  public SyntaxToken separatingToken() {
    return separatingToken;
  }

  @Override
  public Expression memberAccess() {
    return memberAccess;
  }

  @Override
  public List<Tree> children() {
    return Arrays.asList(
      expression,
      separatingToken,
      memberAccess);
  }
}
