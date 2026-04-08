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
package org.sonar.iac.common.checks;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OptimizedListToPatternBuilderTest {

  @Test
  void shouldBuildEmptyPattern() {
    var pattern = OptimizedListToPatternBuilder
      .fromCollection(List.of())
      .build();
    assertThat(pattern).isEmpty();
  }

  @Test
  void shouldBuildSingleValuePattern() {
    var pattern = OptimizedListToPatternBuilder
      .fromCollection(List.of("bob"))
      .build();
    assertThat(pattern).isEqualTo("bob");
  }

  @Test
  void shouldBuildMultipleValuePattern() {
    var pattern = OptimizedListToPatternBuilder
      .fromCollection(List.of("bob", "alice", "charlie"))
      .build();
    assertThat(pattern).isEqualTo("bob|alice|charlie");
  }

  @Test
  void shouldApplyTransformationToFinalString() {
    var pattern = OptimizedListToPatternBuilder
      .fromCollection(List.of("bob", "alice", "charlie"))
      .applyStringTransformation(s -> s.replaceAll("\\b(\\w++)\\b", "'$1'"))
      .build();
    assertThat(pattern).isEqualTo("'bob'|'alice'|'charlie'");
  }

  @Test
  void shouldBuildOptimizedByPrefixValuePattern() {
    var pattern = OptimizedListToPatternBuilder
      .fromCollection(List.of("pre-bob", "pre-alice", "pre-charlie"))
      .optimizeOnPrefix("pre-")
      .build();
    assertThat(pattern).isEqualTo("pre-(?:bob|alice|charlie)");
  }
}
