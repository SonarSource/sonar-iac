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
package org.sonar.iac.arm.parser;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmAssertions.assertThat;

class ArmJsonParserTest {

  @Test
  void multilineStringShouldBeConvertedToScalarWithLinebreaks() {
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

  @Test
  void stringWithLineBreakMarkShouldNotBeSplitInMultipleLine() {
    var parser = new ArmJsonParser();
    var file = parser.parseJson("""
      {
        "string": "line1\nline2"
      }
      """);
    ScalarTree scalar = (ScalarTree) ((MappingTree) file.documents().get(0)).elements().get(0).value();
    assertThat(scalar.value()).isEqualTo("line1\nline2");
    // TODO SONARIAC-1436
    // assertThat(scalar.textRange()).hasRange(2, 12, 2, 25);
  }

  @Test
  void multilineStringsWithEmojisShouldHaveCorrectTextRange() {
    var parser = new ArmJsonParser();
    var file = parser.parseJson("""
      {
        "key1": "🧩📝🚀",
        "key2": "🧩 line1 📝
          🔍 line2 🚀"
      }
      """);
    ScalarTree scalar = (ScalarTree) ((MappingTree) file.documents().get(0)).elements().get(1).value();
    assertThat(scalar.value()).isEqualTo("\uD83E\uDDE9 line1 \uD83D\uDCDD\n    \uD83D\uDD0D line2 \uD83D\uDE80");
  }

}
