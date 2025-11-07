/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
package org.sonar.iac.common.api.tree.impl;

import com.sonar.sslr.api.typed.Optional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.IacToken;
import org.sonar.iac.common.api.tree.SeparatedList;
import org.sonar.iac.common.api.tree.Tree;

/**
 * Store a list of elements and separators.
 * The implementation allow any mix of separator and elements, including separator in first position, last position, consecutive separators and/or
 * consecutive elements.
 * It keeps the order insertion of any separator and elements, and the method {@link #elementsAndSeparators} will provide them back in the same order.
 * @param <E> Represent the elements in the list, it should extends {@link Tree}
 * @param <S> Represent the separator in the list, it should extends {@link IacToken}
 */
public final class SeparatedListImpl<E extends Tree, S extends IacToken> implements SeparatedList<E, S> {

  private final List<E> elements;
  private final List<S> separators;
  private final List<Tree> elementsAndSeparators;

  private SeparatedListImpl(List<E> elements, List<S> separators, List<Tree> elementsAndSeparators) {
    this.elements = elements;
    this.separators = separators;
    this.elementsAndSeparators = elementsAndSeparators;
  }

  @Override
  public List<E> elements() {
    return Collections.unmodifiableList(elements);
  }

  @Override
  public List<S> separators() {
    return Collections.unmodifiableList(separators);
  }

  @Override
  public List<Tree> elementsAndSeparators() {
    return Collections.unmodifiableList(elementsAndSeparators);
  }

  private static class SeparatedListBuilder<E extends Tree, S extends IacToken> {
    private final List<E> elements = new ArrayList<>();
    private final List<S> separators = new ArrayList<>();
    private final List<Tree> elementsAndSeparators = new ArrayList<>();

    private void addElement(E element) {
      elements.add(element);
      elementsAndSeparators.add(element);
    }

    private void addSeparator(@Nullable S separator) {
      if (separator != null) {
        separators.add(separator);
        elementsAndSeparators.add(separator);
      }
    }

    private void addSeparator(Optional<S> separator) {
      if (separator.isPresent()) {
        addSeparator(separator.get());
      }
    }

    private SeparatedListImpl<E, S> build() {
      return new SeparatedListImpl<>(elements, separators, elementsAndSeparators);
    }
  }

  /**
   * Create a separated list with an optional separator in the beginning.
   * Equivalent grammar: {@code separator? element ( separator element )*}
   * @param optSeparator Optional separator in the beginning
   * @param firstElement First element of the list
   * @param separatorWithElements Optional list of tuples with separator and element
   * @return SeparatedList with the elements and separators
   */
  public static <E extends Tree, S extends IacToken> SeparatedListImpl<E, S> separatedList(Optional<S> optSeparator, E firstElement,
    Optional<List<Tuple<S, E>>> separatorWithElements) {
    SeparatedListBuilder<E, S> builder = new SeparatedListBuilder<>();
    builder.addSeparator(optSeparator);
    builder.addElement(firstElement);
    for (Tuple<S, E> elementsWithSeparators : separatorWithElements.or(Collections.emptyList())) {
      builder.addSeparator(elementsWithSeparators.first());
      builder.addElement(elementsWithSeparators.second());
    }
    return builder.build();
  }

  /**
   * Create a separated list with a first element and optinal additional following elements
   * Equivalent grammar: {@code element ( separator element )*}
   * @param firstElement First element of the list
   * @param additionalElements Optional list of tuples with separator and element
   * @return SeparatedList with the elements and separators
   */
  public static <E extends Tree, S extends IacToken> SeparatedListImpl<E, S> separatedList(E firstElement, Optional<List<Tuple<S, E>>> additionalElements) {
    return separatedList(firstElement, additionalElements.or(Collections.emptyList()));
  }

  /**
   * Create a separated list with a first element and optinal additional following elements
   * Equivalent grammar: {@code element ( separator element )+}
   * @param firstElement First element of the list
   * @param additionalElements List of tuples with separator and element
   * @return SeparatedList with the elements and separators
   */
  public static <E extends Tree, S extends IacToken> SeparatedListImpl<E, S> separatedList(E firstElement, List<Tuple<S, E>> additionalElements) {
    SeparatedListBuilder<E, S> builder = new SeparatedListBuilder<>();
    builder.addElement(firstElement);
    for (Tuple<S, E> elementsWithSeparators : additionalElements) {
      builder.addSeparator(elementsWithSeparators.first());
      builder.addElement(elementsWithSeparators.second());
    }
    return builder.build();
  }

  /**
   * Create a separated list with elements and optional separators
   * Equivalent grammar: {@code ( element separator? )+}
   * @param elementsWithOptionalSeparators List of tuples with element and optional separator
   * @return SeparatedList with the elements and separators
   */
  public static <E extends Tree, S extends IacToken> SeparatedListImpl<E, S> separatedList(List<Tuple<E, Optional<S>>> elementsWithOptionalSeparators) {
    SeparatedListBuilder<E, S> builder = new SeparatedListBuilder<>();
    for (Tuple<E, Optional<S>> elementWithOptionalSeparator : elementsWithOptionalSeparators) {
      builder.addElement(elementWithOptionalSeparator.first());
      builder.addSeparator(elementWithOptionalSeparator.second());
    }
    return builder.build();
  }

  /**
   * Create an empty separated list with elements.
   * @return A SeparatedList instance with no elements and separators inside
   */
  public static <R extends Tree, S extends IacToken> SeparatedListImpl<R, S> emptySeparatedList() {
    return new SeparatedListImpl<>(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
  }

  @Override
  public String toString() {
    return elementsAndSeparators().stream()
      .map(Object::toString)
      .collect(Collectors.joining(" "));
  }
}
