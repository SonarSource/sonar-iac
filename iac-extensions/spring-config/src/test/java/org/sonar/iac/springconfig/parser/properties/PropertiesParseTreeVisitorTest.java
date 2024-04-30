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

import org.junit.jupiter.api.Test;
import org.sonar.iac.springconfig.tree.api.File;

import static org.assertj.core.api.Assertions.assertThat;
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

  private File parseProperties(String code) {
    var propertiesFileContext = createPropertiesFileContext(code);
    PropertiesParseTreeVisitor visitor = new PropertiesParseTreeVisitor();
    return (File) visitor.visitPropertiesFile(propertiesFileContext);
  }
}
