package org.sonar.iac.docker.tree.api;

public interface StopSignalTree extends InstructionTree {
  SyntaxToken token();
}
