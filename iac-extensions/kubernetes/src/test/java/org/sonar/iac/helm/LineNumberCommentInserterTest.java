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
package org.sonar.iac.helm;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class LineNumberCommentInserterTest {

  @Test
  void shouldAddCommentLineNumberForActualChart() {
    String code = "apiVersion: v1\n" +
      "kind: Pod\n" +
      "metadata:\n" +
      "  name: example\n" +
      "{{ if .Values.service.annotations}}\n" +
      "  annotations:\n" +
      "    {{- range $key, $value := .Values.service.annotations }}\n" +
      "    {{ $key }}: {{ $value | quote }}\n" +
      "    {{- end }}\n" +
      "{{- end }}\n" +
      "spec:\n" +
      "  containers:\n" +
      "    - name: web\n" +
      "      image: nginx\n" +
      "      ports:\n" +
      "        - name: web\n" +
      "          containerPort: 80\n" +
      "          protocol: TCP\n" +
      "      securityContext:\n" +
      "        allowPrivilegeEscalation: true # TODO SONARIAC-1130 Parse a Helm file containing loops without crash";
    String expected = "apiVersion: v1 #1\n" +
      "kind: Pod #2\n" +
      "metadata: #3\n" +
      "  name: example #4\n" +
      "{{ if .Values.service.annotations}} #5\n" +
      "  annotations: #6\n" +
      "    {{- range $key, $value := .Values.service.annotations }} #7\n" +
      "    {{ $key }}: {{ $value | quote }} #8\n" +
      "    {{- end }} #9\n" +
      "{{- end }} #10\n" +
      "spec: #11\n" +
      "  containers: #12\n" +
      "    - name: web #13\n" +
      "      image: nginx #14\n" +
      "      ports: #15\n" +
      "        - name: web #16\n" +
      "          containerPort: 80 #17\n" +
      "          protocol: TCP #18\n" +
      "      securityContext: #19\n" +
      "        allowPrivilegeEscalation: true # TODO SONARIAC-1130 Parse a Helm file containing loops without crash #20";
    checkHelmProcessing(code, expected);
  }

  @Test
  void shouldAddCommentLineNumber() {
    checkHelmProcessing(
      code("line1",
        "line2",
        "line3"),
      code("line1 #1",
        "line2 #2",
        "line3 #3"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"\n", "\r", "\r\n", "\u2028", "\u2029"})
  void shouldKeepProperCarriageReturn(String carriageReturn) {
    checkHelmProcessing(
      codeSpecific(carriageReturn,
        "line1",
        "",
        "",
        "line4"),
      codeSpecific(carriageReturn,
        "line1 #1",
        " #2",
        " #3",
        "line4 #4"));
  }

  @Test
  void shouldAddCommentLineNumberOnSingleLine() {
    checkHelmProcessing("line1", "line1 #1");
  }

  @Test
  void shouldAddCommentLineNumberOnEmptyFile() {
    checkHelmProcessing("", " #1");
  }

  @Test
  void shouldAddCommentLineNumberOnEmptyLine() {
    checkHelmProcessing(
      code("line1",
        "",
        "line3"),
      code("line1 #1",
        " #2",
        "line3 #3"));
  }

  @Test
  void shouldAddCommentLineNumberOnAlreadyCommentedLine() {
    checkHelmProcessing("line1 # some comment", "line1 # some comment #1");
  }

  @Test
  void shouldNotAddLineCommentWhenSeparateDocument() {
    checkHelmProcessing(
      code("line1",
        "---",
        "line3"),
      code("line1 #1",
        "---",
        "line3 #3"));
  }

  @Test
  void shouldNotAddLineCommentWhenSeparateDocumentAtTheEnd() {
    checkHelmProcessing(
      code("line1",
        "---"),
      code("line1 #1",
        "---"));
  }

  @Test
  void shouldNotAddLineCommentWhenEndDocument() {
    checkHelmProcessing(
      code("line1",
        "..."),
      code("line1 #1",
        "..."));
  }

  void checkHelmProcessing(String source, String expect) {
    assertThat(LineNumberCommentInserter.addLineComments(source)).isEqualTo(expect);
  }

  String codeSpecific(String carriageReturn, String... lines) {
    return StringUtils.join(lines, carriageReturn);
  }

  String code(String... lines) {
    return codeSpecific("\n", lines);
  }
}
