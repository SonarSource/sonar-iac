package org.sonar.iac.arm.tree.impl.json;

import org.sonar.iac.arm.tree.api.NullLiteral;
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;

public class NullLiteralImpl extends ExpressionImpl implements NullLiteral {

  public NullLiteralImpl(YamlTreeMetadata metadata) {
    super(metadata);
  }

  @Override
  public Kind getKind() {
    return Kind.NULL_LITERAL;
  }
}
