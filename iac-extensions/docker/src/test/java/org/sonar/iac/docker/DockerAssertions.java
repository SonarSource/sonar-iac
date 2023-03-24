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
package org.sonar.iac.docker;

import org.sonar.iac.common.testing.IacCommonAssertions;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.ArgumentAssert;
import org.sonar.iac.docker.tree.api.KeyValuePair;
import org.sonar.iac.docker.tree.api.KeyValuePairAssert;

public class DockerAssertions extends IacCommonAssertions {
  public static ArgumentAssert assertThat(Argument actual) {
    return ArgumentAssert.assertThat(actual);
  }

  public static KeyValuePairAssert assertThat(KeyValuePair actual) {
    return KeyValuePairAssert.assertThat(actual);
  }
}
