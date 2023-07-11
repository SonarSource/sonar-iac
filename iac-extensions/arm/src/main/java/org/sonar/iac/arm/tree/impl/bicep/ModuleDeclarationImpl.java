package org.sonar.iac.arm.tree.impl.bicep;

import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.ModuleDeclaration;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

import java.util.List;

public class ModuleDeclarationImpl extends AbstractArmTreeImpl implements ModuleDeclaration {
  private final SyntaxToken keyword;
  private final Identifier name;
  private final InterpolatedString type;
  private final SyntaxToken equals;
  private final Expression value;

  public ModuleDeclarationImpl(SyntaxToken keyword, Identifier name, InterpolatedString type, SyntaxToken equals, Expression value) {
    this.keyword = keyword;
    this.name = name;
    this.type = type;
    this.equals = equals;
    this.value = value;
  }

  @Override
  public List<Tree> children() {
    return List.of(keyword, name, type, equals, value);
  }
}
