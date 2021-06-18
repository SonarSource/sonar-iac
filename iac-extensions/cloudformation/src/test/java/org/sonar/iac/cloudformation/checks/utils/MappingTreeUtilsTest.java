/*
 * SonarQube IaC Plugin
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
package org.sonar.iac.cloudformation.checks.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.cloudformation.api.tree.TupleTree;
import org.sonar.iac.cloudformation.tree.impl.MappingTreeImpl;
import org.sonar.iac.cloudformation.tree.impl.ScalarTreeImpl;
import org.sonar.iac.cloudformation.tree.impl.TupleTreeImpl;
import org.sonar.iac.common.api.tree.impl.TextRanges;

import static org.assertj.core.api.Assertions.assertThat;

class MappingTreeUtilsTest {
  private static final TextRange range = TextRanges.range(1, 1, 1, 1);

  @Test
  void test_get_value() {
    List<TupleTree> elements = new ArrayList<>();
    ScalarTreeImpl expectedValue = scalar("value2");
    elements.add(new TupleTreeImpl(scalar("key1"), scalar("value1"), range));
    elements.add(new TupleTreeImpl(scalar("key2"), expectedValue, range));
    MappingTree mapping = new MappingTreeImpl(elements, "tag", range, Collections.emptyList());

    assertThat(MappingTreeUtils.getValue(mapping, "key2")).isPresent().get().isInstanceOf(ScalarTree.class).isEqualTo(expectedValue);
    assertThat(MappingTreeUtils.getValue(mapping, "unknown")).isNotPresent();
  }

  private static ScalarTreeImpl scalar(String value) {
    return new ScalarTreeImpl(value, ScalarTree.Style.DOUBLE_QUOTED, "tag", range, Collections.emptyList());
  }
}
