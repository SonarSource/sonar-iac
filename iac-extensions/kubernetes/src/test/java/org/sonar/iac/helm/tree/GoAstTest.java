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
package org.sonar.iac.helm.tree;

import com.google.protobuf.Any;
import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.iac.helm.tree.api.ActionNode;
import org.sonar.iac.helm.tree.api.CommandNode;
import org.sonar.iac.helm.tree.api.FieldNode;
import org.sonar.iac.helm.tree.api.ListNode;
import org.sonar.iac.helm.tree.utils.GoTemplateAstConverter;
import org.sonar.iac.helm.utils.GoAstSupplier;

import static org.sonar.iac.common.testing.IacTestUtils.code;

class GoAstTest {
  private GoAstSupplier goAstSupplier;
  @TempDir
  static File tempDir;

  @BeforeEach
  void setUp() throws IOException {
    goAstSupplier = new GoAstSupplier(tempDir);
  }

  @Test
  void shouldIgnoreNullInput() {
    Assertions.assertThat(Tree.fromPbTree(null)).isNull();
  }

  @Test
  void shouldIgnoreUnknownNode() {
    var any = Any.newBuilder().setTypeUrl("unknown").build();
    Assertions.assertThat(GoTemplateAstConverter.unpackNode(any)).isNull();
  }

  @Test
  void shouldBuildTreeFromSimpleTemplate() throws IOException {
    var source = "{{ .Values.header }}";
    var values = "header: 'apiVersion: v1'\n";
    var chart = "apiVersion: v2\nname: my-chart\n";

    var tree = goAstSupplier.goAstFromSource(source, values, chart);

    Assertions.assertThat(tree).satisfies(t -> {
      Assertions.assertThat(t).isNotNull();
      Assertions.assertThat(t.name()).isEqualTo("my-chart/templates/test.yaml");
      Assertions.assertThat(t.parseName()).isEqualTo("my-chart/templates/test.yaml");
      Assertions.assertThat(t.mode()).isZero();
      Assertions.assertThat(t.root()).isInstanceOf(ListNode.class);
    });
    var node = tree.root().nodes().get(0);
    Assertions.assertThat(node).isInstanceOf(ActionNode.class);
    var commandNode = (CommandNode) ((ActionNode) node).pipe().commands().get(0);
    Assertions.assertThat(commandNode.arguments().get(0))
      .isInstanceOf(FieldNode.class)
      .satisfies(fieldNode -> {
        Assertions.assertThat(((FieldNode) fieldNode).identifiers()).containsExactly("Values", "header");
      });
  }

  @ParameterizedTest
  @MethodSource
  void shouldBuildTreeFromTemplate(String source) throws IOException {
    var values = "header: 'apiVersion: v1'\nfallbacks:\n  foo: 'bar'\n";
    var chart = "apiVersion: v2\nname: my-chart\n";

    var tree = goAstSupplier.goAstFromSource(source, values, chart);

    Assertions.assertThat(tree).isNotNull();
    Assertions.assertThat(tree.root().nodes()).isNotEmpty();
  }

  static Stream<String> shouldBuildTreeFromTemplate() {
    return Stream.of(
      code(
        "{{- /* returns \"foo\" */ -}}",
        "{{- define \"foo\" }}",
        "{{ split \"0\" \"00foo\" }}",
        "{{ end }}",
        "{{ template \"foo\" }}"),
      code(
        "apiVersion: v1",
        "kind: ConfigMap",
        "metadata:",
        "{{- if .Values.annotations }}",
        "annotations:",
        "{{- range $key, $value := .Values.annotations }}",
        "{{ $key }}: {{ $value }}",
        "{{- end }}",
        "{{- end }}"),
      code(
        "{{- if .Values.annotations }}",
        "{{- range $key, $value := .Values.annotations }}",
        "{{- if eq $key \"foo\" }} {{- break -}} {{else}} {{continue}} {{- end }}",
        "{{ $key }}: {{ $value }}",
        "{{- end }}",
        "{{- else if false }}",
        "{{- printf nil }}",
        "{{- printf 2.0 }}",
        "{{- else }}",
        "{{ print .Values.fallbacks.foo }}",
        "{{- end }}"),
      code(
        "{{- with .Values.annotations }}",
        "{{- range $key, $value := . }}",
        "{{ $key }}: {{ $value }}",
        "{{- end }}",
        "{{- end }}"),
      code(
        "{{- len ( print .Values.foo) }}"));
  }
}
