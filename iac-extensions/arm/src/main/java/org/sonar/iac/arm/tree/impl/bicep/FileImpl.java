package org.sonar.iac.arm.tree.impl.bicep;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.Statement;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class FileImpl extends AbstractArmTreeImpl implements File {

  private final List<Statement> statements;
  private final SyntaxToken eof;

  public FileImpl(List<Statement> statements, SyntaxToken eof) {
    this.statements = statements;
    this.eof = eof;
  }

  @Override
  public List<Tree> children() {
    ArrayList<Tree> trees = new ArrayList<>(statements);
    trees.add(eof);
    return trees;
  }

  @Override
  public Kind getKind() {
    return Kind.FILE;
  }

  @Override
  public Scope targetScope() {
    // TODO fix it in SONARIAC-932
    return Scope.RESOURCE_GROUP;
  }

  @CheckForNull
  @Override
  public StringLiteral targetScopeLiteral() {
    return null;
  }

  @Override
  public List<Statement> statements() {
    return statements;
  }
}
