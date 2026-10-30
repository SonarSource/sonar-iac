/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.yaml;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.snakeyaml.engine.v2.exceptions.ParserException;
import org.snakeyaml.engine.v2.exceptions.ScannerException;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.languages.IacLanguage;
import org.sonar.iac.common.testing.IacTestUtils;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class YamlParserTest {

  private InputFileContext inputFileContext;
  private final InputFile inputFile = mock(InputFile.class);

  private final YamlParser parser = new YamlParser();

  @BeforeEach
  void setup() {
    inputFileContext = new InputFileContext(mock(SensorContext.class), inputFile, IacLanguage.YAML);
    when(inputFile.filename()).thenReturn("foo.yaml");
  }

  @Test
  void parse_empty_file() {
    Exception exception = assertThrows(ParseException.class, () -> parser.parse("", inputFileContext));
    assertThat(exception.getMessage()).isEqualTo("Unexpected empty nodes list while converting file");
  }

  @Test
  void parse_json_file() {
    when(inputFile.filename()).thenReturn("foo.json");
    assertThatNoException().isThrownBy(() -> parser.parse("foo: {bar: 1234}", inputFileContext));
  }

  @Test
  void parse_without_context() {
    assertThatNoException().isThrownBy(() -> parser.parse("# comment", null));
  }

  @Test
  void parse_recursion() {
    Exception exception = assertThrows(ParserException.class, () -> parser.parse("some_key: &some_anchor\n  sub_key: *some_anchor", inputFileContext));
    assertThat(exception.getMessage()).isEqualTo("""
      Recursive node found
       in reader, line 1, column 11:
          some_key: &some_anchor
                    ^
      """);
  }

  @ParameterizedTest
  @ValueSource(
    // language=yaml
    strings = {
      "--- ###",
      "--- foo ###",
      """
        foo:
          bar: baz
        # block comment before
        ---
        # block comment after
        foo: bar
        --- !foo bar
        --- >
          foo
        --- |
          foo
        """,
      """
        minimal:
          example: |
        ---
        another: key
        """
    })
  void testSnakeyamlSucceeding(String validYaml) {
    // language=yaml
    assertThatNoException().isThrownBy(() -> parser.parse(validYaml, inputFileContext));
  }

  @ParameterizedTest
  @ValueSource(
    // language=yaml
    strings = {
      "--- foo: bar ###",
    })
  void testSnakeyamlFailing(String input) {
    assertThatThrownBy(() -> parser.parse(input, inputFileContext))
      .isOfAnyClassIn(ParserException.class, ScannerException.class, ClassCastException.class, IndexOutOfBoundsException.class);
  }

  /**
   * TAB is still rejected when it is used as block <b>indentation</b> in YAML files, even with snakeyaml-engine 3.2.
   * This is correct: YAML 1.2 (chapter 6.1) forbids tabs in indentation, so the YAML fixtures are invalid YAML and the
   * reference parsers (libyaml, PyYAML, go-yaml) reject them as well.
   * The JSON fixtures are flow collections, where indentation is not significant and the leading tabs are only
   * separation whitespace between tokens, which is allowed (see https://codeberg.org/snakeyaml/snakeyaml-engine/issues/99).
   */
  @ParameterizedTest
  @CsvSource(
    value = {
      "single-tab-indentation-without-nesting.json, true",
      "single-tab-indentation.json, true",
      "single-tab-indentation.yaml, false",
      "double-tab-indentation.yaml, false",
      "double-tab-indentation.json, true"
    })
  void snakeYamlParsingFromResourceFileShouldHaveExpectedBehavior(String filename, boolean shouldParse) throws IOException {
    InputFile customInputFile = IacTestUtils.inputFile("parser/" + filename, "");
    if (shouldParse) {
      assertThatNoException().isThrownBy(() -> parser.parse(customInputFile.contents(), inputFileContext));
    } else {
      var content = customInputFile.contents();
      assertThatThrownBy(() -> parser.parse(content, inputFileContext))
        .isOfAnyClassIn(ParserException.class, ScannerException.class, ClassCastException.class);
    }
  }

  /**
   * TAB used as separation whitespace (not as indentation) is valid YAML 1.2.
   * See https://codeberg.org/snakeyaml/snakeyaml-engine/issues/99
   */
  @ParameterizedTest
  @ValueSource(
    // language=yaml
    strings = {
      "a:\tvalue",
      "a:\t\tvalue",
      "a: value\t# comment",
      "a:\tvalue\nb:\t1\n",
      "a:\n  b:\tvalue\n  c:\tother\n",
      "- a:\tvalue\n  b:\tother\n",
      "-\tvalue",
      "- \tvalue",
      "- a\n-\tb\n",
      "? \ta\n: \tb\n",
      "---\tfoo",
      "a:\t{b: 1}",
      "a:\t[1]",
      "a:\t\"quoted\"",
      "a:\t'quoted'",
      "a:\t|\n  text\n",
      "a:\t>\n  text\n",
      "a:\t&anchor 1\nb:\t*anchor\n",
      "a:\t!!str 1",
      "a: {b:\t1,\tc:\t2}",
      "a: [1,\t2,\t3]",
      "a: {\tb: 1\t}",
      "a: [\t1\t]",
      "{\n\t\"key\":\t\"value\"\n}",
    })
  void shouldParseYamlWithTabAsSeparationWhitespace(String yaml) {
    assertThatNoException().isThrownBy(() -> parser.parse(yaml, inputFileContext));
  }

  /**
   * JSON (RFC 7159, section 2) allows TAB as whitespace anywhere between tokens.
   * See https://codeberg.org/snakeyaml/snakeyaml-engine/issues/99
   */
  @ParameterizedTest
  @ValueSource(
    // language=json
    strings = {
      "{\"key\":\t\"value\"}",
      "{\t\"key\"\t:\t\"value\"\t}",
      "{\"key\": \"value\",\t\"other\": 1}",
      "{\"key\": [1,\t2,\t3]}",
      "[\t1,\t2\t]",
      "{\n\t\"key\": \"value\"\n}",
      "{\n\t\"key\": \"value\",\n\t\"nested\": {\n\t\t\"key\": \"value\"\n\t}\n}",
      "{\n\t\"key\": [\n\t\t1,\n\t\t2\n\t]\n}",
      "{\n\t\"key\":\t{\n\t\t\"nested\":\t\"value\"\n\t},\n\t\"other\":\t1\n}",
    })
  void shouldParseJsonWithTabAsWhitespace(String json) {
    when(inputFile.filename()).thenReturn("foo.json");
    assertThatNoException().isThrownBy(() -> parser.parse(json, inputFileContext));
  }

  /**
   * Known limitation of snakeyaml-engine: a TAB before the top-level JSON value is allowed by RFC 7159, but snakeyaml-engine
   * scans it in block context and treats it as indentation.
   */
  @ParameterizedTest
  @ValueSource(
    // language=json
    strings = {
      "\t{\"key\": \"value\"}",
      "\t[1, 2]",
      "\t\"value\"",
    })
  void shouldFailToParseJsonWithTabBeforeTopLevelValue(String json) {
    when(inputFile.filename()).thenReturn("foo.json");
    assertThatThrownBy(() -> parser.parse(json, inputFileContext))
      .isInstanceOf(ScannerException.class);
  }

  @Test
  void shouldFailToParseYamlWithTabAsIndentation() {
    assertThatThrownBy(() -> parser.parse("a:\n\tb: 1\n", inputFileContext))
      .isInstanceOf(ScannerException.class)
      .hasMessageContaining("Do not use \\t(TAB) for indentation");
  }

  @Test
  void shouldNotIncludeTabSeparatorInParsedValues() {
    var fileTree = parser.parse("a:\tvalue\t# comment\nb: {c:\t1}", inputFileContext);

    var mapping = (MappingTree) fileTree.documents().get(0);
    assertThat(mapping.elements()).hasSize(2);
    assertThat(((ScalarTree) mapping.elements().get(0).key()).value()).isEqualTo("a");
    assertThat(((ScalarTree) mapping.elements().get(0).value()).value()).isEqualTo("value");
    var flowMapping = (MappingTree) mapping.elements().get(1).value();
    assertThat(((ScalarTree) flowMapping.elements().get(0).key()).value()).isEqualTo("c");
    assertThat(((ScalarTree) flowMapping.elements().get(0).value()).value()).isEqualTo("1");
  }

  /**
   * An empty node with a tag followed by an empty line.
   * See https://codeberg.org/snakeyaml/snakeyaml-engine/issues/90
   */
  @ParameterizedTest
  @ValueSource(
    // language=yaml
    strings = {
      "a: !!null\n\nb: 1\n",
      "a: !!null\r\n\r\nb: 1\r\n",
      "a: !!str\n\nb: 1\n",
      "a:\n  b: !!null\n\n  c: 1\n",
    })
  void shouldParseEmptyTaggedNodeFollowedByEmptyLine(String yaml) {
    assertThatNoException().isThrownBy(() -> parser.parse(yaml, inputFileContext));
  }

  /**
   * A block scalar followed by comment lines separated by an empty line.
   * See https://codeberg.org/snakeyaml/snakeyaml-engine/issues/92
   */
  @ParameterizedTest
  @ValueSource(
    // language=yaml
    strings = {
      """
        cm:
          foo: |
            x
          # c1

          # c2
          bar: 1
        """,
      """
        cm:
          foo: >
            x
          # c1

          # c2
          bar: 1
        """,
      """
        cm:
          foo: |-
            x
          # c1

          # c2
          bar: 1
        """,
      """
        cm:
          foo: |+
            x
          # c1

          # c2
          bar: 1
        """,
      """
        cm:
          - foo: |
              x
            # c1

            # c2
            bar: 1
        """,
      """
        cm:
          foo: |
            x
          # c1

          # c2
        """,
      """
        cm:
          foo: |
            x
          # c1

          # c2
        top: 1
        """,
      "cm:\r\n  foo: |\r\n    x\r\n  # c1\r\n\r\n  # c2\r\n  bar: 1\r\n",
      // Reproducer from SONARIAC-2043
      """
        container:
          name: my-container
        ingress:
          className: "nginx"
        configs:
          cm:
            foo: |
              foo
                - aa
                - bb

            # comment 1

            # comment 2
            bar: |
              ### bar bar
              - abc
                - ''
                - bar
        """,
    })
  void shouldParseBlockScalarFollowedByCommentsSeparatedByEmptyLine(String yaml) {
    assertThatNoException().isThrownBy(() -> parser.parse(yaml, inputFileContext));
  }

  /**
   * An in-line comment on a document start or end marker line, when no other document follows.
   * See https://codeberg.org/snakeyaml/snakeyaml-engine/issues/95
   */
  @ParameterizedTest
  @ValueSource(
    // language=yaml
    strings = {
      "--- foo ###\n",
      "--- foo ###\r\n",
      "--- \"foo\" ###\n",
      "--- 'foo' ###\n",
      "--- 123 ###\n",
      "--- !!str foo ###\n",
      "--- &a foo ###\n",
      "foo: 1\n... ###\n",
      "--- foo ###\n--- bar ###\n",
    })
  void shouldParseInlineCommentOnDocumentMarkerLine(String yaml) {
    assertThatNoException().isThrownBy(() -> parser.parse(yaml, inputFileContext));
  }

  /**
   * An in-line comment on a block scalar indicator line of a sequence entry.
   * See https://codeberg.org/snakeyaml/snakeyaml-engine/issues/97
   */
  @ParameterizedTest
  @ValueSource(
    // language=yaml
    strings = {
      "- > # c\n  text\n",
      "- | # c\n  text\n",
      "- >- # c\n  text\n",
      "- |- # c\n  text\n",
      "- |+ # c\n  text\n",
      "- |2 # c\n  text\n",
      "- > # c\n",
      "a:\n  b:\n    - > # c\n      text\n",
      "-\n  - > # c\n    text\n",
      "- > # c\r\n  text\r\n",
      // Reproducer from SONARIAC-1901
      """
        - name: Test json_patch
          assert:
            that:
              - > # Insert a new element into an array at a specified index
                list_input |
                  community.general.json_patch("add", "/1", {"baz": "qux"})
                ==
                [{"foo": {"one": 1}}]
        """,
    })
  void shouldParseCommentOnBlockScalarIndicatorInSequenceEntry(String yaml) {
    assertThatNoException().isThrownBy(() -> parser.parse(yaml, inputFileContext));
  }
}
