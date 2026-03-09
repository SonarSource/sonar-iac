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
package org.sonar.iac.common.checks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * This class provides utility methods to convert a list of strings into a regex pattern.
 * It offers some optimization mechanism based on the common prefixes of the strings.
 */
public class OptimizedListToPatternBuilder {

  private final Collection<String> strings;
  private String prefix;
  private UnaryOperator<String> transformation;

  public OptimizedListToPatternBuilder(Collection<String> strings) {
    this.strings = strings;
    this.prefix = null;
    this.transformation = s -> s;
  }

  public static OptimizedListToPatternBuilder from(String... strings) {
    return fromCollection(List.of(strings));
  }

  public static OptimizedListToPatternBuilder fromCollection(Collection<String> strings) {
    return new OptimizedListToPatternBuilder(strings);
  }

  public OptimizedListToPatternBuilder optimizeOnPrefix(String prefix) {
    this.prefix = prefix;
    return this;
  }

  public OptimizedListToPatternBuilder applyStringTransformation(UnaryOperator<String> transformation) {
    this.transformation = transformation;
    return this;
  }

  public String build() {
    var listStr = strings;
    if (prefix != null) {
      var partitioned = strings.stream().collect(Collectors.partitioningBy(s -> s.startsWith(prefix)));
      var optimizedList = partitioned.get(true).stream()
        .map(s -> s.substring(prefix.length()))
        .toList();
      listStr = new ArrayList<>(partitioned.get(false));
      listStr.add("%s(?:%s)".formatted(prefix, String.join("|", optimizedList)));
    }
    return listStr.stream()
      .map(transformation)
      .reduce((String s1, String s2) -> s1 + "|" + s2)
      .orElse("");
  }
}
