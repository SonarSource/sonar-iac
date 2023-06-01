package org.sonar.iac.arm.tree.impl.json;

import org.sonar.iac.arm.tree.api.NumericLiteral;
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;

public class NumericLiteralImpl extends ExpressionImpl implements NumericLiteral {

  private final Double value;

  public NumericLiteralImpl(Double value, YamlTreeMetadata metadata) {
    super(metadata);
    this.value = value;
  }

  @Override
  public double value() {
    return value;
  }

  @Override
  public Kind getKind() {
    return Kind.NUMERIC_LITERAL;
  }
}
