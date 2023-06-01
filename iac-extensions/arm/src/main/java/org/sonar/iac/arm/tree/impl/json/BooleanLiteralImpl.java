package org.sonar.iac.arm.tree.impl.json;

import org.sonar.iac.arm.tree.api.BooleanLiteral;
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;

public class BooleanLiteralImpl extends ExpressionImpl implements BooleanLiteral {

  private final boolean value;

  public BooleanLiteralImpl(boolean value, YamlTreeMetadata metadata) {
    super(metadata);
    this.value = value;
  }

  @Override
  public boolean value() {
    return value;
  }

  @Override
  public Kind getKind() {
    return Kind.BOOLEAN_LITERAL;
  }
}
