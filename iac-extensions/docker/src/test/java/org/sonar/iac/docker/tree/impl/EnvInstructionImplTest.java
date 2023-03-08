/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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

import org.junit.jupiter.api.Test;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.EnvInstruction;
import org.sonar.iac.docker.tree.api.KeyValuePair;
import org.sonar.iac.docker.tree.api.KeyValuePairAssert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class EnvInstructionImplTest {
  @Test
  void matchingSimple() {
    Assertions.assertThat(DockerLexicalGrammar.ENV)
      .matches("ENV key1=value1")
      .matches("ENV key1=")
      .matches("ENV key1")
      .matches("ENV key1 value1")
      .matches("ENV key1 value1 still_value1 again_value1")
      .matches("ENV key1 \"value1\" still_value1 again_value1")
      .matches("    ENV key1=value1")
      .matches("env key1=value1")
      .matches("ENV key1=value1 key2=value2")
      .matches("ENV \"key1\"=\"value1\" \"key2\"=\"value2\"")
      .matches("ENV \"key1\"=value1 key2=\"value2\"")
      .matches("ENV CPATH=\"/usr/include/vtk-6.2\":$CPATH")
      .matches("ENV CPATH=\"my path\"")
      .matches("ENV CPATH=test\"t\"")
      .matches("ENV CPATH=test\" with spacees \"")
      .matches("ENV \"key1 value1 still_value1 again_value1\"")
      .notMatches("ENV")
      .notMatches("ENV ")
      .notMatches("ENV CPATH=my path\"")
      .notMatches("ENV ACCESS_TOKEN=${ARG");
  }

  @Test
  void envInstructionWithoutEqualsOperator() {
    EnvInstruction tree = parse("ENV key1 value1", DockerLexicalGrammar.ENV);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ENV);
    assertThat(tree.keyword().value()).isEqualTo("ENV");
    assertThat(tree.textRange().start().line()).isEqualTo(1);
    assertThat(tree.textRange().start().lineOffset()).isZero();
    assertThat(tree.textRange().end().line()).isEqualTo(1);
    assertThat(tree.textRange().end().lineOffset()).isEqualTo(15);
    assertThat(tree.children()).hasSize(2);
    assertThat(tree.environmentVariables()).hasSize(1);

    KeyValuePair keyValuePair = tree.environmentVariables().get(0);
    KeyValuePairAssert.assertThat(keyValuePair)
      .hasKind(DockerTree.Kind.KEY_VALUE_PAIR)
      .hasKey("key1")
      .hasEqualSignNull()
      .hasValue("value1");
  }

  @Test
  void envInstructionWithEqualsAndSpecialCharacters() {
    EnvInstruction tree = parse("ENV CPATH=\"/usr/include/vtk-6.2\":CPATH", DockerLexicalGrammar.ENV);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ENV);
    assertThat(tree.keyword().value()).isEqualTo("ENV");
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 38);
    assertThat(tree.children()).hasSize(2);

    assertThat(tree.environmentVariables()).hasSize(1);

    KeyValuePair keyValuePair = tree.environmentVariables().get(0);
    KeyValuePairAssert.assertThat(keyValuePair)
      .hasKind(DockerTree.Kind.KEY_VALUE_PAIR)
      .hasKey("CPATH")
      .hasEqualSign("=")
      .hasValue("/usr/include/vtk-6.2:CPATH");
  }

  @Test
  void envInstructionWithEmptyValue() {
    EnvInstruction tree = parse("ENV key1=", DockerLexicalGrammar.ENV);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ENV);
    assertThat(tree.keyword().value()).isEqualTo("ENV");
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 9);
    assertThat(tree.children()).hasSize(2);
    assertThat(tree.environmentVariables()).hasSize(1);

    KeyValuePair keyValuePair = tree.environmentVariables().get(0);
    KeyValuePairAssert.assertThat(keyValuePair)
      .hasKind(DockerTree.Kind.KEY_VALUE_PAIR)
      .hasKey("key1")
      .hasEqualSign("=")
      .hasValueNull();
  }

  @Test
  void envInstructionWithoutEqualsOperatorLong() {
    EnvInstruction tree = parse("ENV key1 value1 still_value1 again_value1", DockerLexicalGrammar.ENV);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ENV);
    assertThat(tree.keyword().value()).isEqualTo("ENV");
    assertThat(tree.environmentVariables()).hasSize(1);
    assertThat(tree.children()).hasSize(2);
    KeyValuePair keyValuePair = tree.environmentVariables().get(0);
    KeyValuePairAssert.assertThat(keyValuePair)
      .hasKind(DockerTree.Kind.KEY_VALUE_PAIR)
      .hasKey("key1")
      .hasEqualSignNull()
      .hasValue("value1 still_value1 again_value1");
  }

  // SONARIAC-503
  @Test
  void shouldParseMultiline() {
    EnvInstruction tree = parse("ENV JAVA_OPTS -Duser.timezone=\\$TIMEZONE -XX:+UseParallelGC\\\n" +
        "    -Dlog4j.configuration=config.properties -Xms\\$XMS -Xmx\\$XMX \\\n" +
        "    -XX:MaxPermSize=\\$MAXPERMSIZE -Dbanproxy.jdbc.url=\\$BANPROXY_JDBC_URL \\\n" +
        "    -DlogFileDir=\\$LOGFILEDIR \\\n" +
        "    -Dscheme=\\$SCHEME \\\n" +
        "    -Dproxy.port=\\$PROXY_PORT \\\n" +
        "    -Dproxy.name=\\$PROXY_NAME",
      DockerLexicalGrammar.ENV);
    assertThat(tree.environmentVariables()).hasSize(1);
  }
}
