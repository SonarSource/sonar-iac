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

import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.ArgumentAssert;
import org.sonar.iac.docker.tree.api.FromInstruction;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Define static methods that can be used to help writing tests.
 */
public class TestUtils {

  private TestUtils() {
    // utils class
  }

  public static void assertArgumentsValue(List<Argument> args, String... values) {
    assertThat(args).hasSize(values.length);
    for (int i = 0; i < args.size(); i++) {
      ArgumentAssert.assertThat(args.get(i)).hasValue(values[i]);
    }
  }

  public static void assertFrom(FromInstruction from, String expectedName, @Nullable String expectedFlagName, @Nullable String expectedFlagValue, @Nullable String expectedAlias) {
    ArgumentAssert.assertThat(from.image()).hasValue(expectedName);
    if (expectedFlagName != null) {
      assertThat(from.platform()).isNotNull();
      assertThat(from.platform().name()).isEqualTo(expectedFlagName);
      if (expectedFlagValue != null) {
        ArgumentAssert.assertThat(from.platform().value()).hasValue(expectedFlagValue);
      } else {
        assertThat(from.platform().value()).isNull();
      }
    } else {
      assertThat(from.platform()).isNull();
    }

    if (expectedAlias != null) {
      assertThat(from.alias().alias().value()).isEqualTo(expectedAlias);
    } else {
      assertThat(from.alias()).isNull();
    }
  }
}
