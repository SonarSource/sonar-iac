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
package org.sonar.iac.arm.tree.impl.bicep;

import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.testing.IacCommonAssertions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.testing.IacTestUtils.code;
import static org.sonar.iac.common.testing.IacTestUtils.createInputFileContextMock;

class FileImplTest {

  BicepParser parser = BicepParser.create();

  @Test
  void shouldParseMinimalParameter() {
    String code = code("");

    File tree = (File) parser.parse(code, null);
    assertThat(tree.statements()).isEmpty();
    assertThat(tree.targetScope()).isEqualTo(File.Scope.RESOURCE_GROUP);
    ArmAssertions.assertThat(tree.targetScopeLiteral()).isNull();
  }

  @Test
  void shouldParseEmptyFileWithBOM() {
    String code = code("\uFEFF");

    File tree = (File) parser.parse(code, null);
    assertThat(tree.statements()).isEmpty();
    assertThat(tree.targetScope()).isEqualTo(File.Scope.RESOURCE_GROUP);
    ArmAssertions.assertThat(tree.targetScopeLiteral()).isNull();
  }

  @Test
  void shouldFailOnInvalidExpressionValue() {
    String code = code("invalid code -");
    InputFileContext inputFile = createInputFileContextMock("foo.bicep");

    assertThatThrownBy(() -> parser.parse(code, inputFile))
      .isInstanceOf(ParseException.class)
      .hasMessage("Cannot parse 'dir1/dir2/foo.bicep:1:1'");
  }
}
