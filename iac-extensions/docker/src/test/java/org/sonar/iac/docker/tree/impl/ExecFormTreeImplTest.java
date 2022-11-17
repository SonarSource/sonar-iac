package org.sonar.iac.docker.tree.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.ExecFormLiteralTree;
import org.sonar.iac.docker.tree.api.ExecFormTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;

class ExecFormTreeImplTest {

  @Test
  void shouldParseExecForm() {
    Assertions.assertThat(DockerLexicalGrammar.EXEC_FORM)
      .matches("[]")
      .matches("[\"ls\"]")
      .matches("[\"executable\",\"param1\",\"param2\"]")
      .matches("[\"/usr/bin/wc\",\"--help\"]")
      .matches("    [\"/usr/bin/wc\",\"--help\"]")

      .notMatches("[abc]")
      .notMatches("[\"la\" \"-bb\"")
      .notMatches("[\"la\", \"-bb\"")
      .notMatches("[\"la\", \"-bb]")
      .notMatches("\"la\", \"-bb\"]");
  }

  @Test
  void shouldCheckExecFormTree() {
    ExecFormTree execForm = DockerTestUtils.parse("[\"executable\",\"param1\",\"param2\"]", DockerLexicalGrammar.EXEC_FORM);

    assertThat(execForm.getKind()).isEqualTo(DockerTree.Kind.EXEC_FORM);
    assertThat(execForm.leftBracket().value()).isEqualTo("[");
    assertThat(execForm.rightBracket().value()).isEqualTo("]");
    List<String> elementsAndSeparatorsAsText = execForm.literals().elementsAndSeparators().stream()
      .map(t -> {
        if (t instanceof SyntaxToken) {
          return ((SyntaxToken) t).value();
        } else if (t instanceof ExecFormLiteralTree) {
          return ((ExecFormLiteralTree) t).value().value();
        } else {
          throw new RuntimeException("Invalid cast from " + t.getClass());
        }
      })
      .collect(Collectors.toList());
    assertThat(elementsAndSeparatorsAsText).containsExactly("\"executable\"", "\"param1\"", "\"param2\"", ",", ",");

    List<ExecFormLiteralTree> elements = execForm.literals().elements();
    assertThat(elements.get(0).getKind()).isEqualTo(DockerTree.Kind.EXEC_FORM_LITERAL);
    assertThat(elements.stream().map(t -> t.value().value())).containsExactly("\"executable\"", "\"param1\"", "\"param2\"");

    assertThat(execForm.literals().separators().stream().map(SyntaxToken::value)).containsExactly(",", ",");
  }
}
