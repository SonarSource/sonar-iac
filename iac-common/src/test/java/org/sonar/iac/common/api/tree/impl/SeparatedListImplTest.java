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
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.IacToken;
import org.sonar.iac.common.api.tree.SeparatedList;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.CommonTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class SeparatedListImplTest {

  @Test
  void emptySeparatedListShouldProducesEmptyLists() {
    SeparatedList<Tree, IacToken> separatedList = SeparatedListImpl.emptySeparatedList();

    assertThat(separatedList.separators()).isEmpty();
    assertThat(separatedList.elements()).isEmpty();
    assertThat(separatedList.elementsAndSeparators()).isEmpty();
  }

  @Test
  void separatedListShouldBeCorrectlyConstructed() {
    Tree firstElement = CommonTestUtils.TestTree.tree();
    Tree tupleElement = CommonTestUtils.TestTree.tree();
    IacToken tupleToken = CommonTestUtils.TestIacToken.token();

    SeparatedList<Tree, IacToken> resultingSeparatedList = SeparatedListImpl.separatedList(firstElement, Optional.of(List.of(new Tuple<>(tupleToken, tupleElement))));

    assertThat(resultingSeparatedList.elements()).containsExactly(firstElement, tupleElement);
    assertThat(resultingSeparatedList.separators()).containsExactly(tupleToken);
    assertThat(resultingSeparatedList.elementsAndSeparators()).containsExactly(firstElement, tupleToken, tupleElement);
  }

  @Test
  void separatedListShouldHandleNullSeparator() {
    Tree firstElement = CommonTestUtils.TestTree.tree();
    Tree tupleElement = CommonTestUtils.TestTree.tree();

    SeparatedList<Tree, IacToken> resultingSeparatedList = SeparatedListImpl.separatedList(firstElement, Optional.of(List.of(new Tuple<>(null, tupleElement))));

    assertThat(resultingSeparatedList.elements()).containsExactly(firstElement, tupleElement);
    assertThat(resultingSeparatedList.separators()).isEmpty();
    assertThat(resultingSeparatedList.elementsAndSeparators()).containsExactly(firstElement, tupleElement);
  }

  @Test
  void separatedListShouldHandleBothSeparatorAndNullSeparatorAndKeepThemOrdered() {
    Tree element1 = CommonTestUtils.TestTree.tree();
    IacToken separator1 = CommonTestUtils.TestIacToken.token();
    Tree element2 = CommonTestUtils.TestTree.tree();
    Tree element3 = CommonTestUtils.TestTree.tree();
    IacToken separator2 = CommonTestUtils.TestIacToken.token();
    Tree element4 = CommonTestUtils.TestTree.tree();

    SeparatedList<Tree, IacToken> resultingSeparatedList = SeparatedListImpl.separatedList(element1, Optional.of(List.of(
      new Tuple<>(separator1, element2),
      new Tuple<>(null, element3),
      new Tuple<>(separator2, element4))));

    assertThat(resultingSeparatedList.elements()).containsExactly(element1, element2, element3, element4);
    assertThat(resultingSeparatedList.separators()).containsExactly(separator1, separator2);
    assertThat(resultingSeparatedList.elementsAndSeparators()).containsExactly(element1, separator1, element2, element3, separator2, element4);
  }

  @Test
  void separatedListShouldConstructWithJustAList() {
    Tree element1 = CommonTestUtils.TestTree.tree();
    IacToken separator1 = CommonTestUtils.TestIacToken.token();
    Tree element2 = CommonTestUtils.TestTree.tree();
    Tree element3 = CommonTestUtils.TestTree.tree();
    IacToken separator2 = CommonTestUtils.TestIacToken.token();
    Tree element4 = CommonTestUtils.TestTree.tree();

    SeparatedList<Tree, IacToken> resultingSeparatedList = SeparatedListImpl.separatedList(List.of(
      new Tuple<>(element1, Optional.of(separator1)),
      new Tuple<>(element2, Optional.absent()),
      new Tuple<>(element3, Optional.of(separator2)),
      new Tuple<>(element4, Optional.absent())));

    assertThat(resultingSeparatedList.elements()).containsExactly(element1, element2, element3, element4);
    assertThat(resultingSeparatedList.separators()).containsExactly(separator1, separator2);
    assertThat(resultingSeparatedList.elementsAndSeparators()).containsExactly(element1, separator1, element2, element3, separator2, element4);
  }

  @Test
  void absentOptionalShouldRetrieveSeparatedListWithOnlyOneElement() {
    Tree firstElement = CommonTestUtils.TestTree.tree();

    SeparatedList<Tree, IacToken> resultingSeparatedList = SeparatedListImpl.separatedList(firstElement, Optional.absent());

    assertThat(resultingSeparatedList.elements()).containsExactly(firstElement);
    assertThat(resultingSeparatedList.separators()).isEmpty();
  }

  @Test
  void shouldHandleSeparatedListStartingWithOptionalSeparatorWithValue() {
    Tree element1 = CommonTestUtils.TestTree.tree();
    IacToken separator1 = CommonTestUtils.TestIacToken.token();

    SeparatedList<Tree, IacToken> resultingSeparatedList = SeparatedListImpl.separatedList(Optional.of(separator1), element1, Optional.absent());

    assertThat(resultingSeparatedList.elements()).containsExactly(element1);
    assertThat(resultingSeparatedList.separators()).containsExactly(separator1);
    assertThat(resultingSeparatedList.elementsAndSeparators()).containsExactly(separator1, element1);
  }

  @Test
  void shouldHandleSeparatedListStartingWithOptionalSeparatorAndAdditionalValues() {
    Tree element1 = CommonTestUtils.TestTree.tree();
    IacToken separator1 = CommonTestUtils.TestIacToken.token();
    Tree element2 = CommonTestUtils.TestTree.tree();
    IacToken separator2 = CommonTestUtils.TestIacToken.token();

    SeparatedList<Tree, IacToken> resultingSeparatedList = SeparatedListImpl.separatedList(Optional.of(separator1), element1, Optional.of(List.of(
      new Tuple<>(separator2, element2))));

    assertThat(resultingSeparatedList.elements()).containsExactly(element1, element2);
    assertThat(resultingSeparatedList.separators()).containsExactly(separator1, separator2);
    assertThat(resultingSeparatedList.elementsAndSeparators()).containsExactly(separator1, element1, separator2, element2);
  }

  @Test
  void shouldHandleSeparatedListStartingWithOptionalSeparatorWithNoValue() {
    Tree element1 = CommonTestUtils.TestTree.tree();

    SeparatedList<Tree, IacToken> resultingSeparatedList = SeparatedListImpl.separatedList(Optional.absent(), element1, Optional.absent());

    assertThat(resultingSeparatedList.elements()).containsExactly(element1);
    assertThat(resultingSeparatedList.separators()).isEmpty();
    assertThat(resultingSeparatedList.elementsAndSeparators()).containsExactly(element1);
  }

  @Test
  void shouldConvertToString() {
    Tree element1 = CommonTestUtils.TestTree.tree();
    IacToken separator1 = CommonTestUtils.TestIacToken.token();
    Tree element2 = CommonTestUtils.TestTree.tree();
    IacToken separator2 = CommonTestUtils.TestIacToken.token();
    Tree element3 = CommonTestUtils.TestTree.tree();

    SeparatedList<Tree, IacToken> separatedList = SeparatedListImpl.separatedList(element1, Optional.of(List.of(
      new Tuple<>(separator1, element2),
      new Tuple<>(separator2, element3))));

    String expected = element1 + " " + separator1 + " " + element2 + " " + separator2 + " " + element3;
    assertThat(separatedList).hasToString(expected);
  }
}
