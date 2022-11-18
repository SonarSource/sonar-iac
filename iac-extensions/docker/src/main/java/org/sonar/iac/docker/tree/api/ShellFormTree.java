package org.sonar.iac.docker.tree.api;

import java.util.List;

public interface ShellFormTree extends DockerTree {
  List<SyntaxToken> literals();
}
