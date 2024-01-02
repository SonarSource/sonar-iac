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
package org.sonar.iac.arm.parser;

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.plugin.ArmLanguage;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.sonar.iac.common.testing.IacTestUtils.createInputFileContextMock;

class ArmParserTest {

  private final ArmParser parser = new ArmParser();

  private final InputFileContext inputFileJsonContext = createInputFileContextMock("foo.json");

  @Test
  void shouldParseEmptyJson() {
    File tree = (File) parser.parse("{}", inputFileJsonContext);
    assertThat(tree.is(ArmTree.Kind.FILE)).isTrue();
    assertThat(tree.statements()).isEmpty();
    assertThat(tree.children()).isEmpty();
    assertThat(tree.parent()).isNull();
    assertThatThrownBy(tree::textRange)
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrowExceptionWhenParseError() {
    assertThatThrownBy(() -> parser.parse("{", inputFileJsonContext))
      .isInstanceOf(ParseException.class)
      .hasMessage("Cannot parse 'dir1/dir2/foo.json'");
  }

  @Test
  void shouldThrowExceptionWithFileNameWhenParseError() {
    assertThatThrownBy(() -> parser.parse("{", inputFileJsonContext))
      .isInstanceOf(ParseException.class)
      .hasMessage("Cannot parse 'dir1/dir2/foo.json'");
  }

  @Test
  void shouldParseEmptyBicep() {
    File tree = (File) parser.parse("", createInputFileContextMock("foo.bicep", ArmLanguage.KEY));
    assertThat(tree.is(ArmTree.Kind.FILE)).isTrue();
    assertThat(tree.statements()).isEmpty();
    assertThat(tree.children()).hasSize(1).extracting("value").containsExactly("");
    assertThat(tree.parent()).isNull();
  }
}
