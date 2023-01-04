/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.AbstractTestTree;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.CommonTestUtils.TestAttributeTree;
import org.sonar.iac.common.checks.CommonTestUtils.TestPropertiesTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.checks.CommonTestUtils.TestAttributeTree.attribute;
import static org.sonar.iac.common.checks.CommonTestUtils.TestPropertiesTree.properties;
import static org.sonar.iac.common.checks.CommonTestUtils.TestTextTree.text;

class PropertyUtilsTest {

  TextTree value1 = text("value1");
  TextTree value2 = text("value2");
  TextTree key2 = text("key2");
  PropertyTree attribute2 = attribute(key2, value2);
  TestPropertiesTree tree = properties(attribute("key1", value1), attribute2);

  @Test
  void has() {
    assertThat(PropertyUtils.has(tree, "key2")).isEqualTo(Trilean.TRUE);
    assertThat(PropertyUtils.has(tree, "key3")).isEqualTo(Trilean.FALSE);

    tree.addElement(attribute((Tree) null, value2));
    assertThat(PropertyUtils.has(tree, "key3")).isEqualTo(Trilean.UNKNOWN);
    assertThat(PropertyUtils.has(null, "key2")).isEqualTo(Trilean.FALSE);
  }

  @Test
  void valueIs() {
    assertThat(PropertyUtils.valueIs(tree, "key2", tree -> tree.equals(attribute2.value()))).isTrue();
    assertThat(PropertyUtils.valueIs(tree, "key1", tree -> tree.equals(attribute2.value()))).isFalse();
    assertThat(PropertyUtils.valueIs(tree, "key3", tree -> tree.equals(attribute2.value()))).isFalse();
  }

  @Test
  void get() {
    assertThat(PropertyUtils.get(tree, "key2")).isPresent().get().isEqualTo(attribute2);
    assertThat(PropertyUtils.get(tree, "key3")).isNotPresent();
    assertThat(PropertyUtils.get(null, "key3")).isNotPresent();
    assertThat(PropertyUtils.get(tree, "key2", TestAttributeTree.class)).isPresent();
    assertThat(PropertyUtils.get(tree, "key2", OtherTree.class)).isNotPresent();
  }

  @Test
  void valueOrNull() {
    assertThat(PropertyUtils.valueOrNull(tree, "key2")).isEqualTo(value2);
    assertThat(PropertyUtils.valueOrNull(tree, "key3")).isNull();
    assertThat(PropertyUtils.valueOrNull(null, "key3")).isNull();
    assertThat(PropertyUtils.valueOrNull(tree, "key2", TextTree.class)).isEqualTo(value2);
    assertThat(PropertyUtils.valueOrNull(tree, "key2", OtherTree.class)).isNull();
  }

  @Test
  void key() {
    assertThat(PropertyUtils.key(tree, "key2")).isPresent().get().isEqualTo(key2);
    assertThat(PropertyUtils.key(tree, "key3")).isNotPresent();
    assertThat(PropertyUtils.key(null, "key3")).isNotPresent();
  }

  @Test
  void value() {
    assertThat(PropertyUtils.value(tree, "key2")).isPresent().get().isEqualTo(value2);
    assertThat(PropertyUtils.value(tree, "key3")).isNotPresent();
    assertThat(PropertyUtils.value(null, "key3")).isNotPresent();
    assertThat(PropertyUtils.value(tree, "key2", TextTree.class)).isPresent().get().isEqualTo(value2);
    assertThat(PropertyUtils.value(tree, "key2", OtherTree.class)).isNotPresent();
  }

  @Test
  void value_with_predicate() {
    assertThat(PropertyUtils.value(tree, k -> k.equals("key1"))).isPresent().get().isEqualTo(value1);
    assertThat(PropertyUtils.value(tree, k -> k.equals("key2"))).isPresent().get().isEqualTo(value2);

    // return the first match only
    assertThat(PropertyUtils.value(tree, k -> k.contains("key"))).isPresent().get().isEqualTo(value1);

    assertThat(PropertyUtils.value(null, k -> k.contains("key"))).isNotPresent();
    assertThat(PropertyUtils.value(tree, k -> k.equals("key3"))).isNotPresent();
  }

  @Test
  void getAll() {
    PropertyTree property1 = attribute("key", value1);
    PropertyTree property2 = attribute("key", value2);
    TestPropertiesTree testTree = properties(property1, property2);
    assertThat(PropertyUtils.getAll(testTree, "key")).containsExactly(property1, property2);

    assertThat(PropertyUtils.getAll(testTree, "key", TestAttributeTree.class)).hasSize(2);
    assertThat(PropertyUtils.getAll(testTree, "key", OtherTree.class)).isEmpty();

    assertThat(PropertyUtils.getAll(testTree, TestAttributeTree.class)).hasSize(2);
    assertThat(PropertyUtils.getAll(testTree, OtherTree.class)).isEmpty();
  }

  @Test
  void missing() {
    assertThat(PropertyUtils.isMissing(tree, "key1")).isFalse();
    assertThat(PropertyUtils.isMissing(tree, "unknownKey")).isTrue();
  }

  static class OtherTree extends AbstractTestTree {

  }
}
