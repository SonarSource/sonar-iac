package org.sonar.iac.arm.tree.impl.bicep;

import java.util.List;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.bicep.LambdaExpression;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class LambdaExpressionImpl extends AbstractArmTreeImpl implements LambdaExpression {
  private final ArmTree arguments;
  private final SyntaxToken doubleArrow;
  private final Expression body;

  public LambdaExpressionImpl(ArmTree arguments, SyntaxToken doubleArrow, Expression body) {
    this.arguments = arguments;
    this.doubleArrow = doubleArrow;
    this.body = body;
  }

  @Override
  public List<Tree> children() {
    return List.of(arguments, doubleArrow, body);
  }
}
