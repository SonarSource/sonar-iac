package org.sonar.iac.arm.tree.impl.json;

import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;

public class StringLiteralImpl extends ExpressionImpl implements StringLiteral {

  private final String value;

  public StringLiteralImpl(String value, YamlTreeMetadata metadata) {
    super(metadata);
    this.value = value;
  }

  @Override
  public String value() {
    return value;
  }

  @Override
  public Kind getKind() {
    return Kind.STRING_LITERAL;
  }
}
