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
package org.sonar.iac.common.predicates;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.iac.common.testing.IacTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

class KubernetesFilePredicateTest {
  @TempDir
  Path tempDir;

  @ParameterizedTest
  @MethodSource
  void shouldDetectKubernetesFile(String content, boolean shouldMatch) {
    var predicate = new KubernetesFilePredicate(true);
    assertThat(predicate.apply(IacTestUtils.inputFile("test.yaml", tempDir, content, "kubernetes"))).isEqualTo(shouldMatch);
  }

  static Stream<Arguments> shouldDetectKubernetesFile() {
    return Stream.of(
      of("apiVersion: v1", false),
      of("kind: Pod", false),
      of("""
        metadata:
          labels:
            foo: bar
        """, false),
      of("""
        apiVersion: v1
        kind: Pod
        """, false),
      of("""
        apiVersion: v1
        kind: Pod
        metadata:
          labels:
            foo: bar
        """, true),
      of("""
        apiVersion: v1
        kind: Pod
        ---
        metadata:
          labels:
            foo: bar
        """, false));
  }
}
