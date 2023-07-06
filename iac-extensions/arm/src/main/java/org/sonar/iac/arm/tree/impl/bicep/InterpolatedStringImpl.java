package org.sonar.iac.arm.tree.impl.bicep;

import java.util.List;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class InterpolatedStringImpl extends AbstractArmTreeImpl implements InterpolatedString {
  private final SyntaxToken openApostrophe;
  private final SyntaxToken value;
  private final SyntaxToken closeApostrophe;

  public InterpolatedStringImpl(SyntaxToken openApostrophe, SyntaxToken value, SyntaxToken closeApostrophe) {
    this.openApostrophe = openApostrophe;
    this.value = value;
    this.closeApostrophe = closeApostrophe;
  }

  @Override
  public List<Tree> children() {
    return List.of(openApostrophe, value, closeApostrophe);
  }

  @Override
  public Kind getKind() {
    return Kind.INTERPOLATED_STRING;
  }

  @Override
  public String value() {
    return value.value();
  }
}
