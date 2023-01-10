/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.docker.checks;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DirectoryCopySourceCheckTest {

  @Test
  void test_add() {
    DockerVerifier.verify("DirectoryCopySourceCheck/Dockerfile_add", new DirectoryCopySourceCheck());
  }

  @Test
  void test_copy() {
    DockerVerifier.verify("DirectoryCopySourceCheck/Dockerfile_copy", new DirectoryCopySourceCheck());
  }

  @Test
  void test_normalize() {
    assertThat(DirectoryCopySourceCheck.normalize("./test")).isEqualTo(new String[] {".", "test"});
    assertThat(DirectoryCopySourceCheck.normalize("./p/../test")).isEqualTo(new String[] {".", "test"});
    assertThat(DirectoryCopySourceCheck.normalize("/test")).isEqualTo(new String[] {"", "test"});
    assertThat(DirectoryCopySourceCheck.normalize("/./test")).isEqualTo(new String[] {"", "test"});
    assertThat(DirectoryCopySourceCheck.normalize("test")).isEqualTo(new String[] {"test"});
    assertThat(DirectoryCopySourceCheck.normalize("test/p")).isEqualTo(new String[] {"test", "p"});
    assertThat(DirectoryCopySourceCheck.normalize("c:/test")).isEqualTo(new String[] {"c:", "test"});
    assertThat(DirectoryCopySourceCheck.normalize("./test/a*")).isEqualTo(new String[] {".", "test", "a*"});
  }
}
