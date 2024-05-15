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
package org.sonar.iac.springconfig.parser.yaml;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.snakeyaml.engine.v2.exceptions.ParserException;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.testing.TextRangeAssert;
import org.sonar.iac.springconfig.tree.api.File;
import org.sonar.iac.springconfig.tree.api.Profile;
import org.sonar.iac.springconfig.tree.api.Tuple;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpringConfigYamlParserTest {
  private InputFileContext inputFileContext;
  private final InputFile inputFile = mock(InputFile.class);

  private final SpringConfigYamlParser parser = new SpringConfigYamlParser();

  @BeforeEach
  void setup() {
    inputFileContext = new InputFileContext(mock(SensorContext.class), inputFile);
    when(inputFile.filename()).thenReturn("foo.yaml");
  }

  @Test
  void shouldParseEmptyFile() {
    assertThatThrownBy(() -> parser.parse("", inputFileContext))
      .isInstanceOf(ParseException.class)
      .hasMessage("Unexpected empty nodes list while converting file");
  }

  @Test
  void shouldParseWithoutContext() {
    assertThatNoException().isThrownBy(() -> parser.parse("# comment", null));
  }

  @Test
  void shouldFailOnRecursion() {
    assertThatThrownBy(() -> parser.parse("some_key: &some_anchor\n  sub_key: *some_anchor", inputFileContext))
      .isInstanceOf(ParserException.class)
      .hasMessage("Recursive node found\n" +
        " in reader, line 1, column 11:\n" +
        "    some_key: &some_anchor\n" +
        "              ^\n");
  }

  @Test
  void shouldParseSimpleValue() {
    String source = """
      a""";
    File parse = parser.parse(source, inputFileContext);

    List<Profile> profiles = parse.profiles();
    assertThat(profiles).hasSize(1);

    Profile defaultProfile = profiles.get(0);

    assertThat(defaultProfile.properties()).hasSize(1);

    Tuple tuple = defaultProfile.properties().get(0);
    assertThat(tuple.key().value().value()).isEmpty();
    assertThat(tuple.value().value().value()).isEqualTo("a");

    TextRange range1 = TextRanges.range(1, 0, 1, 1);
    TextRangeAssert.assertThat(tuple.textRange()).isEqualTo(range1);
    TextRangeAssert.assertThat(tuple.key().textRange()).isEqualTo(range1);
    TextRangeAssert.assertThat(tuple.key().value().textRange()).isEqualTo(range1);
    TextRangeAssert.assertThat(tuple.value().textRange()).isEqualTo(range1);
    TextRangeAssert.assertThat(tuple.value().value().textRange()).isEqualTo(range1);
  }

  @Test
  void shouldParseSimpleKeyValue() {
    String source = """
      a: b""";
    File parse = parser.parse(source, inputFileContext);

    List<Profile> profiles = parse.profiles();
    assertThat(profiles).hasSize(1);

    Profile defaultProfile = profiles.get(0);

    assertThat(defaultProfile.properties()).hasSize(1);

    Tuple tuple = defaultProfile.properties().get(0);
    assertThat(tuple.key().value().value()).isEqualTo("a");
    assertThat(tuple.value().value().value()).isEqualTo("b");

    TextRange range1 = TextRanges.range(1, 0, 1, 4);
    TextRange range2 = TextRanges.range(1, 0, 1, 1);
    TextRange range3 = TextRanges.range(1, 3, 1, 4);
    TextRangeAssert.assertThat(tuple.textRange()).isEqualTo(range1);
    TextRangeAssert.assertThat(tuple.key().textRange()).isEqualTo(range2);
    TextRangeAssert.assertThat(tuple.key().value().textRange()).isEqualTo(range2);
    TextRangeAssert.assertThat(tuple.value().textRange()).isEqualTo(range3);
    TextRangeAssert.assertThat(tuple.value().value().textRange()).isEqualTo(range3);
  }

  @Test
  void shouldParseSequences() {
    String source = """
      map:
        array:
         - keyA1: valueA1
           keyB1:
             - valueB1[0]
         - keyA2:
             innerKeyA2: valueA2
         - value[2]
      """;
    File parse = parser.parse(source, inputFileContext);

    List<Profile> profiles = parse.profiles();
    assertThat(profiles).hasSize(1);

    Profile defaultProfile = profiles.get(0);

    assertThat(defaultProfile.properties()).hasSize(4);

    Tuple firstTuple = defaultProfile.properties().get(0);
    assertThat(firstTuple.key().value().value()).isEqualTo("map.array[0].keyA1");
    assertThat(firstTuple.value().value().value()).isEqualTo("valueA1");
    TextRangeAssert.assertThat(firstTuple.textRange()).hasRange(3, 5, 3, 19);
    TextRangeAssert.assertThat(firstTuple.key().textRange()).hasRange(3, 5, 3, 10);
    TextRangeAssert.assertThat(firstTuple.value().textRange()).hasRange(3, 12, 3, 19);

    Tuple secondTuple = defaultProfile.properties().get(1);
    assertThat(secondTuple.key().value().value()).isEqualTo("map.array[0].keyB1[0]");
    assertThat(secondTuple.value().value().value()).isEqualTo("valueB1[0]");
    TextRange secondTupleRange = TextRanges.range(5, 9, 5, 19);
    TextRangeAssert.assertThat(secondTuple.textRange()).isEqualTo(secondTupleRange);
    TextRangeAssert.assertThat(secondTuple.key().textRange()).isEqualTo(secondTupleRange);
    TextRangeAssert.assertThat(secondTuple.value().textRange()).isEqualTo(secondTupleRange);

    Tuple thirdTuple = defaultProfile.properties().get(2);
    assertThat(thirdTuple.key().value().value()).isEqualTo("map.array[1].keyA2.innerKeyA2");
    assertThat(thirdTuple.value().value().value()).isEqualTo("valueA2");
    TextRangeAssert.assertThat(thirdTuple.textRange()).hasRange(7, 7, 7, 26);
    TextRangeAssert.assertThat(thirdTuple.key().textRange()).hasRange(7, 7, 7, 17);
    TextRangeAssert.assertThat(thirdTuple.value().textRange()).hasRange(7, 19, 7, 26);

    Tuple fourthTuple = defaultProfile.properties().get(3);
    assertThat(fourthTuple.key().value().value()).isEqualTo("map.array[2]");
    assertThat(fourthTuple.value().value().value()).isEqualTo("value[2]");
    TextRange fourthTupleRange = TextRanges.range(8, 5, 8, 13);
    TextRangeAssert.assertThat(fourthTuple.textRange()).isEqualTo(fourthTupleRange);
    TextRangeAssert.assertThat(fourthTuple.key().textRange()).isEqualTo(fourthTupleRange);
    TextRangeAssert.assertThat(fourthTuple.value().textRange()).isEqualTo(fourthTupleRange);
  }

  @Test
  void shouldParseSequenceInSequence() {
    String source = """
      array:
        - - value1
          - value2
        - - value3
          - value4
      """;
    File parse = parser.parse(source, inputFileContext);

    List<Profile> profiles = parse.profiles();
    assertThat(profiles).hasSize(1);

    Profile defaultProfile = profiles.get(0);

    assertThat(defaultProfile.properties()).hasSize(4);

    Tuple firstTuple = defaultProfile.properties().get(0);
    assertThat(firstTuple.key().value().value()).isEqualTo("array[0][0]");
    assertThat(firstTuple.value().value().value()).isEqualTo("value1");
    TextRangeAssert.assertThat(firstTuple.textRange()).hasRange(2, 6, 2, 12);

    Tuple secondTuple = defaultProfile.properties().get(1);
    assertThat(secondTuple.key().value().value()).isEqualTo("array[0][1]");
    assertThat(secondTuple.value().value().value()).isEqualTo("value2");
    TextRangeAssert.assertThat(secondTuple.textRange()).hasRange(3, 6, 3, 12);

    Tuple thirdTuple = defaultProfile.properties().get(2);
    assertThat(thirdTuple.key().value().value()).isEqualTo("array[1][0]");
    assertThat(thirdTuple.value().value().value()).isEqualTo("value3");
    TextRangeAssert.assertThat(thirdTuple.textRange()).hasRange(4, 6, 4, 12);

    Tuple fourthTuple = defaultProfile.properties().get(3);
    assertThat(fourthTuple.key().value().value()).isEqualTo("array[1][1]");
    assertThat(fourthTuple.value().value().value()).isEqualTo("value4");
    TextRangeAssert.assertThat(fourthTuple.textRange()).hasRange(5, 6, 5, 12);
  }

  @Test
  void shouldParseComments() {
    String source = """
      #comment1
      server:
        address: 192.168.1.100
      array: #comment2
        - keyA1:
            innerKey: valueA1 #comment3
        - example2.org
      ---
      server:
      #comment4
        address: 127.0.0.1
      """;
    File parse = parser.parse(source, inputFileContext);

    List<Profile> profiles = parse.profiles();
    assertThat(profiles).hasSize(2);

    List<Comment> firstProfilesComments = profiles.get(0).comments();
    assertThat(firstProfilesComments).hasSize(3);

    Comment comment1 = firstProfilesComments.get(0);
    assertThat(comment1.contentText()).isEqualTo("comment1");
    TextRangeAssert.assertThat(comment1.textRange()).hasRange(1, 0, 1, 9);

    Comment comment2 = firstProfilesComments.get(1);
    assertThat(comment2.contentText()).isEqualTo("comment2");
    TextRangeAssert.assertThat(comment2.textRange()).hasRange(4, 7, 4, 16);

    Comment comment3 = firstProfilesComments.get(2);
    assertThat(comment3.contentText()).isEqualTo("comment3");
    TextRangeAssert.assertThat(comment3.textRange()).hasRange(6, 24, 6, 33);

    List<Comment> secondProfilesComments = profiles.get(1).comments();
    assertThat(secondProfilesComments).hasSize(1);

    Comment comment4 = secondProfilesComments.get(0);
    assertThat(comment4.contentText()).isEqualTo("comment4");
    TextRangeAssert.assertThat(comment4.textRange()).hasRange(10, 0, 10, 9);
  }

  @Test
  void shouldParseIntoMultipleProfiles() {
    String source = """
      a: b
      ---
      a: b
      ---
      a: b""";
    File file = parser.parse(source, inputFileContext);

    List<Profile> profiles = file.profiles();
    assertThat(profiles).hasSize(3);

    for (Profile profile : profiles) {
      assertThat(profile.properties()).hasSize(1);
      Tuple tuple = profile.properties().get(0);
      assertThat(tuple.key().value().value()).isEqualTo("a");
      assertThat(tuple.value().value().value()).isEqualTo("b");
    }

    TextRangeAssert.assertThat(file.textRange()).hasRange(1, 0, 5, 4);
    TextRangeAssert.assertThat(file.profiles().get(0).textRange()).hasRange(1, 0, 1, 4);
    TextRangeAssert.assertThat(file.profiles().get(1).textRange()).hasRange(3, 0, 3, 4);
    TextRangeAssert.assertThat(file.profiles().get(2).textRange()).hasRange(5, 0, 5, 4);
  }

  static Stream<Arguments> shouldParseProfileName() {
    return Stream.of(
      Arguments.of("""
        a: b""", "default"),
      Arguments.of("""
        spring:
          profiles:
            active: profileName""", "profileName"),
      Arguments.of("""
        spring:
          config:
            activate:
             on-profile: profileName""", "profileName"),
      Arguments.of("""
        spring:
          config:
            activate:
             on-profile: profileName1
          profiles:
            active: profileName2""", "profileName2"),
      Arguments.of("""
        spring:
          profiles:
            active: profileName1
          config:
            activate:
             on-profile: profileName2""", "profileName2"),
      Arguments.of("""
        spring:
          profiles:
            active: production & test""", "production & test"),
      Arguments.of("""
        spring:
          profiles:
            active: profileName1
            active: profileName2""", "profileName2"),
      Arguments.of("""
        spring:
          profiles:
            default: profileName1""", "profileName1"),
      Arguments.of("""
        spring:
          profiles:
            default: profileName1
            active: profileName2""", "profileName2"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"#---", "# ---", "#--", "#----"})
  void shouldReadOneProfile(String notProfileSeparator) {
    var code = """
      # comment 1
      a: b
      %s
      # comment 2
      a: b""".formatted(notProfileSeparator);

    var file = parser.parse(code, inputFileContext);

    assertThat(file.profiles()).hasSize(1);
  }

  @MethodSource("shouldParseProfileName")
  @ParameterizedTest
  void shouldParseProfileNameInFirstProfile(String profileDefinition, String expectedProfileName) {
    File file = parser.parse(profileDefinition, inputFileContext);

    List<Profile> profiles = file.profiles();
    assertThat(profiles).hasSize(1);

    Profile profile = file.profiles().get(0);
    assertThat(profile.name()).isEqualTo(expectedProfileName);
  }

  @MethodSource("shouldParseProfileName")
  @ParameterizedTest
  void shouldParseProfileNameInSecondProfile(String profileDefinition, String expectedProfileName) {
    String source = """
      a: b
      ---
      %s""".formatted(profileDefinition);
    File file = parser.parse(source, inputFileContext);

    List<Profile> profiles = file.profiles();
    assertThat(profiles).hasSize(2);

    Profile profile = file.profiles().get(1);
    assertThat(profile.name()).isEqualTo(expectedProfileName);
  }

  @Test
  void shouldAdjustMavenSubstitutions() {
    var code = "foo: @maven.property@";

    var file = (File) parser.parse(code, inputFileContext);

    var scalar = file.profiles().get(0).properties().get(0).value();
    assertThat(scalar.value().value()).isEqualTo("maven.property");
    TextRangeAssert.assertThat(scalar.textRange()).hasRange(1, 5, 1, 21);
  }
}
