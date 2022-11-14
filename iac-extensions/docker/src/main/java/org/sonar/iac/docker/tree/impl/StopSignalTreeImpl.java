package org.sonar.iac.docker.tree.impl;

import java.util.List;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.tree.api.StopSignalTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class StopSignalTreeImpl extends DockerTreeImpl implements StopSignalTree {

  private final SyntaxToken stopSignalToken;
  private final SyntaxToken value;

  public StopSignalTreeImpl(SyntaxToken stopSignalToken, SyntaxToken tokenValue) {
    this.stopSignalToken = stopSignalToken;
    this.value = tokenValue;
  }

  @Override
  public List<Tree> children() {
    return List.of(stopSignalToken, value);
  }

  @Override
  public Kind getKind() {
    return Kind.STOPSIGNAL;
  }

  @Override
  public SyntaxToken token() {
    return stopSignalToken;
  }

  @Override
  public TextRange textRange() {
    return TextRanges.merge(List.of(stopSignalToken.textRange(), value.textRange()));
  }
}
