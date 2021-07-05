/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
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
import org.sonar.iac.common.checks.Trilean;

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

  @Test
  void test_has_value() {
    List<TupleTree> elements = new ArrayList<>();
    elements.add(new TupleTreeImpl(scalar("key1"), scalar("value1"), range));
    MappingTree mapping = new MappingTreeImpl(elements, "tag", range, Collections.emptyList());

    assertThat(MappingTreeUtils.hasValue(mapping, "key1")).isEqualTo(Trilean.TRUE);
    assertThat(MappingTreeUtils.hasValue(mapping, "key2")).isEqualTo(Trilean.FALSE);
    assertThat(MappingTreeUtils.hasValue(scalar("key1"), "key1")).isEqualTo(Trilean.UNKNOWN);
  }

  private static ScalarTreeImpl scalar(String value) {
    return new ScalarTreeImpl(value, ScalarTree.Style.DOUBLE_QUOTED, "tag", range, Collections.emptyList());
  }
}
