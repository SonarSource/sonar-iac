package org.sonar.iac.docker.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.DockerAssertions;

class StringLiteralImplTest {

  @Test
  void test() {
    DockerAssertions.assertThat(DockerLexicalGrammar.STRING_LITERAL)
      .matches("f")
      .matches("foo")
      .matches("   foo")
      .matches("1")
      .matches("123")
      .matches("SIGKILL");
  }
}
