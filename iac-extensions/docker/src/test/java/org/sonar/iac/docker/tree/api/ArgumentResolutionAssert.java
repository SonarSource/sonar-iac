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
package org.sonar.iac.docker.tree.api;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.sonar.iac.docker.symbols.ArgumentResolution;

public class ArgumentResolutionAssert extends AbstractAssert<ArgumentResolutionAssert, ArgumentResolution> {

  protected ArgumentResolutionAssert(ArgumentResolution argumentResolution) {
    super(argumentResolution, ArgumentResolutionAssert.class);
  }

  public static ArgumentResolutionAssert assertThat(ArgumentResolution actual) {
    return new ArgumentResolutionAssert(actual);
  }

  public ArgumentResolutionAssert hasValue(String value) {
    isNotNull();
    Assertions.assertThat(actual.value())
      .overridingErrorMessage("Expected ArgumentResolution value to be <%s> but was <%s>", value, actual.value())
      .isEqualTo(value);
    return this;
  }

  public ArgumentResolutionAssert isUnresolved() {
    isNotNull();
    Assertions.assertThat(actual.status())
      .overridingErrorMessage("Expected ArgumentResolution to be unresolved but value was <%s>", actual.value())
      .isEqualTo(ArgumentResolution.Status.UNRESOLVED);
    return this;
  }
}
