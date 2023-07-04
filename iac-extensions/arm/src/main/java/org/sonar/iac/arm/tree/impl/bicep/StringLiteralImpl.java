package org.sonar.iac.arm.tree.impl.bicep;

import java.util.List;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class StringLiteralImpl extends AbstractArmTreeImpl implements StringLiteral {

  private final SyntaxToken token;

  public StringLiteralImpl(SyntaxToken token) {
    this.token = token;
  }

  @Override
  public String value() {
    return token.value();
  }

  @Override
  public List<Tree> children() {
    return List.of(token);
  }

  @Override
  public Kind getKind() {
    return Kind.STRING_LITERAL;
  }
}
