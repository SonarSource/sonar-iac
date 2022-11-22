package org.sonar.iac.docker.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.LiteralListTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;

class EntrypointTreeImplTest {

  @Test
  void shouldParseEntrypointExecForm() {
    Assertions.assertThat(DockerLexicalGrammar.ENTRYPOINT)
      .matches("ENTRYPOINT")
      .matches("ENTRYPOINT []")
      .matches("ENTRYPOINT [\"ls\"]")
      .matches("ENTRYPOINT [\"executable\",\"param1\",\"param2\"]")
      .matches("ENTRYPOINT [\"/usr/bin/wc\",\"--help\"]")
      .matches("ENTRYPOINT [\"/usr/bin/wc\",\"--help\"]")
      .matches("    ENTRYPOINT []")
      .matches("ENTRYPOINT [\"c:\\\\Program Files\\\\foo.exe\"]")
      .matches("entrypoint")

      .notMatches("ENTRYPOINTT")
      // not exec form
      .notMatches("");
  }

  @Test
  void shouldParseEntrypointShellForm() {
    Assertions.assertThat(DockerLexicalGrammar.ENTRYPOINT)
      .matches("ENTRYPOINT")
      .matches("ENTRYPOINT ls")
      .matches("ENTRYPOINT \"ls\"")
      .matches("ENTRYPOINT command param1 param2")
      .matches("ENTRYPOINT echo \"This is a test.\" | wc -")
      .matches("ENTRYPOINT /bin/sh /deploy.sh")
      .matches("ENTRYPOINT mkdir -p /output && zip -FS -r /output/lambda.zip ./")
      .matches("ENTRYPOINT \"/usr/bin/run.sh\"")
      .matches("    ENTRYPOINT \"/usr/bin/run.sh\"")
      .matches("ENTRYPOINT     \"/usr/bin/run.sh\"")
      .matches("entrypoint")
      // not exec form
      .matches("ENTRYPOINT [\"la\", \"-bb\"")
      .matches("ENTRYPOINT [\"la\", \"-bb]")
      .matches("ENTRYPOINT \"la\", \"-bb\"]")

      .notMatches("/bin/sh /deploy.sh");
  }

  @Test
  void shouldCheckParseEntrypointExecFormTree() {
    EntrypointTreeImpl tree = DockerTestUtils.parse("ENTRYPOINT [\"executable\",\"param1\",\"param2\"]", DockerLexicalGrammar.ENTRYPOINT);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ENTRYPOINT);
    assertThat(tree.keyword().value()).isEqualTo("ENTRYPOINT");

    assertThat(tree.entrypointArguments()).isInstanceOf(ExecFormTreeImpl.class);
    assertThat(tree.entrypointArguments().literals().stream().map(TextTree::value))
      .containsExactly("\"executable\"", "\"param1\"", "\"param2\"");
    assertThat(tree.entrypointArguments().type()).isEqualTo(LiteralListTree.LiteralListType.EXEC);
    assertThat(((SyntaxToken)tree.children().get(0)).value()).isEqualTo("ENTRYPOINT");
    assertThat(((ExecFormTreeImpl)tree.children().get(1))).isSameAs(tree.entrypointArguments());
  }

  @Test
  void shouldCheckParseEntrypointShellFormTree() {
    EntrypointTreeImpl tree = DockerTestUtils.parse("ENTRYPOINT executable param1 param2", DockerLexicalGrammar.ENTRYPOINT);

    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ENTRYPOINT);
    assertThat(tree.keyword().value()).isEqualTo("ENTRYPOINT");

    assertThat(tree.entrypointArguments()).isInstanceOf(ShellFormTreeImpl.class);
    assertThat(tree.entrypointArguments().literals().stream().map(TextTree::value))
      .containsExactly("executable", "param1", "param2");
    assertThat(tree.entrypointArguments().type()).isEqualTo(LiteralListTree.LiteralListType.SHELL);
    assertThat(((SyntaxToken)tree.children().get(0)).value()).isEqualTo("ENTRYPOINT");
    assertThat((tree.children().get(1))).isSameAs(tree.entrypointArguments());
  }

  @Test
  void shouldCheckParseEmptyEntrypointExecFormTree() {
    EntrypointTreeImpl tree = DockerTestUtils.parse("ENTRYPOINT []", DockerLexicalGrammar.ENTRYPOINT);

    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ENTRYPOINT);
    assertThat(tree.keyword().value()).isEqualTo("ENTRYPOINT");

    assertThat(tree.entrypointArguments()).isInstanceOf(ExecFormTreeImpl.class);
    assertThat(tree.entrypointArguments().literals()).isEmpty();
    assertThat(tree.entrypointArguments().type()).isEqualTo(LiteralListTree.LiteralListType.EXEC);
    assertThat(((SyntaxToken)tree.children().get(0)).value()).isEqualTo("ENTRYPOINT");
    assertThat((tree.children().get(1))).isSameAs(tree.entrypointArguments());
  }

  @Test
  void shouldCheckParseEmptyEntrypointTree() {
    EntrypointTreeImpl tree = DockerTestUtils.parse("ENTRYPOINT", DockerLexicalGrammar.ENTRYPOINT);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ENTRYPOINT);
    assertThat(tree.keyword().value()).isEqualTo("ENTRYPOINT");

    assertThat(tree.entrypointArguments()).isNull();
    assertThat(((SyntaxToken)tree.children().get(0)).value()).isEqualTo("ENTRYPOINT");
    assertThat(tree.children()).hasSize(1);
  }
}
