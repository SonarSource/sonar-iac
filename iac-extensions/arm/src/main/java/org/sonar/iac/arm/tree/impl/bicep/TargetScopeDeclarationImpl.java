package org.sonar.iac.arm.tree.impl.bicep;

import java.util.List;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.TargetScopeDeclaration;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class TargetScopeDeclarationImpl extends AbstractArmTreeImpl implements TargetScopeDeclaration {

  private final SyntaxToken targetScope;
  private final SyntaxToken equals;
  private final Expression expression;

  public TargetScopeDeclarationImpl(SyntaxToken targetScope, SyntaxToken equals, Expression expression) {
    this.targetScope = targetScope;
    this.equals = equals;
    this.expression = expression;
  }

  @Override
  public List<Tree> children() {
    return List.of(targetScope, equals, expression);
  }

  @Override
  public Kind getKind() {
    return Kind.TARGET_SCOPE_DECLARATION;
  }

  @Override
  public File.Scope scope() {
    if (expression.is(Kind.STRING_LITERAL)) {
      StringLiteral stringLiteral = (StringLiteral) expression;
      switch (stringLiteral.value()) {
        case "managementGroup":
          return File.Scope.MANAGEMENT_GROUP;
        case "resourceGroup":
          return File.Scope.RESOURCE_GROUP;
        case "subscription":
          return File.Scope.SUBSCRIPTION;
        case "tenant":
          return File.Scope.TENANT;
        default:
          return File.Scope.UNKNOWN;
      }
    } else {
      return File.Scope.UNKNOWN;
    }
  }
}
