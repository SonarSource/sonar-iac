/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.IacToken;
import org.sonar.iac.common.api.tree.SeparatedList;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.CommonTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class SeparatedListImplTest {

  @Test
  void emptySeparatedListProducesEmptyLists() {
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
  void separatedListWithNullSeparator() {
    Tree firstElement = CommonTestUtils.TestTree.tree();
    Tree tupleElement = CommonTestUtils.TestTree.tree();

    SeparatedList<Tree, IacToken> resultingSeparatedList = SeparatedListImpl.separatedList(firstElement, Optional.of(List.of(new Tuple<>(null, tupleElement))));

    assertThat(resultingSeparatedList.elements()).containsExactly(firstElement, tupleElement);
    assertThat(resultingSeparatedList.separators()).isEmpty();
    assertThat(resultingSeparatedList.elementsAndSeparators()).containsExactly(firstElement, tupleElement);
  }

  @Test
  void separatedListWithBothSeparatorAndNullSeparator() {
    Tree firstElement = CommonTestUtils.TestTree.tree();
    IacToken firstSeparator = CommonTestUtils.TestIacToken.token();
    Tree tupleElement1 = CommonTestUtils.TestTree.tree();
    Tree tupleElement2 = CommonTestUtils.TestTree.tree();

    SeparatedList<Tree, IacToken> resultingSeparatedList = SeparatedListImpl.separatedList(firstElement, Optional.of(List.of(
      new Tuple<>(firstSeparator, tupleElement1),
      new Tuple<>(null, tupleElement2))));

    assertThat(resultingSeparatedList.elements()).containsExactly(firstElement, tupleElement1, tupleElement2);
    assertThat(resultingSeparatedList.separators()).containsExactly(firstSeparator);
    assertThat(resultingSeparatedList.elementsAndSeparators()).containsExactly(firstElement, firstSeparator, tupleElement1, tupleElement2);
  }

  @Test
  void absentOptionalShouldRetrieveSeparatedListWithOnlyOneElement() {
    Tree firstElement = CommonTestUtils.TestTree.tree();

    SeparatedList<Tree, IacToken> resultingSeparatedList = SeparatedListImpl.separatedList(firstElement, Optional.absent());

    assertThat(resultingSeparatedList.elements()).containsExactly(firstElement);
    assertThat(resultingSeparatedList.separators()).isEmpty();
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
