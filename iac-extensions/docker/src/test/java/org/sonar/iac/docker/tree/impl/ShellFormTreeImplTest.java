package org.sonar.iac.docker.tree.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.ShellFormTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;

class ShellFormTreeImplTest {

  @Test
  void shouldParseExecForm() {
    Assertions.assertThat(DockerLexicalGrammar.SHELL_FORM)
      .matches("ls")
      .matches("executable param1 param2")
      .matches("executable \"param1\" param2")
      .matches("ls    -a")
      .matches("   ls -a")
      .matches("git commit -m \"first commit\"")

      .notMatches("");
  }

  @Test
  void shouldCheckExecFormTree() {
    ShellFormTree execForm = DockerTestUtils.parse("executable param1 param2", DockerLexicalGrammar.SHELL_FORM);

    assertThat(execForm.getKind()).isEqualTo(DockerTree.Kind.SHELL_FORM);
    List<String> elementsAndSeparatorsAsText = execForm.literals().stream()
      .map(TextTree::value)
      .collect(Collectors.toList());
    assertThat(elementsAndSeparatorsAsText).containsExactly("executable", "param1", "param2");

    List<SyntaxToken> elements = execForm.literals();
    assertThat(elements.get(0).getKind()).isEqualTo(DockerTree.Kind.TOKEN);
  }

  @Test
  void shouldCheckExecFormWithQuotesTree() {
    ShellFormTree execForm = DockerTestUtils.parse("git commit -m \"Some commit message\"", DockerLexicalGrammar.SHELL_FORM);

    assertThat(execForm.getKind()).isEqualTo(DockerTree.Kind.SHELL_FORM);
    List<String> elementsAndSeparatorsAsText = execForm.literals().stream()
      .map(TextTree::value)
      .collect(Collectors.toList());
    assertThat(elementsAndSeparatorsAsText).containsExactly("git", "commit", "-m", "\"Some commit message\"");

    List<SyntaxToken> elements = execForm.literals();
    assertThat(elements.get(0).getKind()).isEqualTo(DockerTree.Kind.TOKEN);
  }
}
