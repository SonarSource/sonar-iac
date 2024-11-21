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
package org.sonar.iac.common.api.tree.impl;

import com.sonar.sslr.api.typed.Optional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.sonar.iac.common.api.tree.IacToken;
import org.sonar.iac.common.api.tree.SeparatedList;
import org.sonar.iac.common.api.tree.Tree;

public record SeparatedListImpl<T extends Tree, U extends IacToken> (List<T> elements,
  List<U> separators) implements SeparatedList<T, U> {

  @Override
  public List<U> separators() {
    return separators.stream().filter(Objects::nonNull).toList();
  }

  @Override
  public List<Tree> elementsAndSeparators() {
    List<Tree> result = new ArrayList<>();

    Iterator<T> elementsIterator = elements.iterator();
    Iterator<U> separatorIterator = separators.iterator();

    while (elementsIterator.hasNext() && separatorIterator.hasNext()) {
      result.add(elementsIterator.next());
      U separator = separatorIterator.next();
      if (separator != null) {
        result.add(separator);
      }
    }
    if (elementsIterator.hasNext()) {
      result.add(elementsIterator.next());
    }

    return result;
  }

  public static <R extends Tree, S extends IacToken> SeparatedListImpl<R, S> separatedList(R firstElement, Optional<List<Tuple<S, R>>> additionalElements) {
    return separatedList(firstElement, additionalElements.or(Collections.emptyList()));
  }

  public static <R extends Tree, S extends IacToken> SeparatedListImpl<R, S> separatedList(R firstElement, List<Tuple<S, R>> additionalElements) {
    List<R> elements = new ArrayList<>();
    List<S> separators = new ArrayList<>();
    elements.add(firstElement);

    for (Tuple<S, R> elementsWithSeparators : additionalElements) {
      separators.add(elementsWithSeparators.first());
      elements.add(elementsWithSeparators.second());
    }

    return new SeparatedListImpl<>(elements, separators);
  }

  public static <R extends Tree, S extends IacToken> SeparatedListImpl<R, S> emptySeparatedList() {
    return new SeparatedListImpl<>(new ArrayList<>(), new ArrayList<>());
  }

  @Override
  public String toString() {
    return elementsAndSeparators().stream()
      .map(Object::toString)
      .collect(Collectors.joining(" "));
  }
}
