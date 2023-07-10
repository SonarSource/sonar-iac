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
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.CommonSyntaxToken;
import org.sonar.iac.common.api.tree.SeparatedList;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.CommonTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class SeparatedListImplTest {

  @Test
  void optionalSeparatedListShouldBeRetrieved() {
    Tree tree = CommonTestUtils.TestTree.tree();
    CommonSyntaxToken token = CommonTestUtils.TestCommonSyntaxToken.token();
    SeparatedListImpl<Tree, CommonSyntaxToken> separatedList = new SeparatedListImpl<>(List.of(tree), List.of(token));

    SeparatedList<Tree, CommonSyntaxToken> resultingSeparatedList = SeparatedListImpl.optionalSeparatedList(Optional.of(separatedList));

    assertThat(resultingSeparatedList).isEqualTo(separatedList);
  }

  @Test
  void absentOptionalShouldRetrieveEmptySeparatedList() {
    SeparatedList<Tree, CommonSyntaxToken> separatedList = SeparatedListImpl.optionalSeparatedList(Optional.absent());

    assertThat(separatedList.separators()).isEmpty();
    assertThat(separatedList.elements()).isEmpty();
  }

  @Test
  void separatedListShouldBeCorrectlyConstructed() {
    Tree firstElement = CommonTestUtils.TestTree.tree();
    Tree tupleElement = CommonTestUtils.TestTree.tree();
    CommonSyntaxToken tupleToken = CommonTestUtils.TestCommonSyntaxToken.token();

    SeparatedList<Tree, CommonSyntaxToken> resultingSeparatedList = SeparatedListImpl.separatedList(firstElement, Optional.of(List.of(new Tuple<>(tupleToken, tupleElement))));

    assertThat(resultingSeparatedList.elements()).containsExactly(firstElement, tupleElement);
    assertThat(resultingSeparatedList.separators()).containsExactly(tupleToken);
    assertThat(resultingSeparatedList.elementsAndSeparators()).containsExactly(firstElement, tupleElement, tupleToken);
  }

  @Test
  void absentOptionalShouldRetrieveSeparatedListWithOnlyOneElement() {
    Tree firstElement = CommonTestUtils.TestTree.tree();

    SeparatedList<Tree, CommonSyntaxToken> resultingSeparatedList = SeparatedListImpl.separatedList(firstElement, Optional.absent());

    assertThat(resultingSeparatedList.elements()).containsExactly(firstElement);
    assertThat(resultingSeparatedList.separators()).isEmpty();
  }
}
