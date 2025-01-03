/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.jvmframeworkconfig.parser.properties;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.jvmframeworkconfig.tree.api.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.sonar.iac.jvmframeworkconfig.JvmFrameworkConfigAssertions.assertThat;
import static org.sonar.iac.jvmframeworkconfig.parser.properties.PropertiesTestUtils.createPropertiesFileContext;

class PropertiesParseTreeVisitorTest {

  @Test
  void shouldReadSimpleProperty() {
    var code = "database=h2";

    var file = parseProperties(code);

    var databaseKeyValue = file.profiles().get(0).properties().get(0);
    assertThat(databaseKeyValue.key().value().value()).isEqualTo("database");
    assertThat(databaseKeyValue.key().textRange()).hasRange(1, 0, 1, 8);
    assertThat(databaseKeyValue.key().value().textRange()).hasRange(1, 0, 1, 8);

    assertThat(databaseKeyValue.value().value().value()).isEqualTo("h2");
    assertThat(databaseKeyValue.value().textRange()).hasRange(1, 9, 1, 11);
    assertThat(databaseKeyValue.value().value().textRange()).hasRange(1, 9, 1, 11);
  }

  @Test
  void shouldReadSimplePropertyWithColonAsDelimiter() {
    var code = "database : h2";

    var file = parseProperties(code);

    var databaseKeyValue = file.profiles().get(0).properties().get(0);
    assertThat(databaseKeyValue.key().value().value()).isEqualTo("database");
    assertThat(databaseKeyValue.key().textRange()).hasRange(1, 0, 1, 8);
    assertThat(databaseKeyValue.key().value().textRange()).hasRange(1, 0, 1, 8);

    assertThat(databaseKeyValue.value().value().value()).isEqualTo("h2");
    assertThat(databaseKeyValue.value().textRange()).hasRange(1, 11, 1, 13);
    assertThat(databaseKeyValue.value().value().textRange()).hasRange(1, 11, 1, 13);
  }

  @Test
  void shouldReadPropertyWithoutValue() {
    var code = "foo";

    var file = parseProperties(code);

    var databaseKeyValue = file.profiles().get(0).properties().get(0);
    assertThat(databaseKeyValue.key().value().value()).isEqualTo("foo");
    assertThat(databaseKeyValue.key().textRange()).hasRange(1, 0, 1, 3);
    assertThat(databaseKeyValue.key().value().textRange()).hasRange(1, 0, 1, 3);

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
    assertThat(databaseTuple.key().textRange()).hasRange(1, 0, 1, 8);
    assertThat(databaseTuple.key().value().textRange()).hasRange(1, 0, 1, 8);

    assertThat(databaseTuple.value().value().value()).isEqualTo("h2");
    assertThat(databaseTuple.value().textRange()).hasRange(1, 9, 1, 11);
    assertThat(databaseTuple.value().value().textRange()).hasRange(1, 9, 1, 11);

    var schemaLocationTuple = file.profiles().get(0).properties().get(1);
    assertThat(schemaLocationTuple.key().value().value()).isEqualTo("spring.sql.init.schema-locations");
    assertThat(schemaLocationTuple.key().textRange()).hasRange(2, 0, 2, 32);
    assertThat(schemaLocationTuple.key().value().textRange()).hasRange(2, 0, 2, 32);

    assertThat(schemaLocationTuple.value().value().value()).isEqualTo("classpath*:db/${database}/schema.sql");
    assertThat(schemaLocationTuple.value().textRange()).hasRange(2, 34, 2, 70);
    assertThat(schemaLocationTuple.value().value().textRange()).hasRange(2, 34, 2, 70);
  }

  @Test
  void shouldReadSimpleComment() {
    var code = "# database init";

    var file = parseProperties(code);

    var comment = file.profiles().get(0).comments().get(0);
    assertThat(comment.value()).isEqualTo("# database init");
    assertThat(comment.contentText()).isEqualTo(" database init");
    assertThat(comment.textRange()).hasRange(1, 0, 1, 15);
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
    assertThat(comment1.textRange()).hasRange(1, 0, 1, 11);

    var comment2 = file.profiles().get(0).comments().get(1);
    assertThat(comment2.value()).isEqualTo("! comment 2");
    assertThat(comment2.contentText()).isEqualTo(" comment 2");
    assertThat(comment2.textRange()).hasRange(3, 0, 3, 11);

    var comment3 = file.profiles().get(0).comments().get(2);
    assertThat(comment3.value()).isEqualTo("! comment 3");
    assertThat(comment3.contentText()).isEqualTo(" comment 3");
    assertThat(comment3.textRange()).hasRange(5, 0, 5, 11);

    var tuple1 = file.profiles().get(0).properties().get(0);
    assertThat(tuple1.key().value().value()).isEqualTo("foo");
    assertThat(tuple1.key().textRange()).hasRange(2, 0, 2, 3);
    assertThat(tuple1.value().value().value()).isEqualTo("bar");
    assertThat(tuple1.value().textRange()).hasRange(2, 4, 2, 7);

    var tuple2 = file.profiles().get(0).properties().get(1);
    assertThat(tuple2.key().value().value()).isEqualTo("bar");
    assertThat(tuple2.key().textRange()).hasRange(4, 0, 4, 3);
    assertThat(tuple2.value().value().value()).isEqualTo("baz");
    assertThat(tuple2.value().textRange()).hasRange(4, 4, 4, 7);
  }

  @Test
  void shouldReadKeyWithArray() {
    var code = """
      my.servers[0]=dev.example.com
      my.servers[1]=another.example.com""";

    var file = parseProperties(code);

    var tuple1 = file.profiles().get(0).properties().get(0);
    assertThat(tuple1.key().value().value()).isEqualTo("my.servers[0]");
    assertThat(tuple1.key().textRange()).hasRange(1, 0, 1, 13);
    assertThat(tuple1.value().value().value()).isEqualTo("dev.example.com");
    assertThat(tuple1.value().textRange()).hasRange(1, 14, 1, 29);

    var tuple2 = file.profiles().get(0).properties().get(1);
    assertThat(tuple2.key().value().value()).isEqualTo("my.servers[1]");
    assertThat(tuple2.key().textRange()).hasRange(2, 0, 2, 13);
    assertThat(tuple2.value().value().value()).isEqualTo("another.example.com");
    assertThat(tuple2.value().textRange()).hasRange(2, 14, 2, 33);
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
    assertThat(comment1.textRange()).hasRange(1, 0, 1, 11);

    var tuple1 = profile1.properties().get(0);
    assertThat(tuple1.key().value().value()).isEqualTo("foo");
    assertThat(tuple1.key().textRange()).hasRange(2, 0, 2, 3);
    assertThat(tuple1.value().value().value()).isEqualTo("bar");
    assertThat(tuple1.value().textRange()).hasRange(2, 6, 2, 9);

    assertThat(profile1.comments()).hasSize(1);
    assertThat(profile1.properties()).hasSize(1);

    var profile2 = file.profiles().get(1);
    var comment2 = profile2.comments().get(0);
    assertThat(comment2.value()).isEqualTo(profileSeparator);
    assertThat(comment2.contentText()).isEqualTo("---");
    assertThat(comment2.textRange()).hasRange(3, 0, 3, 4);

    var comment3 = profile2.comments().get(1);
    assertThat(comment3.value()).isEqualTo("! comment 2");
    assertThat(comment3.contentText()).isEqualTo(" comment 2");
    assertThat(comment3.textRange()).hasRange(4, 0, 4, 11);

    var tuple2 = profile1.properties().get(0);
    assertThat(tuple2.key().value().value()).isEqualTo("foo");
    assertThat(tuple2.key().textRange()).hasRange(2, 0, 2, 3);
    assertThat(tuple2.value().value().value()).isEqualTo("bar");
    assertThat(tuple2.value().textRange()).hasRange(2, 6, 2, 9);

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
      arguments("spring.profiles.active=profile1 \nspring.config.activate.on-profile=profile2", "profile2"),
      arguments("spring.profiles.active=profile1 \nspring.profiles.active=profile2", "profile2"),
      arguments("spring.profiles.default=profile1 \nspring.profiles.active=profile2", "profile2"),
      arguments("spring.config.activate.on-profile=profile1 \nspring.profiles.active=profile2\n", "profile2"),
      arguments("spring.profiles.active=profile1 \nspring.config.activate.on-profile=profile2 \nspring.profiles.default=newDefaultProfile", "profile2"),
      arguments("spring.profiles.active=profile1 \nspring.profiles.default=newDefaultProfile \nspring.config.activate.on-profile=profile2", "profile2"),
      arguments("spring.profiles.default=newDefaultProfile \nspring.config.activate.on-profile=profile1 \nspring.profiles.active=profile2", "profile2"),
      arguments("spring.profiles.default=newDefaultProfile \nfoo.bar=fooBar", "newDefaultProfile"),
      arguments("spring.profiles.default=profile1", "profile1"),
      arguments("#comment", "default"));
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

    assertThat(file.profiles().get(0).name()).isEqualTo("default");
    assertThat(file.profiles().get(1).name()).isEqualTo(expectedProfileName);
  }

  private File parseProperties(String code) {
    var propertiesFileContext = createPropertiesFileContext(code);
    PropertiesParseTreeVisitor visitor = new PropertiesParseTreeVisitor();
    return (File) visitor.visitPropertiesFile(propertiesFileContext);
  }
}
