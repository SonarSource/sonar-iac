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
package org.sonar.iac.springconfig.parser.properties;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.springconfig.tree.api.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.sonar.iac.springconfig.SpringConfigAssertions.assertThat;
import static org.sonar.iac.springconfig.parser.properties.PropertiesTestUtils.createPropertiesFileContext;

class PropertiesParseTreeVisitorTest {

  @Test
  void shouldReadSimpleProperty() {
    var code = "database=h2";

    var file = parseProperties(code);

    var databaseKeyValue = file.profiles().get(0).properties().get(0);
    assertThat(databaseKeyValue.key().value().value()).isEqualTo("database");
    assertThat(databaseKeyValue.key().textRange()).hasRange(1, 0, 1, 7);
    assertThat(databaseKeyValue.key().value().textRange()).hasRange(1, 0, 1, 7);

    assertThat(databaseKeyValue.value().value().value()).isEqualTo("h2");
    assertThat(databaseKeyValue.value().textRange()).hasRange(1, 9, 1, 10);
    assertThat(databaseKeyValue.value().value().textRange()).hasRange(1, 9, 1, 10);
  }

  @Test
  void shouldReadSimplePropertyWithColonAsDelimiter() {
    var code = "database : h2";

    var file = parseProperties(code);

    var databaseKeyValue = file.profiles().get(0).properties().get(0);
    assertThat(databaseKeyValue.key().value().value()).isEqualTo("database");
    assertThat(databaseKeyValue.key().textRange()).hasRange(1, 0, 1, 7);
    assertThat(databaseKeyValue.key().value().textRange()).hasRange(1, 0, 1, 7);

    assertThat(databaseKeyValue.value().value().value()).isEqualTo("h2");
    assertThat(databaseKeyValue.value().textRange()).hasRange(1, 11, 1, 12);
    assertThat(databaseKeyValue.value().value().textRange()).hasRange(1, 11, 1, 12);
  }

  @Test
  void shouldReadPropertyWithoutValue() {
    var code = "foo";

    var file = parseProperties(code);

    var databaseKeyValue = file.profiles().get(0).properties().get(0);
    assertThat(databaseKeyValue.key().value().value()).isEqualTo("foo");
    assertThat(databaseKeyValue.key().textRange()).hasRange(1, 0, 1, 2);
    assertThat(databaseKeyValue.key().value().textRange()).hasRange(1, 0, 1, 2);

    assertThat(databaseKeyValue.value()).isNull();
  }

  @Test
  void shouldReadSimpleProperties() {
    var code = """
      database=h2
      spring.sql.init.schema-locations  classpath*:db/${database}/schema.sql""";

    var file = parseProperties(code);

    var databaseTuple = file.profiles().get(0).properties().get(0);
    assertThat(databaseTuple.key().value().value()).isEqualTo("database");
    assertThat(databaseTuple.key().textRange()).hasRange(1, 0, 1, 7);
    assertThat(databaseTuple.key().value().textRange()).hasRange(1, 0, 1, 7);

    assertThat(databaseTuple.value().value().value()).isEqualTo("h2");
    assertThat(databaseTuple.value().textRange()).hasRange(1, 9, 1, 10);
    assertThat(databaseTuple.value().value().textRange()).hasRange(1, 9, 1, 10);

    var schemaLocationTuple = file.profiles().get(0).properties().get(1);
    assertThat(schemaLocationTuple.key().value().value()).isEqualTo("spring.sql.init.schema-locations");
    assertThat(schemaLocationTuple.key().textRange()).hasRange(2, 0, 2, 31);
    assertThat(schemaLocationTuple.key().value().textRange()).hasRange(2, 0, 2, 31);

    assertThat(schemaLocationTuple.value().value().value()).isEqualTo("classpath*:db/${database}/schema.sql");
    assertThat(schemaLocationTuple.value().textRange()).hasRange(2, 34, 2, 69);
    assertThat(schemaLocationTuple.value().value().textRange()).hasRange(2, 34, 2, 69);
  }

  @Test
  void shouldReadSimpleComment() {
    var code = "# database init";

    var file = parseProperties(code);

    var comment = file.profiles().get(0).comments().get(0);
    assertThat(comment.value()).isEqualTo("# database init");
    assertThat(comment.contentText()).isEqualTo(" database init");
    assertThat(comment.textRange()).hasRange(1, 0, 1, 14);
  }

  @Test
  void shouldReadCommentsAndTuples() {
    var code = """
      # comment 1
      foo=bar
      ! comment 2
      bar\fbaz
      ! comment 3""";

    var file = parseProperties(code);

    var comment1 = file.profiles().get(0).comments().get(0);
    assertThat(comment1.value()).isEqualTo("# comment 1");
    assertThat(comment1.contentText()).isEqualTo(" comment 1");
    assertThat(comment1.textRange()).hasRange(1, 0, 1, 10);

    var comment2 = file.profiles().get(0).comments().get(1);
    assertThat(comment2.value()).isEqualTo("! comment 2");
    assertThat(comment2.contentText()).isEqualTo(" comment 2");
    assertThat(comment2.textRange()).hasRange(3, 0, 3, 10);

    var comment3 = file.profiles().get(0).comments().get(2);
    assertThat(comment3.value()).isEqualTo("! comment 3");
    assertThat(comment3.contentText()).isEqualTo(" comment 3");
    assertThat(comment3.textRange()).hasRange(5, 0, 5, 10);

    var tuple1 = file.profiles().get(0).properties().get(0);
    assertThat(tuple1.key().value().value()).isEqualTo("foo");
    assertThat(tuple1.key().textRange()).hasRange(2, 0, 2, 2);
    assertThat(tuple1.value().value().value()).isEqualTo("bar");
    assertThat(tuple1.value().textRange()).hasRange(2, 4, 2, 6);

    var tuple2 = file.profiles().get(0).properties().get(1);
    assertThat(tuple2.key().value().value()).isEqualTo("bar");
    assertThat(tuple2.key().textRange()).hasRange(4, 0, 4, 2);
    assertThat(tuple2.value().value().value()).isEqualTo("baz");
    assertThat(tuple2.value().textRange()).hasRange(4, 4, 4, 6);
  }

  @Test
  void shouldReadKeyWithArray() {
    var code = """
      my.servers[0]=dev.example.com
      my.servers[1]=another.example.com""";

    var file = parseProperties(code);

    var tuple1 = file.profiles().get(0).properties().get(0);
    assertThat(tuple1.key().value().value()).isEqualTo("my.servers[0]");
    assertThat(tuple1.key().textRange()).hasRange(1, 0, 1, 12);
    assertThat(tuple1.value().value().value()).isEqualTo("dev.example.com");
    assertThat(tuple1.value().textRange()).hasRange(1, 14, 1, 28);

    var tuple2 = file.profiles().get(0).properties().get(1);
    assertThat(tuple2.key().value().value()).isEqualTo("my.servers[1]");
    assertThat(tuple2.key().textRange()).hasRange(2, 0, 2, 12);
    assertThat(tuple2.value().value().value()).isEqualTo("another.example.com");
    assertThat(tuple2.value().textRange()).hasRange(2, 14, 2, 32);
  }

  @ParameterizedTest
  @ValueSource(strings = {"#---", "!---"})
  void shouldReadTwoProfiles(String profileSeparator) {
    var code = """
      # comment 1
      foo = bar
      %s
      ! comment 2
      foo = baz""".formatted(profileSeparator);

    var file = parseProperties(code);

    var profile1 = file.profiles().get(0);
    var comment1 = profile1.comments().get(0);
    assertThat(comment1.value()).isEqualTo("# comment 1");
    assertThat(comment1.contentText()).isEqualTo(" comment 1");
    assertThat(comment1.textRange()).hasRange(1, 0, 1, 10);

    var tuple1 = profile1.properties().get(0);
    assertThat(tuple1.key().value().value()).isEqualTo("foo");
    assertThat(tuple1.key().textRange()).hasRange(2, 0, 2, 2);
    assertThat(tuple1.value().value().value()).isEqualTo("bar");
    assertThat(tuple1.value().textRange()).hasRange(2, 6, 2, 8);

    assertThat(profile1.comments()).hasSize(1);
    assertThat(profile1.properties()).hasSize(1);

    var profile2 = file.profiles().get(1);
    var comment2 = profile2.comments().get(0);
    assertThat(comment2.value()).isEqualTo(profileSeparator);
    assertThat(comment2.contentText()).isEqualTo("---");
    assertThat(comment2.textRange()).hasRange(3, 0, 3, 3);

    var comment3 = profile2.comments().get(1);
    assertThat(comment3.value()).isEqualTo("! comment 2");
    assertThat(comment3.contentText()).isEqualTo(" comment 2");
    assertThat(comment3.textRange()).hasRange(4, 0, 4, 10);

    var tuple2 = profile1.properties().get(0);
    assertThat(tuple2.key().value().value()).isEqualTo("foo");
    assertThat(tuple2.key().textRange()).hasRange(2, 0, 2, 2);
    assertThat(tuple2.value().value().value()).isEqualTo("bar");
    assertThat(tuple2.value().textRange()).hasRange(2, 6, 2, 8);

    assertThat(profile2.comments()).hasSize(2);
    assertThat(profile2.properties()).hasSize(1);
    assertThat(file.profiles()).hasSize(2);
  }

  @ParameterizedTest
  @ValueSource(strings = {"#----", "!----", "# ---", "! ---", "#--", "!--", "#+++", "!+++"})
  void shouldReadOneProfile(String notProfileSeparator) {
    var code = """
      # comment 1
      foo = bar
      %s
      ! comment 2
      foo = baz""".formatted(notProfileSeparator);

    var file = parseProperties(code);

    assertThat(file.profiles()).hasSize(1);
  }

  static Stream<Arguments> shouldReadProfileName() {
    return Stream.of(
      arguments("spring.profiles.active=profile1", "profile1"),
      arguments("spring.config.activate.on-profile=dev & qa", "dev & qa"),
      arguments("spring.profiles.active=profile1\nspring.config.activate.on-profile=profile2", "profile1 profile2"),
      arguments("#comment", ""));
  }

  @ParameterizedTest
  @MethodSource
  void shouldReadProfileName(String properties, String expectedProfileName) {
    var code = """
      %s
      foo=bar""".formatted(properties);

    var file = parseProperties(code);

    assertThat(file.profiles().get(0).name()).isEqualTo(expectedProfileName);
  }

  @ParameterizedTest
  @MethodSource("shouldReadProfileName")
  void shouldReadProfileNameInSecondProfile(String properties, String expectedProfileName) {
    var code = """
      database=h2
      #---
      %s
      foo=bar""".formatted(properties);

    var file = parseProperties(code);

    assertThat(file.profiles().get(0).name()).isEmpty();
    assertThat(file.profiles().get(1).name()).isEqualTo(expectedProfileName);
  }

  private File parseProperties(String code) {
    var propertiesFileContext = createPropertiesFileContext(code);
    PropertiesParseTreeVisitor visitor = new PropertiesParseTreeVisitor();
    return (File) visitor.visitPropertiesFile(propertiesFileContext);
  }
}
