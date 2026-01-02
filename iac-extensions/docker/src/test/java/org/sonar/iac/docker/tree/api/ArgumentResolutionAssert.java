/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
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
