package org.sonar.iac.docker.tree.impl;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.docker.parser.grammar.DockerKeyword;
import org.sonar.iac.docker.parser.utils.DockerAssertions;
import org.sonar.iac.docker.tree.api.DockerTree;

class StopSignalTreeImplTest {

  @Test
  void test() {
    DockerAssertions.assertThat(DockerKeyword.STOPSIGNAL)
      .matches("STOPSIGNAL SIGKILL")
      .matches("STOPSIGNAL SIGTERM")
      .matches("STOPSIGNAL foo")
      .matches("STOPSIGNAL 9")
      .matches("STOPSIGNAL 1")
      .matches("STOPSIGNAL 1   ")
      .matches("   STOPSIGNAL 1")
      .notMatches("STOPSIGNALfooo")
      .notMatches("stopsignal")
      .notMatches("stopsignal 9")
      .notMatches("STOPSIGNALL");
  }

  @Test
  void test2() {
    DockerTree tree = DockerTestUtils.parse("STOPSIGNAL SIGKILL", DockerKeyword.STOPSIGNAL);
    Assertions.assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.STOPSIGNAL);
    Assertions.assertThat(tree.children().get(0)).isEqualTo("STOPSIGNAL");
    Assertions.assertThat(tree.children().get(1)).isEqualTo("SIGKILL");
  }
}
