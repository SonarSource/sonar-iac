/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.TreeUtils;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.ExecForm;
import org.sonar.iac.docker.tree.api.Flag;
import org.sonar.iac.docker.tree.api.Literal;
import org.sonar.iac.docker.tree.api.RunInstruction;
import org.sonar.iac.docker.tree.api.ShellForm;
import org.sonar.iac.docker.tree.api.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.sonar.iac.common.testing.IacCommonAssertions.assertThat;
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
      .matches("RUN [\"/usr/bin/supervisord\", \"-n\"] -D")
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
      .matches("""
        RUN <<EOF
        apk update
        apk add git
        EOF""")
      .matches("RUN export FLASK_APP=app.py")
      .matches("RUN export FLASK_APP=app.py")
      .matches("RUN RUN set -ex \\\n\t&& apk add --no-cache --virtual .fetch-deps \\\n\t\tgnupg")
      .matches("RUN \"/usr/bin/run.sh\"")
      .matches("    RUN \"/usr/bin/run.sh\"")
      .matches("RUN     \"/usr/bin/run.sh\"")
      .matches("run")
      .matches("RUN service postgresql start && su postgres -c psql < /tmp/create_db.sql")
      .matches("RUN npm cache clean -g -f < /dev/null 2> /dev/null")
      .matches("RUN pip install mongoengine\\>=0.10,\\<0.11 pymongo\\>=3.0,\\<3.1 pyopenssl msgpack-python\\<0.5 pyyaml\\<4 psutil")
      // not exec form
      .matches("RUN \"la\", \"-bb\"]")
      .matches("RUN ${run}")
      .matches("RUN ${run:-test}")
      .matches("RUN ${run%%[a-z]+}")
      .matches("RUN 'this is my var \" and other \"'")
      // a malformed exec form will be treated as shell form
      .matches("RUN [\"la\", \"-bb\"")
      .matches("RUN [\"la\", \"-bb]")
      .matches("RUN [ \"/bin/bash”, “-c” ]")

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
      .matches("RUN \"la\", \"-bb\"]")
      // a malformed exec form
      .matches("RUN [\"la\", \"-bb\"")
      .matches("RUN [\"la\", \"-bb]")

      .notMatches("--mount=target=. /bin/sh /deploy.sh");
  }

  @Test
  void shouldParseRunHereDocument() {
    Assertions.assertThat(DockerLexicalGrammar.RUN)
      .matches("RUN <<EOT\n  mkdir -p foo/bar\nEOT")
      .matches("RUN <<EOT\nEOT")
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

  @Test
  void shouldParseRunWithShellAndHereDocument() {
    Assertions.assertThat(DockerLexicalGrammar.RUN)
      .matches(
        """
          RUN echo <<EOF
          Hello World
          EOF""")
      .matches(
        """
          RUN <<EOF cat >/etc/apt/apt.conf.d/99-retry
          Acquire::Retries "10";
          EOF""")
      .matches(
        """
          RUN patch -Np2 -i - <<"EOF"
          diff --git a/applications/shepherd b/applications/shepherd
          EOF""")
      .matches(
        """
          RUN cat > ".env" <<EOF
              PORT=80
              GIT_REV=${GIT_REV}
              MAGIC_SECRET_KEY=${MAGIC_SECRET_KEY}
          EOF""")
      .matches(
        """
          RUN apt update && \\
              apt upgrade && \\
              apt clean all && \\
              cat <<EOF >> /root/.profile
          # Source global definitions
          if [[ -f /etc/profile ]]
          then
              source /etc/profile
          fi
          EOF""")
      .matches(
        """
          RUN npm i semver && \\
            npm cache  -g -f < /dev/null 2> /dev/null \\
            && cat <<EOF
          Installation done
          EOF""")
      .matches(
        """
          RUN \\
          IFS=' ' read -a PACKAGES <<< "$PACKAGES" ; \\
          pacman -Sy --noconfirm --needed "${PACKAGES[@]}\"""")
      .matches("""
        RUN grappa/configure --cc=$(which gcc) --gen=Ninja \\
                      --third-party=/grappa-third-party \\
                      --shmmax=$((1<<30))""");
  }

  @Test
  void shouldParseMultiline() {
    RunInstruction tree = DockerTestUtils.parse("""
      RUN  \\
              TEST=test && \\
              ls && \\
              curl sLO https://google.com &&\\
              echo TEST | sha256sum --check""",
      DockerLexicalGrammar.RUN);

    assertThat(tree.options()).isEmpty();
    assertThat(tree.arguments()).hasSize(13);
  }

  @Test
  void shouldCheckParseRunExecFormTree() {
    RunInstruction tree = DockerTestUtils.parse("RUN [\"executable\",\"param1\",\"param2\"]", DockerLexicalGrammar.RUN);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.RUN);
    assertThat(tree.keyword().value()).isEqualTo("RUN");
    assertThat(tree.textRange()).hasRange(1, 0, 1, 36);

    assertThat(tree.arguments().stream().map(arg -> ArgumentResolution.of(arg).value())).containsExactly("executable", "param1", "param2");

    assertThat(((SyntaxToken) tree.children().get(0)).value()).isEqualTo("RUN");
    assertThat(tree.children().get(1)).isInstanceOf(ExecForm.class);
  }

  @Test
  void shouldCheckParseRunShellFormTree() {
    RunInstruction tree = DockerTestUtils.parse("RUN executable param1 param2", DockerLexicalGrammar.RUN);

    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.RUN);
    assertThat(tree.keyword().value()).isEqualTo("RUN");
    assertThat(tree.textRange()).hasRange(1, 0, 1, 28);

    assertArgumentsValue(tree.arguments(), "executable", "param1", "param2");

    assertThat(((SyntaxToken) tree.children().get(0)).value()).isEqualTo("RUN");
    assertThat(tree.children().get(1)).isInstanceOf(ShellForm.class);
  }

  @ParameterizedTest
  @MethodSource
  void shouldCheckParseRunShellFormTreeWithEnvVariable(String code, String[] expectedArguments) {
    RunInstruction tree = DockerTestUtils.parse(code, DockerLexicalGrammar.RUN);

    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.RUN);
    assertThat(tree.keyword().value()).isEqualTo("RUN");
    assertThat(tree.textRange()).hasRange(1, 0, 1, code.length());

    assertArgumentsValue(tree.arguments(), expectedArguments);

    assertThat(((SyntaxToken) tree.children().get(0)).value()).isEqualTo("RUN");
    assertThat(tree.children().get(1)).isInstanceOf(ShellForm.class);
  }

  public static Stream<Arguments> shouldCheckParseRunShellFormTreeWithEnvVariable() {
    return Stream.of(
      arguments("RUN VAR=value executable param", new String[] {"VAR=value", "executable", "param"}),
      arguments("RUN VAR='value and more' executable param", new String[] {"VAR=value and more", "executable", "param"}),
      arguments("RUN VAR=\"value and more\" executable param", new String[] {"VAR=value and more", "executable", "param"}));
  }

  @Test
  void shouldCheckParseRunOptionExecFormTree() {
    RunInstruction tree = DockerTestUtils.parse("RUN --mount=type=cache,target=/root/.cache/pip [\"executable\",\"param1\",\"param2\"]", DockerLexicalGrammar.RUN);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.RUN);
    assertThat(tree.keyword().value()).isEqualTo("RUN");
    assertThat(tree.textRange()).hasRange(1, 0, 1, 79);

    Flag option = tree.options().get(0);
    assertThat(((SyntaxToken) option.children().get(0)).value()).isEqualTo("--");
    assertThat(option.name()).isEqualTo("mount");
    assertThat(ArgumentResolution.of(option.value()).value()).isEqualTo("type=cache,target=/root/.cache/pip");
    assertThat(option.textRange()).hasRange(1, 4, 1, 46);

    assertArgumentsValue(tree.arguments(), "executable", "param1", "param2");

    assertThat(((SyntaxToken) tree.children().get(0)).value()).isEqualTo("RUN");
    assertThat(tree.children().get(1)).isInstanceOf(Flag.class);
    assertThat(tree.children().get(2)).isInstanceOf(ExecForm.class);
  }

  @Test
  void shouldCheckParseRunOptionShellFormTree() {
    RunInstruction tree = DockerTestUtils.parse("RUN --mount=type=${mount_type:+ssh} executable param1 param2", DockerLexicalGrammar.RUN);

    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.RUN);
    assertThat(tree.keyword().value()).isEqualTo("RUN");
    assertThat(tree.textRange()).hasRange(1, 0, 1, 60);

    Flag option = tree.options().get(0);
    assertThat(((SyntaxToken) option.children().get(0)).value()).isEqualTo("--");
    assertThat(option.name()).isEqualTo("mount");
    assertThat(ArgumentResolution.of(option.value()).value()).isEqualTo("type=");
    assertThat(option.textRange()).hasRange(1, 4, 1, 35);

    assertArgumentsValue(tree.arguments(), "executable", "param1", "param2");

    assertThat(((SyntaxToken) tree.children().get(0)).value()).isEqualTo("RUN");
    assertThat(tree.children().get(1)).isInstanceOf(Flag.class);
    assertThat(tree.children().get(2)).isInstanceOf(ShellForm.class);
  }

  @Test
  void shouldCheckParseEmptyRunExecFormTree() {
    RunInstruction tree = DockerTestUtils.parse("RUN []", DockerLexicalGrammar.RUN);

    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.RUN);
    assertThat(tree.keyword().value()).isEqualTo("RUN");
    assertThat(tree.arguments()).isEmpty();

    assertThat(tree.children().get(1)).isInstanceOf(ExecForm.class);
    assertThat(tree.arguments()).isEmpty();
  }

  @Test
  void shouldCheckParseEmptyRunTree() {
    RunInstruction tree = DockerTestUtils.parse("RUN", DockerLexicalGrammar.RUN);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.RUN);
    assertThat(tree.keyword().value()).isEqualTo("RUN");

    assertThat(tree.arguments()).isEmpty();
  }

  @Test
  void shouldCheckParseRunMultiLineFileEnding() {
    String toParse = """
      RUN <<FILE1
      line 1
      line 2
      FILE1""";
    RunInstruction tree = DockerTestUtils.parse(toParse, DockerLexicalGrammar.RUN);
    assertThat(tree.textRange()).hasRange(1, 0, 4, 5);

    assertThat(tree.keyword().value()).isEqualTo("RUN");
    assertThat(tree.arguments()).hasSize(6);
    assertArgumentsValue(tree.arguments(), "<<FILE1", "line", "1", "line", "2", "FILE1");
  }

  @Test
  void shouldCheckParseRunMultiLineFollowedByOtherInstructionAndDash() {
    String toParse = """
      RUN <<-FILE1 line 0
      line 1
      line 2
      FILE1
      HEALTHCHECK NONE""";
    RunInstruction tree = DockerTestUtils.parse(toParse, DockerLexicalGrammar.RUN);
    assertThat(tree.textRange()).hasRange(1, 0, 4, 5);

    assertThat(tree.keyword().value()).isEqualTo("RUN");
    assertThat(tree.arguments()).hasSize(8);
    assertArgumentsValue(tree.arguments(), "<<-FILE1", "line", "0", "line", "1", "line", "2", "FILE1");
  }

  @Test
  void shouldCheckParseRunMultiLineMultipleRedirect() {
    String toParse = """
      RUN <<FILE1 <<FILE2
      line file 1
      FILE1
      line file 2
      FILE2
      HEALTHCHECK NONE""";
    RunInstruction tree = DockerTestUtils.parse(toParse, DockerLexicalGrammar.RUN);
    assertThat(tree.textRange()).hasRange(1, 0, 5, 5);

    assertThat(tree.keyword().value()).isEqualTo("RUN");
    assertThat(tree.arguments()).hasSize(10);
    assertArgumentsValue(tree.arguments(), "<<FILE1", "<<FILE2", "line", "file", "1", "FILE1", "line", "file", "2", "FILE2");
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "RUN executable    \\\n# my comment\nparameters",
    "RUN executable  \\\n# my comment\n  parameters",
    "RUN executable  \\\n# com\n  \n  parameters",
    "RUN executable  \\\n# com\n  \n# com\n  parameters"
  })
  void shouldHaveInlineCommentAttachedToInaccessibleWhitespace(String toParse) {
    RunInstruction tree = DockerTestUtils.parse(toParse, DockerLexicalGrammar.RUN);

    assertThat(tree.keyword().value()).isEqualTo("RUN");
    assertThat(tree.arguments()).hasSize(2);
    assertArgumentsValue(tree.arguments(), "executable", "parameters");

    // Comments are associated to the whitespace which are not accessible from the tree because they are ignored/not stored on the grammar
    SyntaxTokenImpl syntaxToken1 = (SyntaxTokenImpl) TreeUtils.lastDescendant(tree.arguments().get(0), SyntaxTokenImpl.class::isInstance).get();
    assertThat(syntaxToken1.value()).isEqualTo("executable");
    assertThat(syntaxToken1.comments()).isEmpty();
    SyntaxTokenImpl syntaxToken2 = (SyntaxTokenImpl) TreeUtils.lastDescendant(tree.arguments().get(1), SyntaxTokenImpl.class::isInstance).get();
    assertThat(syntaxToken2.value()).isEqualTo("parameters");
    assertThat(syntaxToken2.comments()).isEmpty();
  }

  @Test
  void shouldHaveInlineCommentAttachedToPreviousToken() {
    String toParse = """
      RUN executable\\
      # my comment
           parameters""";
    RunInstruction tree = DockerTestUtils.parse(toParse, DockerLexicalGrammar.RUN);
    assertThat(tree.textRange()).hasRange(1, 0, 3, 15);

    assertThat(tree.keyword().value()).isEqualTo("RUN");
    assertThat(tree.arguments()).hasSize(2);
    assertArgumentsValue(tree.arguments(), "executable", "parameters");

    SyntaxTokenImpl syntaxToken = (SyntaxTokenImpl) TreeUtils.lastDescendant(tree.arguments().get(0), SyntaxTokenImpl.class::isInstance).get();
    assertThat(syntaxToken.value()).isEqualTo("executable");

    assertThat(syntaxToken.comments()).hasSize(1);
    Comment comment = syntaxToken.comments().get(0);
    assertThat(comment.value()).isEqualTo("# my comment");
    assertThat(comment.contentText()).isEqualTo("my comment");
    assertThat(comment.textRange()).hasRange(2, 0, 2, 12);
  }

  @Test
  void shouldHaveInlineCommentAttachedToMergedTokens() {
    String toParse = """
      RUN executable\\
      # my comment
      parameters""";
    RunInstruction tree = DockerTestUtils.parse(toParse, DockerLexicalGrammar.RUN);
    assertThat(tree.textRange()).hasRange(1, 0, 3, 10);

    assertThat(tree.keyword().value()).isEqualTo("RUN");
    assertThat(tree.arguments()).hasSize(1);
    assertArgumentsValue(tree.arguments(), "executableparameters");

    SyntaxTokenImpl syntaxToken = (SyntaxTokenImpl) TreeUtils.lastDescendant(tree, SyntaxTokenImpl.class::isInstance).get();
    assertThat(syntaxToken.value()).isEqualTo("executableparameters");
    assertThat(syntaxToken.comments())
      .hasSize(1)
      .extracting("value").containsExactly("# my comment");
  }

  @Test
  void shouldHaveInlineCommentAttachedToMergedTokensMultipleComment() {
    String toParse = """
      RUN executable\\
      # my comment 1
          # my comment 2
      parameters""";
    RunInstruction tree = DockerTestUtils.parse(toParse, DockerLexicalGrammar.RUN);
    assertThat(tree.textRange()).hasRange(1, 0, 4, 10);

    assertThat(tree.keyword().value()).isEqualTo("RUN");
    assertThat(tree.arguments()).hasSize(1);
    assertArgumentsValue(tree.arguments(), "executableparameters");

    SyntaxTokenImpl syntaxToken = (SyntaxTokenImpl) TreeUtils.lastDescendant(tree, SyntaxTokenImpl.class::isInstance).get();
    assertThat(syntaxToken.value()).isEqualTo("executableparameters");
    assertThat(syntaxToken.comments())
      .hasSize(2)
      .extracting("value").containsExactly("# my comment 1", "# my comment 2");
  }

  @Test
  void endOfFileAfterInlineComment() {
    String toParse = """
      RUN executable\\
      # my comment""";
    RunInstruction tree = DockerTestUtils.parse(toParse, DockerLexicalGrammar.RUN);
    assertThat(tree.textRange()).hasRange(1, 0, 2, 12);

    assertThat(tree.keyword().value()).isEqualTo("RUN");
    assertThat(tree.arguments()).hasSize(1);
    assertArgumentsValue(tree.arguments(), "executable");

    SyntaxTokenImpl syntaxToken = (SyntaxTokenImpl) TreeUtils.lastDescendant(tree, SyntaxTokenImpl.class::isInstance).get();
    assertThat(syntaxToken.value()).isEqualTo("executable");
    assertThat(syntaxToken.comments()).isEmpty();
  }

  @Test
  void shouldParseRunInstructionWithMultipleHeredoc() {
    var code = """
      RUN cat <<TEXT1 && cat <<TEXT2
      Text1
      TEXT1
      Text2
      TEXT2
      """;
    var run = DockerTestUtils.<RunInstruction>parse(code, DockerLexicalGrammar.RUN);

    assertThat(run.arguments())
      .map(a -> ((Literal) a.expressions().get(0)).value())
      .containsExactly("cat", "<<TEXT1", "&&", "cat", "<<TEXT2", "Text1", "TEXT1", "Text2", "TEXT2");
  }

  @Test
  void shouldTreatMalformedExecFormAsShellForm() {
    var tree = DockerTestUtils.<RunInstruction>parse("RUN [ \"/bin/bash”, “-c” ]", DockerLexicalGrammar.RUN);

    assertThat(tree.getKindOfArgumentList()).isEqualTo(DockerTree.Kind.SHELL_FORM);
    assertArgumentsValue(tree.arguments(), "[", "\"/bin/bash”, “-c” ]");
  }

  @Test
  void shouldParseWeirdButValidExecForm() {
    var tree = DockerTestUtils.<RunInstruction>parse("RUN [ \"/bin/bash”, “-c\" ]", DockerLexicalGrammar.RUN);

    assertThat(tree.getKindOfArgumentList()).isEqualTo(DockerTree.Kind.EXEC_FORM);
    assertArgumentsValue(tree.arguments(), "/bin/bash”, “-c");
  }

  @Test
  void shouldParseMalformedButValidShellForm() {
    var tree = DockerTestUtils.<RunInstruction>parse("RUN [ '/bin/bash”, “-c” ]", DockerLexicalGrammar.RUN);

    assertThat(tree.getKindOfArgumentList()).isEqualTo(DockerTree.Kind.SHELL_FORM);
    assertArgumentsValue(tree.arguments(), "[", "'/bin/bash”, “-c” ]");
  }

  @Test
  void shouldTreatAnotherMalformedExecFormAsShellForm() {
    var tree = DockerTestUtils.<RunInstruction>parse("RUN [ '/bin/bash”, “-c' ]", DockerLexicalGrammar.RUN);

    assertThat(tree.getKindOfArgumentList()).isEqualTo(DockerTree.Kind.SHELL_FORM);
    assertArgumentsValue(tree.arguments(), "[", "/bin/bash”, “-c", "]");
  }

  @Test
  void shouldConvertToString() {
    RunInstruction tree = DockerTestUtils.parse("RUN executable param1 param2", DockerLexicalGrammar.RUN);
    assertThat(tree).hasToString("RUN executable param1 param2");
  }
}
