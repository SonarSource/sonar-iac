/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.arm.tree.impl.bicep;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.bicep.MemberExpression;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

import static org.sonar.iac.arm.tree.ArmHelper.addChildrenIfPresent;

public class MemberExpressionImpl extends AbstractArmTreeImpl implements MemberExpression {

  private final SyntaxToken separatingToken;
  @CheckForNull
  private final Expression expression;
  @CheckForNull
  private final SyntaxToken safeDereference;
  @CheckForNull
  private final SyntaxToken closingBracket;

  private Expression memberAccess;

  public MemberExpressionImpl(SyntaxToken separatingToken, @Nullable Expression expression, @Nullable SyntaxToken safeDereference, @Nullable SyntaxToken closingBracket) {
    this.separatingToken = separatingToken;
    this.expression = expression;
    this.safeDereference = safeDereference;
    this.closingBracket = closingBracket;
  }

  public MemberExpression complete(Expression memberAccess) {
    this.memberAccess = memberAccess;
    return this;
  }

  @Override
  public List<Tree> children() {
    List<Tree> result = new ArrayList<>();
    result.add(memberAccess);
    result.add(separatingToken);
    addChildrenIfPresent(result, safeDereference);
    addChildrenIfPresent(result, expression);
    addChildrenIfPresent(result, closingBracket);
    return result;
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
  public String toString() {
    return memberAccess.toString()
      + separatingToken
      + (safeDereference != null ? safeDereference : "")
      + (expression != null ? expression : "")
      + (closingBracket != null ? closingBracket : "");
  }
}
