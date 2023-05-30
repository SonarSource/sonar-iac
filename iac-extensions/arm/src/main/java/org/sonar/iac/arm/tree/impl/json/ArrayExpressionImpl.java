package org.sonar.iac.arm.tree.impl.json;

import java.util.Collections;
import java.util.List;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.PropertyValue;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class ArrayExpressionImpl extends AbstractArmTreeImpl implements ArrayExpression {
  private final List<PropertyValue> values;

  public ArrayExpressionImpl(List<PropertyValue> values) {
    this.values = values;
  }

  @Override
  public List<Tree> children() {
    return Collections.unmodifiableList(values);
  }

  @Override
  public Kind getKind() {
    return Kind.ARRAY_EXPRESSION;
  }

  @Override
  public List<PropertyValue> values() {
    return values;
  }
}
