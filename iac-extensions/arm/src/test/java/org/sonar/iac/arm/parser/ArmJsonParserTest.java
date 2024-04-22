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
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmAssertions.assertThat;

class ArmJsonParserTest {

  @Test
  void convertedMultilineStringShouldBeConvertedToScalarWithLinebreaks() {
    var parser = new ArmJsonParser();

    var file = parser.parseJson("""
      {
        "string": "line1
          line2"
      }
      """);
    ScalarTree scalar = (ScalarTree) ((MappingTree) file.documents().get(0)).elements().get(0).value();
    assertThat(scalar.value()).isEqualTo("line1\n    line2");
    assertThat(scalar.textRange()).hasRange(2, 12, 3, 10);
  }

}
