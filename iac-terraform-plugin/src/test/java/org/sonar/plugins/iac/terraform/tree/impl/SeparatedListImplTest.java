package org.sonar.plugins.iac.terraform.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.iac.terraform.api.tree.Tree;
import org.sonar.plugins.iac.terraform.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.iac.terraform.parser.lexical.InternalSyntaxToken;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeparatedListImplTest {
  private static Tree treeA = new InternalSyntaxToken(1, 1, "a", Collections.emptyList(), 1, false);
  private static Tree treeB = new InternalSyntaxToken(1, 1, "b", Collections.emptyList(), 1, false);
  private static List<Tree> elementsList = Arrays.asList(treeA, treeB);
  private static SyntaxToken separator = new InternalSyntaxToken(1, 1, ",", Collections.emptyList(), 1, false);
  private static SeparatedListImpl<Tree> list = new SeparatedListImpl<>(elementsList, Arrays.asList(separator));

  @Test
  void empty() {
    SeparatedListImpl<Tree> list = SeparatedListImpl.empty();
    assertThat(list).isEmpty();
    assertThat(list.elementsAndSeparators()).isEmpty();
  }

  @Test
  void simple_list() {
    assertThat(list).hasSize(2);
    assertThat(list.getSeparators()).hasSize(1);
  }

  @Test
  void elementsAndSeparators() {
    assertThat(list.elementsAndSeparators()).containsExactly(treeA, separator, treeB);
  }

  @Test
  void contains() {
    assertThat(list.contains(treeA)).isTrue();
  }

  @Test
  void iterator() {
    assertThat(list.iterator().next()).isEqualTo(treeA);
  }

  @Test
  void toArray() {
    assertThat(list.toArray()).isEqualTo(elementsList.toArray());
  }

  @Test
  void add() {
    SeparatedListImpl<Tree> tmpList = new SeparatedListImpl<>(Arrays.asList(treeA, treeB), Arrays.asList(separator));
    assertThatThrownBy(() -> tmpList.add(treeA)).isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void remove() {
    SeparatedListImpl<Tree> tmpList = new SeparatedListImpl<>(Arrays.asList(treeA, treeB), Arrays.asList(separator));
    assertThatThrownBy(() -> tmpList.remove(treeA)).isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void containsAll() {
  }

  @Test
  void addAll() {
  }

  @Test
  void testAddAll() {
  }

  @Test
  void removeAll() {
  }

  @Test
  void retainAll() {
  }

  @Test
  void clear() {
  }

  @Test
  void get() {
    assertThat(list.get(0)).isEqualTo(elementsList.get(0));
  }

  @Test
  void set() {
  }

  @Test
  void testAdd() {
  }

  @Test
  void testRemove() {
  }

  @Test
  void indexOf() {
    assertThat(list.indexOf(treeA)).isZero();
  }

  @Test
  void lastIndexOf() {
  }

  @Test
  void listIterator() {
  }

  @Test
  void testListIterator() {
  }

  @Test
  void subList() {
  }

}
