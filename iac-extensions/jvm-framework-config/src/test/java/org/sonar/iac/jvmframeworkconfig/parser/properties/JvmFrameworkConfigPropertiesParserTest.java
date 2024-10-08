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
package org.sonar.iac.jvmframeworkconfig.parser.properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.testing.IacTestUtils;
import org.sonar.iac.jvmframeworkconfig.tree.api.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class JvmFrameworkConfigPropertiesParserTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  @Test
  void shouldParseExampleFile() {
    var code = """
      foo=bar""";
    var parser = new JvmFrameworkConfigPropertiesParser();
    InputFileContext inputFileContext = IacTestUtils.createInputFileContextMock("foo.properties");

    var tree = (File) parser.parse(code, inputFileContext);

    var tuple = tree.profiles().get(0).properties().get(0);
    assertThat(tuple.key().value().value()).isEqualTo("foo");
    assertThat(tuple.value().value().value()).isEqualTo("bar");
  }

  @Test
  void shouldParsePropertyWithoutValue() {
    var code = """
      foo=""";
    var parser = new JvmFrameworkConfigPropertiesParser();
    InputFileContext inputFileContext = IacTestUtils.createInputFileContextMock("foo.properties");

    var tree = (File) parser.parse(code, inputFileContext);

    var tuple = tree.profiles().get(0).properties().get(0);
    assertThat(tuple.key().value().value()).isEqualTo("foo");
    assertThat(tuple.value()).isNull();
    assertThatNoException().isThrownBy(tuple::textRange);
  }
}
