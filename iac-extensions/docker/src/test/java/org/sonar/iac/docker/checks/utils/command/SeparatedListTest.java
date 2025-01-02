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
package org.sonar.iac.docker.checks.utils.command;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.fest.assertions.Assertions.assertThat;

class SeparatedListTest {

  @Test
  void shouldEncapsulateConstructorArguments() {
    List<String> elements = List.of("foo");
    List<String> separators = List.of("bar");
    SeparatedList<String, String> actual = new SeparatedList<>(elements, separators);
    assertThat(actual.elements()).isSameAs(elements);
    assertThat(actual.separators()).isSameAs(separators);
  }
}
