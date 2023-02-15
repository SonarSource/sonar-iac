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
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.Flag;
import org.sonar.iac.docker.tree.api.Literal;
import org.sonar.iac.docker.tree.api.RunInstruction;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.ExecForm;
import org.sonar.iac.docker.tree.api.LiteralList;
import org.sonar.iac.docker.tree.api.SeparatedList;
import org.sonar.iac.docker.tree.api.ShellForm;
import org.sonar.iac.docker.tree.api.SyntaxToken;
import org.sonar.iac.docker.utils.ArgumentUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;
import static org.sonar.iac.docker.TestUtils.assertArgumentsValue;

class RunInstructionImplTest {

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
      .matches("RUN <<EOF\n" +
        "apk update\n" +
        "apk add git\n" +
        "EOF")
      .matches("RUN export FLASK_APP=app.py")
      .matches("RUN export FLASK_APP=app.py")
      .matches("RUN RUN set -ex \\\n\t&& apk add --no-cache --virtual .fetch-deps \\\n\t\tgnupg")
      .matches("RUN \"/usr/bin/run.sh\"")
      .matches("    RUN \"/usr/bin/run.sh\"")
      .matches("RUN     \"/usr/bin/run.sh\"")
      .matches("run")
      // not exec form
      .matches("RUN [\"la\", \"-bb\"")
      .matches("RUN \"la\", \"-bb\"]")
      .matches("RUN ${run}")
      .matches("RUN ${run:-test}")
      .matches("RUN ${run%%[a-z]+}")
      .matches("RUN 'this is my var \" and other \"'")

      .notMatches("RUN [\"la\", \"-bb]")
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
      .matches("RUN [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12;     Write-Host \"Downloading Prometheus version: $env:PROMETHEUS_VERSION\";")
      // not exec form
      .matches("RUN [\"la\", \"-bb\"")
      .matches("RUN \"la\", \"-bb\"]")

      .notMatches("RUN [\"la\", \"-bb]")
      .notMatches("--mount=target=. /bin/sh /deploy.sh");
  }

  @Test
  void shouldParseRunHereDocument() {
    Assertions.assertThat(DockerLexicalGrammar.RUN)
      .matches("RUN <<EOT\n  mkdir -p foo/bar\nEOT")
      .matches("RUN <<EOT early code\n  mkdir -p foo/bar\nEOT")
      .matches("   RUN       <<EOT\n  mkdir -p foo/bar\nEOT")
      .matches("RUN <<eot\n  mkdir -p foo/bar\neot")
      .matches("RUN <<EOT\n\n  mkdir -p foo/bar\n\n\nEOT")
      .matches("RUN <<EOT\r\n  mkdir -p foo/bar\r\nEOT")
      .matches("RUN <<EOT1 <<EOT2\n  mkdir -p foo/bar\nEOT1\n  mkdir -p foo/bar\nEOT2")
      .matches("RUN <<-EOT\n  mkdir -p foo/bar\nEOT")
      .matches("RUN <<\"EOT\"\n  mkdir -p foo/bar\nEOT")
      .notMatches("RUN <EOT\n  mkdir -p foo/bar\nEOT")
      .notMatches("RUN <<EOT\n  mkdir -p foo/bar\nEOT5");
  }

  // SONARIAC-504
  @Test
  void shouldParseMultiline() {
    RunInstruction tree = DockerTestUtils.parse("RUN  \\\n" +
        "        TEST=test && \\\n" +
        "        ls && \\\n" +
        "        curl sLO https://google.com &&\\\n" +
        "        echo TEST | sha256sum --check",
      DockerLexicalGrammar.RUN);

    assertThat(tree.options()).isEmpty();
    assertThat(tree.arguments()).isNotNull();
    assertThat(tree.arguments().type()).isEqualTo(LiteralList.LiteralListType.SHELL);
    assertThat(tree.arguments().arguments()).hasSize(13);
  }

  @Test
  void shouldCheckParseRunExecFormTree() {
    RunInstruction tree = DockerTestUtils.parse("RUN [\"executable\",\"param1\",\"param2\"]", DockerLexicalGrammar.RUN);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.RUN);
    assertThat(tree.keyword().value()).isEqualTo("RUN");
    assertTextRange(tree.textRange()).hasRange(1,0,1,36);

    assertThat(tree.arguments()).isNotNull();
    assertThat(tree.arguments().type()).isEqualTo(LiteralList.LiteralListType.EXEC);
    assertThat(tree.arguments().arguments().stream().map(arg -> ArgumentUtils.resolve(arg).value())).containsExactly("executable", "param1", "param2");

    assertThat(((SyntaxToken)tree.children().get(0)).value()).isEqualTo("RUN");
    assertThat(tree.children().get(1)).isInstanceOf(ExecForm.class);
  }

  @Test
  void shouldCheckParseRunShellFormTree() {
    RunInstruction tree = DockerTestUtils.parse("RUN executable param1 param2", DockerLexicalGrammar.RUN);

    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.RUN);
    assertThat(tree.keyword().value()).isEqualTo("RUN");
    assertTextRange(tree.textRange()).hasRange(1,0,1,28);

    assertThat(tree.arguments()).isNotNull();
    assertThat(tree.arguments().type()).isEqualTo(LiteralList.LiteralListType.SHELL);
    assertArgumentsValue(tree.arguments().arguments(), "executable", "param1", "param2");
    List<TextRange> textRanges = tree.arguments().arguments()
      .stream().map(ArgumentUtils::resolve).map(ArgumentUtils.ArgumentResolution::textRange).collect(Collectors.toList());
    assertTextRange(textRanges.get(0)).hasRange(1,4,1,14);
    assertTextRange(textRanges.get(1)).hasRange(1,15,1,21);
    assertTextRange(textRanges.get(2)).hasRange(1,22,1,28);

    assertThat(((SyntaxToken)tree.children().get(0)).value()).isEqualTo("RUN");
    assertThat(tree.children().get(1)).isInstanceOf(ShellForm.class);
  }

  @Test
  void shouldCheckParseRunOptionExecFormTree() {
    RunInstruction tree = DockerTestUtils.parse("RUN --mount=type=cache,target=/root/.cache/pip [\"executable\",\"param1\",\"param2\"]", DockerLexicalGrammar.RUN);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.RUN);
    assertThat(tree.keyword().value()).isEqualTo("RUN");
    assertTextRange(tree.textRange()).hasRange(1,0,1,79);

    Flag option = tree.options().get(0);
    assertThat(((SyntaxToken)option.children().get(0)).value()).isEqualTo("--");
    assertThat(option.name()).isEqualTo("mount");
    assertThat(ArgumentUtils.resolve(option.value()).value()).isEqualTo("type=cache,target=/root/.cache/pip");
    assertTextRange(option.textRange()).hasRange(1,4,1,46);

    assertThat(tree.arguments()).isNotNull();
    assertThat(tree.arguments().type()).isEqualTo(LiteralList.LiteralListType.EXEC);
    assertArgumentsValue(tree.arguments().arguments(), "executable", "param1", "param2");

    assertThat(((SyntaxToken)tree.children().get(0)).value()).isEqualTo("RUN");
    assertThat(tree.children().get(1)).isInstanceOf(Flag.class);
    assertThat(tree.children().get(2)).isInstanceOf(ExecForm.class);
  }

  @Test
  void shouldCheckParseRunOptionShellFormTree() {
    RunInstruction tree = DockerTestUtils.parse("RUN --mount=type=${mount_type:+ssh} executable param1 param2", DockerLexicalGrammar.RUN);

    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.RUN);
    assertThat(tree.keyword().value()).isEqualTo("RUN");
    assertTextRange(tree.textRange()).hasRange(1,0,1,60);

    Flag option = tree.options().get(0);
    assertThat(((SyntaxToken)option.children().get(0)).value()).isEqualTo("--");
    assertThat(option.name()).isEqualTo("mount");
    assertThat(ArgumentUtils.resolve(option.value()).value()).isNull();
    assertTextRange(option.textRange()).hasRange(1,4,1,35);

    assertThat(tree.arguments()).isNotNull();
    assertThat(tree.arguments().type()).isEqualTo(LiteralList.LiteralListType.SHELL);
    assertArgumentsValue(tree.arguments().arguments(), "executable", "param1", "param2");
    List<TextRange> textRanges = tree.arguments().arguments()
      .stream().map(ArgumentUtils::resolve).map(ArgumentUtils.ArgumentResolution::textRange).collect(Collectors.toList());
    assertTextRange(textRanges.get(0)).hasRange(1,36,1,46);
    assertTextRange(textRanges.get(1)).hasRange(1,47,1,53);
    assertTextRange(textRanges.get(2)).hasRange(1,54,1,60);

    assertThat(((SyntaxToken)tree.children().get(0)).value()).isEqualTo("RUN");
    assertThat(tree.children().get(1)).isInstanceOf(Flag.class);
    assertThat(tree.children().get(2)).isInstanceOf(ShellForm.class);
  }

  @Test
  void shouldCheckParseEmptyRunExecFormTree() {
    RunInstruction tree = DockerTestUtils.parse("RUN []", DockerLexicalGrammar.RUN);

    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.RUN);
    assertThat(tree.keyword().value()).isEqualTo("RUN");
    assertThat(tree.arguments()).isNotNull();
    assertThat(tree.arguments().arguments()).isEmpty();

    assertThat(tree.children().get(1)).isInstanceOf(ExecForm.class);
    SeparatedList<Argument> literals = ((ExecForm) tree.arguments()).argumentsWithSeparators();
    assertThat(literals.elementsAndSeparators()).isEmpty();
    assertThat(literals.elements()).isEmpty();
    assertThat(literals.separators()).isEmpty();
  }

  @Test
  void shouldCheckParseEmptyRunTree() {
    RunInstruction tree = DockerTestUtils.parse("RUN", DockerLexicalGrammar.RUN);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.RUN);
    assertThat(tree.keyword().value()).isEqualTo("RUN");

    assertThat(tree.arguments()).isNull();
  }

  @Test
  void shouldCheckParseRunMultiLineFileEnding() {
    String toParse = "RUN <<FILE1\n" +
      "line 1\n" +
      "line 2\n" +
      "FILE1";
    RunInstruction tree = DockerTestUtils.parse(toParse, DockerLexicalGrammar.RUN);
    assertThat(tree.arguments().type()).isEqualTo(LiteralList.LiteralListType.HEREDOC);
    assertThat(tree.arguments().getKind()).isEqualTo(DockerTree.Kind.HEREDOCUMENT);
    assertTextRange(tree.textRange()).hasRange(1,0,4,5);

    assertThat(tree.keyword().value()).isEqualTo("RUN");
    assertThat(tree.arguments()).isNotNull();
    assertThat(tree.arguments().arguments()).hasSize(1);
    assertThat(((Literal)tree.arguments().arguments().get(0).expressions().get(0)).value()).isEqualTo("<<FILE1\nline 1\nline 2\nFILE1");
  }

  @Test
  void shouldCheckParseRunMultiLineFollowedByOtherInstructionAndDash() {
    String toParse = "RUN <<-FILE1 line 0\n" +
      "line 1\n" +
      "line 2\n" +
      "FILE1\n" +
      "HEALTHCHECK NONE";
    RunInstruction tree = DockerTestUtils.parse(toParse, DockerLexicalGrammar.RUN);
    assertTextRange(tree.textRange()).hasRange(1,0,4,5);

    assertThat(tree.keyword().value()).isEqualTo("RUN");
    assertThat(tree.arguments()).isNotNull();
    assertThat(tree.arguments().arguments()).hasSize(1);
    assertThat(((Literal)tree.arguments().arguments().get(0).expressions().get(0)).value()).isEqualTo("<<-FILE1 line 0\nline 1\nline 2\nFILE1");
  }

  @Test
  void shouldCheckParseRunMultiLineMultipleRedirect() {
    String toParse = "RUN <<FILE1 <<FILE2\n" +
      "line file 1\n" +
      "FILE1\n" +
      "line file 2\n" +
      "FILE2\n" +
      "HEALTHCHECK NONE";
    RunInstruction tree = DockerTestUtils.parse(toParse, DockerLexicalGrammar.RUN);
    assertTextRange(tree.textRange()).hasRange(1,0,5,5);

    assertThat(tree.keyword().value()).isEqualTo("RUN");
    assertThat(tree.arguments()).isNotNull();
    assertThat(tree.arguments().arguments()).hasSize(1);
    assertThat(((LiteralImpl)tree.arguments().arguments().get(0).expressions().get(0)).value()).isEqualTo("<<FILE1 <<FILE2\nline file 1\nFILE1\nline file 2\nFILE2");
  }
}
