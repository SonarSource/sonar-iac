package org.sonar.iac.arm.tree.api.bicep.interpstring;

import org.sonar.iac.common.api.tree.Tree;

import java.util.List;

public interface InterpolatedStringLeftPiece {
  List<Tree> children();
}
