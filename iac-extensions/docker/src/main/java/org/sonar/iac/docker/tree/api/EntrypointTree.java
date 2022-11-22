package org.sonar.iac.docker.tree.api;

import javax.annotation.CheckForNull;

public interface EntrypointTree extends InstructionTree {

  @CheckForNull
  LiteralListTree entrypointArguments();
}
