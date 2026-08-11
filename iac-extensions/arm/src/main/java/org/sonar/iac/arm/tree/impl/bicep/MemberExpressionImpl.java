/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.bicep.MemberExpression;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

import static org.sonar.iac.arm.tree.ArmHelper.addChildrenIfPresent;

public class MemberExpressionImpl extends AbstractArmTreeImpl implements MemberExpression {

  private final SyntaxToken separatingToken;

  @Nullable
  private final SyntaxToken safeDereference;
  @Nullable
  private final SyntaxToken reverseIndexAccessorToken;
  @Nullable
  private final Expression expression;
  @Nullable
  private final SyntaxToken closingBracket;

  // Set right after construction through complete(), which the parser always calls.
  @Nullable
  private Expression memberAccess;

  public MemberExpressionImpl(SyntaxToken separatingToken, @Nullable SyntaxToken safeDereference, @Nullable SyntaxToken reverseIndexAccessorToken, @Nullable Expression expression,
    @Nullable SyntaxToken closingBracket) {
    this.separatingToken = separatingToken;
    this.safeDereference = safeDereference;
    this.reverseIndexAccessorToken = reverseIndexAccessorToken;
    this.expression = expression;
    this.closingBracket = closingBracket;
  }

  public MemberExpression complete(Expression memberAccess) {
    this.memberAccess = memberAccess;
    return this;
  }

  @Override
  public List<Tree> children() {
    List<Tree> result = new ArrayList<>();
    result.add(memberAccess());
    result.add(separatingToken);
    addChildrenIfPresent(result, safeDereference);
    addChildrenIfPresent(result, reverseIndexAccessorToken);
    addChildrenIfPresent(result, expression);
    addChildrenIfPresent(result, closingBracket);
    return result;
  }

  @Nullable
  @Override
  public Expression expression() {
    return expression;
  }

  @Override
  public SyntaxToken separatingToken() {
    return separatingToken;
  }

  /**
   * The member access is assigned through {@link #complete(Expression)} by the parser, so it is always set by the time
   * this is called, honouring the non-null {@link MemberExpression#memberAccess()} contract.
   */
  @Override
  public Expression memberAccess() {
    return Objects.requireNonNull(memberAccess, "Member access is not set yet");
  }

  @Override
  public String toString() {
    return memberAccess().toString()
      + separatingToken
      + (safeDereference != null ? safeDereference : "")
      + (reverseIndexAccessorToken != null ? reverseIndexAccessorToken : "")
      + (expression != null ? expression : "")
      + (closingBracket != null ? closingBracket : "");
  }
}
