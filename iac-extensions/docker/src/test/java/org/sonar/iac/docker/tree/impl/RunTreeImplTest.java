/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.docker.tree.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;
import org.sonar.iac.docker.tree.api.ParamTree;
import org.sonar.iac.docker.tree.api.RunTree;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.ExecFormLiteralTree;
import org.sonar.iac.docker.tree.api.ExecFormTree;
import org.sonar.iac.docker.tree.api.LiteralListTree;
import org.sonar.iac.docker.tree.api.SeparatedList;
import org.sonar.iac.docker.tree.api.ShellFormTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;

class RunTreeImplTest {

  @Test
  void shouldParseRunExecForm() {
    Assertions.assertThat(DockerLexicalGrammar.RUN)
      .matches("RUN")
      .matches("RUN []")
      .matches("RUN [\"ls\"]")
      .matches("RUN [\"executable\",\"param1\",\"param2\"]")
      .matches("RUN [\"/usr/bin/wc\",\"--help\"]")
      .matches("RUN [\"/usr/bin/wc\",\"--help\"]")
      .matches("    RUN []")
      .matches("RUN [\"c:\\\\Program Files\\\\foo.exe\"]")
      .matches("run")

      .notMatches("RUND")
      // not exec form
      .notMatches("");
  }

  @Test
  void shouldParseRunShellForm() {
    Assertions.assertThat(DockerLexicalGrammar.RUN)
      .matches("RUN")
      .matches("RUN ls")
      .matches("RUN \"ls\"")
      .matches("RUN command param1 param2")
      .matches("RUN echo \"This is a test.\" | wc -")
      .matches("RUN /bin/sh /deploy.sh")
      .matches("RUN mkdir -p /output && zip -FS -r /output/lambda.zip ./")
      .matches("RUN CGO_ENABLED=0 go build -o backend main.go")
      // TODO to implement in scope of SONARIAC-472
//      .matches("RUN <<EOF\n" +
//        "apk update\n" +
//        "apk add git\n" +
//        "EOF")
      .matches("RUN export FLASK_APP=app.py")
      .matches("RUN export FLASK_APP=app.py")
      .matches("RUN \"/usr/bin/run.sh\"")
      .matches("    RUN \"/usr/bin/run.sh\"")
      .matches("RUN     \"/usr/bin/run.sh\"")
      .matches("run")
      // not exec form
      .matches("RUN [\"la\", \"-bb\"")
      .matches("RUN [\"la\", \"-bb]")
      .matches("RUN \"la\", \"-bb\"]")

      .notMatches("/bin/sh /deploy.sh");
  }

  @Test
  void shouldParseRunOptionsExecForm() {
    Assertions.assertThat(DockerLexicalGrammar.RUN)
      .matches("RUN --mount=type=bind")
      // Docker throws: "failed to marshal LLB definition: arguments are required"
      .matches("RUN []")
      .matches("RUN --mount=type=cache [\"ls\"]")
      .matches("RUN --mount=type=secret [\"executable\",\"param1\",\"param2\"]")
      .matches("RUN --mount=type=ssh [\"/usr/bin/wc\",\"--help\"]")
      .matches("RUN --mount=type=${mount_type} [\"/usr/bin/wc\",\"--help\"]")
      .matches("RUN --mount=type=${mount_type:-ssh} [\"/usr/bin/wc\",\"--help\"]")
      .matches("RUN --mount=type=${mount_type:+ssh} [\"/usr/bin/wc\",\"--help\"]")
      .matches("    RUN --mount=type=${mount_type:+ssh} [\"/usr/bin/wc\",\"--help\"]")
      .matches("RUN --mount=type=secret [\"c:\\\\Program Files\\\\foo.exe\"]")

      .notMatches("RUND")
      .notMatches("");
  }

  @Test
  void shouldParseRunOptionsShellForm() {
    Assertions.assertThat(DockerLexicalGrammar.RUN)
      .matches("RUN --network=default")
      .matches("RUN --network=default ls")
      .matches("RUN --network=none \"ls\"")
      .matches("RUN --network=host command param1 param2")
      .matches("RUN --security=insecure echo \"This is a test.\" | wc -")
      .matches("RUN --security=sandbox /bin/sh /deploy.sh")
      .matches("RUN --mount=target=. mkdir -p /output && zip -FS -r /output/lambda.zip ./")
      .matches("RUN --mount=target=/ \"/usr/bin/run.sh\"")
      .matches("RUN --mount=type=cache,target=/go/pkg/mod/cache \\\n" +
        "    go mod download")
      .matches("RUN --mount=type=cache,target=/root/.cache/pip \\\n" +
        "    pip3 install -r requirements.txt")
      .matches("RUN msbuild .\\DockerSamples.AspNetExporter.App\\DockerSamples.AspNetExporter.App.csproj /p:OutputPath=c:\\out")
      .matches("    RUN --mount=target=/ \"/usr/bin/run.sh\"")
      .matches("RUN     --mount=target=/   \"/usr/bin/run.sh\"")
      // not exec form
      .matches("RUN [\"la\", \"-bb\"")
      .matches("RUN [\"la\", \"-bb]")
      .matches("RUN \"la\", \"-bb\"]")

      .notMatches("--mount=target=. /bin/sh /deploy.sh");
  }

  @Test
  void shouldCheckParseRunExecFormTree() {
    RunTree tree = DockerTestUtils.parse("RUN [\"executable\",\"param1\",\"param2\"]", DockerLexicalGrammar.RUN);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.RUN);
    assertThat(tree.keyword().value()).isEqualTo("RUN");
    assertTextRange(tree.textRange()).hasRange(1,0,1,36);

    assertThat(tree.arguments()).isNotNull();
    assertThat(tree.arguments().type()).isEqualTo(LiteralListTree.LiteralListType.EXEC);
    assertThat(tree.arguments().literals().stream().map(TextTree::value)).containsExactly("\"executable\"", "\"param1\"", "\"param2\"");
    List<TextRange> textRanges = tree.arguments().literals().stream().map(TextTree::textRange).collect(Collectors.toList());
    assertTextRange(textRanges.get(0)).hasRange(1,5,1,17);
    assertTextRange(textRanges.get(1)).hasRange(1,18,1,26);
    assertTextRange(textRanges.get(2)).hasRange(1,27,1,35);

    assertThat(((SyntaxToken)tree.children().get(0)).value()).isEqualTo("RUN");
    assertThat(tree.children().get(1)).isInstanceOf(ExecFormTree.class);
  }

  @Test
  void shouldCheckParseRunShellFormTree() {
    RunTree tree = DockerTestUtils.parse("RUN executable param1 param2", DockerLexicalGrammar.RUN);

    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.RUN);
    assertThat(tree.keyword().value()).isEqualTo("RUN");
    assertTextRange(tree.textRange()).hasRange(1,0,1,28);

    assertThat(tree.arguments()).isNotNull();
    assertThat(tree.arguments().type()).isEqualTo(LiteralListTree.LiteralListType.SHELL);
    assertThat(tree.arguments().literals().stream().map(TextTree::value)).containsExactly("executable", "param1", "param2");
    List<TextRange> textRanges = tree.arguments().literals().stream().map(TextTree::textRange).collect(Collectors.toList());
    assertTextRange(textRanges.get(0)).hasRange(1,4,1,14);
    assertTextRange(textRanges.get(1)).hasRange(1,15,1,21);
    assertTextRange(textRanges.get(2)).hasRange(1,22,1,28);

    assertThat(((SyntaxToken)tree.children().get(0)).value()).isEqualTo("RUN");
    assertThat(tree.children().get(1)).isInstanceOf(ShellFormTree.class);
  }

  @Test
  void shouldCheckParseRunOptionExecFormTree() {
    RunTree tree = DockerTestUtils.parse("RUN --mount=type=cache,target=/root/.cache/pip [\"executable\",\"param1\",\"param2\"]", DockerLexicalGrammar.RUN);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.RUN);
    assertThat(tree.keyword().value()).isEqualTo("RUN");
    assertTextRange(tree.textRange()).hasRange(1,0,1,79);

    ParamTree option = tree.options().get(0);
    assertThat(((SyntaxToken)option.children().get(0)).value()).isEqualTo("--");
    assertThat(option.name()).isEqualTo("mount");
    assertThat(option.value().value()).isEqualTo("type=cache,target=/root/.cache/pip");
    assertTextRange(option.textRange()).hasRange(1,4,1,46);

    assertThat(tree.arguments()).isNotNull();
    assertThat(tree.arguments().type()).isEqualTo(LiteralListTree.LiteralListType.EXEC);
    assertThat(tree.arguments().literals().stream().map(TextTree::value)).containsExactly("\"executable\"", "\"param1\"", "\"param2\"");
    List<TextRange> textRanges = tree.arguments().literals().stream().map(TextTree::textRange).collect(Collectors.toList());
    assertTextRange(textRanges.get(0)).hasRange(1,48,1,60);
    assertTextRange(textRanges.get(1)).hasRange(1,61,1,69);
    assertTextRange(textRanges.get(2)).hasRange(1,70,1,78);

    assertThat(((SyntaxToken)tree.children().get(0)).value()).isEqualTo("RUN");
    assertThat(tree.children().get(1)).isInstanceOf(ParamTree.class);
    assertThat(tree.children().get(2)).isInstanceOf(ExecFormTree.class);
  }

  @Test
  void shouldCheckParseRunOptionShellFormTree() {
    RunTree tree = DockerTestUtils.parse("RUN --mount=type=${mount_type:+ssh} executable param1 param2", DockerLexicalGrammar.RUN);

    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.RUN);
    assertThat(tree.keyword().value()).isEqualTo("RUN");
    assertTextRange(tree.textRange()).hasRange(1,0,1,60);

    ParamTree option = tree.options().get(0);
    assertThat(((SyntaxToken)option.children().get(0)).value()).isEqualTo("--");
    assertThat(option.name()).isEqualTo("mount");
    assertThat(option.value().value()).isEqualTo("type=${mount_type:+ssh}");
    assertTextRange(option.textRange()).hasRange(1,4,1,35);

    assertThat(tree.arguments()).isNotNull();
    assertThat(tree.arguments().type()).isEqualTo(LiteralListTree.LiteralListType.SHELL);
    assertThat(tree.arguments().literals().stream().map(TextTree::value)).containsExactly("executable", "param1", "param2");
    List<TextRange> textRanges = tree.arguments().literals().stream().map(TextTree::textRange).collect(Collectors.toList());
    assertTextRange(textRanges.get(0)).hasRange(1,36,1,46);
    assertTextRange(textRanges.get(1)).hasRange(1,47,1,53);
    assertTextRange(textRanges.get(2)).hasRange(1,54,1,60);

    assertThat(((SyntaxToken)tree.children().get(0)).value()).isEqualTo("RUN");
    assertThat(tree.children().get(1)).isInstanceOf(ParamTree.class);
    assertThat(tree.children().get(2)).isInstanceOf(ShellFormTree.class);
  }

  @Test
  void shouldCheckParseEmptyRunExecFormTree() {
    RunTree tree = DockerTestUtils.parse("RUN []", DockerLexicalGrammar.RUN);

    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.RUN);
    assertThat(tree.keyword().value()).isEqualTo("RUN");
    assertThat(tree.arguments()).isNotNull();
    assertThat(tree.arguments().literals()).isEmpty();

    assertThat(tree.children().get(1)).isInstanceOf(ExecFormTree.class);
    SeparatedList<ExecFormLiteralTree> literals = ((ExecFormTree) tree.arguments()).literalsWithSeparators();
    assertThat(literals.elementsAndSeparators()).isEmpty();
    assertThat(literals.elements()).isEmpty();
    assertThat(literals.separators()).isEmpty();
  }

  @Test
  void shouldCheckParseEmptyRunTree() {
    RunTree tree = DockerTestUtils.parse("RUN", DockerLexicalGrammar.RUN);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.RUN);
    assertThat(tree.keyword().value()).isEqualTo("RUN");

    assertThat(tree.arguments()).isNull();
  }
}
