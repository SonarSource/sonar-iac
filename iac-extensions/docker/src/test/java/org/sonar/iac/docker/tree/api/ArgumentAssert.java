/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.docker.tree.api;

import org.sonar.iac.docker.symbols.ArgumentResolution;

public class ArgumentAssert extends DockerTreeAssert<ArgumentAssert, Argument> {

  private ArgumentAssert(Argument argument) {
    super(argument, ArgumentAssert.class);
  }

  public static ArgumentAssert assertThat(Argument actual) {
    return new ArgumentAssert(actual);
  }

  public ArgumentResolutionAssert resolve() {
    return new ArgumentResolutionAssert(ArgumentResolution.of(actual));
  }

  public ArgumentAssert hasValue(String value) {
    isNotNull();
    resolve().hasValue(value);
    return this;
  }

  public ArgumentAssert isUnresolved() {
    isNotNull();
    resolve().isUnresolved();
    return this;
  }
}
