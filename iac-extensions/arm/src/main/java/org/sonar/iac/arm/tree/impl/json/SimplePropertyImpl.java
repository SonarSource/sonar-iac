package org.sonar.iac.arm.tree.impl.json;

import java.util.List;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.SimpleProperty;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;

public class SimplePropertyImpl implements SimpleProperty {

  private final Identifier key;
  private final Expression value;

  public SimplePropertyImpl(Identifier key, Expression value) {
    this.key = key;
    this.value = value;
  }

  @Override
  public Identifier key() {
    return key;
  }

  @Override
  public Expression value() {
    return value;
  }

  @Override
  public TextRange textRange() {
    return TextRanges.merge(List.of(key.textRange(), value.textRange()));
  }
}
