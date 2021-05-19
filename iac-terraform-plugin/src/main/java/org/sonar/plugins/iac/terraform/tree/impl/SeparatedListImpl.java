/*
 * SonarQube IaC Terraform Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.plugins.iac.terraform.tree.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.sonar.plugins.iac.terraform.api.tree.SeparatedList;
import org.sonar.plugins.iac.terraform.api.tree.Tree;
import org.sonar.plugins.iac.terraform.api.tree.lexical.SyntaxToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class SeparatedListImpl<T extends Tree> implements SeparatedList<T> {

  private final List<T> list;
  private final List<SyntaxToken> separators;
  private final List<Tree> elementsAndSeparators;

  public SeparatedListImpl(List<T> list, List<SyntaxToken> separators) {
    Preconditions.checkArgument(
      list.size() == separators.size() + 1 || list.size() == separators.size(),
      "Instantiating a SeparatedList with inconsistent number of elements (%s) and separators (%s)",
      list.size(), separators.size());

    this.list = list;
    this.separators = separators;

    elementsAndSeparators = new ArrayList<>();
    int separatorsSize = separators.size();
    for (int i = 0; i < list.size(); i++) {
      elementsAndSeparators.add(list.get(i));
      if (i < separatorsSize) {
        elementsAndSeparators.add(separators.get(i));
      }
    }
  }

  public static <T extends Tree> SeparatedListImpl<T> empty() {
    return new SeparatedListImpl<>(ImmutableList.<T>of(), ImmutableList.<SyntaxToken>of());
  }

  @Override
  public SyntaxToken getSeparator(int i) {
    return separators.get(i);
  }

  @Override
  public List<SyntaxToken> getSeparators() {
    return separators;
  }


  @Override
  public int size() {
    return list.size();
  }

  @Override
  public boolean isEmpty() {
    return list.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return list.contains(o);
  }

  @Override
  public Iterator<T> iterator() {
    return list.iterator();
  }

  @Override
  public Object[] toArray() {
    return list.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return list.toArray(a);
  }

  @Override
  public boolean add(T e) {
    return list.add(e);
  }

  @Override
  public boolean remove(Object o) {
    return list.remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return list.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    return list.addAll(c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends T> c) {
    return list.addAll(index, c);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return list.removeAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return list.retainAll(c);
  }

  @Override
  public void clear() {
    list.clear();
  }

  @Override
  public T get(int index) {
    return list.get(index);
  }

  @Override
  public T set(int index, T element) {
    return list.set(index, element);
  }

  @Override
  public void add(int index, T element) {
    list.add(index, element);
  }

  @Override
  public T remove(int index) {
    return list.remove(index);
  }

  @Override
  public int indexOf(Object o) {
    return list.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return list.lastIndexOf(o);
  }

  @Override
  public ListIterator<T> listIterator() {
    return list.listIterator();
  }

  @Override
  public ListIterator<T> listIterator(int index) {
    return list.listIterator(index);
  }

  @Override
  public List<T> subList(int fromIndex, int toIndex) {
    return list.subList(fromIndex, toIndex);
  }

  @Override
  public List<Tree> elementsAndSeparators() {
    return elementsAndSeparators;
  }

}
