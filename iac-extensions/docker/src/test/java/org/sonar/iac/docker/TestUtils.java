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
import org.sonar.iac.docker.tree.api.FromInstruction;
import org.sonar.iac.docker.utils.ArgumentUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class TestUtils {

  private TestUtils() {
    // utils class
  }

  @Nullable
  public static String argValue(Argument argument) {
    return ArgumentUtils.resolve(argument).value();
  }

  public static void assertArgumentsValue(List<Argument> args, String... values) {
    assertThat(args).hasSize(values.length);
    for (int i = 0; i < args.size(); i++) {
      assertThat(argValue(args.get(i))).isEqualTo(values[i]);
    }
  }

  public static void assertFrom(FromInstruction from, String expectedName, @Nullable String expectedFlagName, @Nullable String expectedFlagValue, @Nullable String expectedAlias) {
    assertThat(argValue(from.image())).isEqualTo(expectedName);
    if (expectedFlagName != null) {
      assertThat(from.platform()).isNotNull();
      assertThat(from.platform().name()).isEqualTo(expectedFlagName);
      if (expectedFlagValue != null) {
        assertThat(argValue(from.platform().value())).isEqualTo(expectedFlagValue);
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

  public static <T extends Tree> T firstDescendant(Tree root, Class<T> clazz) {
    return (T) firstDescendant(root, clazz::isInstance).orElse(null);
  }

  private static Optional<Tree> firstDescendant(@Nullable Tree root, Predicate<Tree> predicate) {
    return descendants(root).filter(predicate).findFirst();
  }

  private static Stream<Tree> descendants(@Nullable Tree root) {
    if (root == null || root.children().isEmpty()) {
      return Stream.empty();
    }
    Spliterator<Tree> spliterator = Spliterators.spliteratorUnknownSize(root.children().iterator(), Spliterator.ORDERED);
    Stream<Tree> stream = StreamSupport.stream(spliterator, false);
    return stream.flatMap(tree -> Stream.concat(Stream.of(tree), descendants(tree)));
  }
}
