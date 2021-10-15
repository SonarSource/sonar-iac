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
package org.sonar.iac.common.checks;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.api.tree.Tree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.checks.TextUtilsTest.TestTextTree.text;
import static org.sonar.iac.common.checks.TextUtilsTest.TestTree.tree;

class TextUtilsTest {

  @Test
  void getValue() {
    assertThat(TextUtils.getValue(text("foo"))).isPresent().get().isEqualTo("foo");
    assertThat(TextUtils.getValue(tree())).isNotPresent();
    assertThat(TextUtils.getValue(null)).isNotPresent();
  }

  @Test
  void getIntValue() {
    assertThat(TextUtils.getIntValue(text("1"))).isPresent().get().isEqualTo(1);
    assertThat(TextUtils.getIntValue(tree())).isNotPresent();
    assertThat(TextUtils.getIntValue(text("foo"))).isNotPresent();
    assertThat(TextUtils.getIntValue(null)).isNotPresent();
  }

  @Test
  void isValue() {
    assertThat(TextUtils.isValue(text("foo"), "foo")).isEqualTo(Trilean.TRUE);
    assertThat(TextUtils.isValue(text("foo"), "bar")).isEqualTo(Trilean.FALSE);
    assertThat(TextUtils.isValue(tree(), "foo")).isEqualTo(Trilean.UNKNOWN);
    assertThat(TextUtils.isValue(null, "foo")).isEqualTo(Trilean.UNKNOWN);
  }

  @Test
  void matchesValue() {
    assertThat(TextUtils.matchesValue(text("foo"), "foo"::equals)).isEqualTo(Trilean.TRUE);
    assertThat(TextUtils.matchesValue(text("foo"), "bar"::equals)).isEqualTo(Trilean.FALSE);
    assertThat(TextUtils.matchesValue(tree(), k -> k.startsWith("foo"))).isEqualTo(Trilean.UNKNOWN);
    assertThat(TextUtils.matchesValue(null, k -> k.startsWith("foo"))).isEqualTo(Trilean.UNKNOWN);
  }

  @Test
  void isValueTrue() {
    assertThat(TextUtils.isValueTrue(text("true"))).isTrue();
    assertThat(TextUtils.isValueTrue(text("false"))).isFalse();
    assertThat(TextUtils.isValueTrue(tree())).isFalse();
    assertThat(TextUtils.isValueTrue(null)).isFalse();
  }

  @Test
  void isValueFalse() {
    assertThat(TextUtils.isValueFalse(text("false"))).isTrue();
    assertThat(TextUtils.isValueFalse(text("true"))).isFalse();
    assertThat(TextUtils.isValueFalse(tree())).isFalse();
    assertThat(TextUtils.isValueFalse(null)).isFalse();
  }

  static class TestTree implements Tree {

    static Tree tree() {
      return new TestTree();
    }

    @Override
    public TextRange textRange() {
      return null;
    }

    @Override
    public List<Tree> children() {
      return null;
    }
  }

  static class TestTextTree extends TestTree implements TextTree {

    static TextTree text(String value) {
      return new TestTextTree(value);
    }

    private final String value;

    public TestTextTree(String value) {
      this.value = value;
    }

    @Override
    public String value() {
      return value;
    }
  }


}
