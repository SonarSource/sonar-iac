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

public class SeparatedListImpl<T extends Tree, U extends IacToken> implements SeparatedList<T, U> {

  private final List<T> elements;
  private final List<U> separators;

  public SeparatedListImpl(List<T> elements, List<U> separators) {
    this.elements = elements;
    this.separators = separators;
  }

  @Override
  public List<T> elements() {
    return elements;
  }

  @Override
  public List<U> separators() {
    return separators.stream().filter(Objects::nonNull).collect(Collectors.toList());
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

}
