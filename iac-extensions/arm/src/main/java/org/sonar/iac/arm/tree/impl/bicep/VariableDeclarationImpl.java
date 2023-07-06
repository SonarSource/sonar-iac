package org.sonar.iac.arm.tree.impl.bicep;

import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.VariableDeclaration;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

import java.util.List;

public class VariableDeclarationImpl extends AbstractArmTreeImpl implements VariableDeclaration {
  private final SyntaxToken variableKeyword;
  private final Identifier identifier;
  private final SyntaxToken equals;
  private final Expression expression;

  public VariableDeclarationImpl(SyntaxToken variableKeyword, Identifier identifier, SyntaxToken equals, Expression expression) {
    this.variableKeyword = variableKeyword;
    this.identifier = identifier;
    this.equals = equals;
    this.expression = expression;
  }

  @Override
  public List<Tree> children() {
    return List.of(variableKeyword, identifier, equals, expression);
  }

  @Override
  public Kind getKind() {
    return Kind.VARIABLE_DECLARATION;
  }

  @Override
  public Identifier name() {
    return this.identifier;
  }

  @Override
  public Expression value() {
    return this.expression;
  }
}
