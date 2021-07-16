/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.common.checks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.AbstractTestTree;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.HasProperties;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.api.tree.Tree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.checks.PropertyUtilsTest.TestProperty.attribute;
import static org.sonar.iac.common.checks.PropertyUtilsTest.TestTree.tree;
import static org.sonar.iac.common.checks.TextUtilsTest.TestTextTree.text;

class PropertyUtilsTest {

  Tree value1 = text("value1");
  Tree value2 = text("value2");
  TextTree key2 = text("key2");
  PropertyTree attribute2 = attribute(key2, value2);
  TestTree tree = tree(attribute("key1", value1), attribute2);

  @Test
  void has() {
    assertThat(PropertyUtils.has(tree, "key2")).isEqualTo(Trilean.TRUE);
    assertThat(PropertyUtils.has(tree, "key3")).isEqualTo(Trilean.FALSE);

    tree.addElement(attribute((Tree) null, value2));
    assertThat(PropertyUtils.has(tree, "key3")).isEqualTo(Trilean.UNKNOWN);
    assertThat(PropertyUtils.has(null, "key2")).isEqualTo(Trilean.FALSE);
  }

  @Test
  void get() {
    assertThat(PropertyUtils.get(tree, "key2")).isPresent().get().isEqualTo(attribute2);
    assertThat(PropertyUtils.get(tree, "key3")).isNotPresent();
    assertThat(PropertyUtils.get(null, "key3")).isNotPresent();
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

  static class OtherTree extends AbstractTestTree {

  }

  static class TestProperty implements PropertyTree {

    private final Tree key;
    private final Tree value;

    public TestProperty(Tree key, Tree value) {
      this.key = key;
      this.value = value;
    }

    static PropertyTree attribute(Tree key, Tree value) {
      return new TestProperty(key, value);
    }

    static PropertyTree attribute(String key, Tree value) {
      return attribute(text(key), value);
    }

    @Override
    public Tree key() {
      return key;
    }

    @Override
    public Tree value() {
      return value;
    }
  }

  static class TestTree extends AbstractTestTree implements HasProperties {

    private final List<PropertyTree> attributes = new ArrayList<>();

    private TestTree(PropertyTree... attributes) {
      this.attributes.addAll(Arrays.asList(attributes));
    }

    static TestTree tree(PropertyTree... attributes) {
      return new TestTree(attributes);
    }

    @Override
    public List<PropertyTree> attributes() {
      return attributes;
    }

    public void addElement(PropertyTree element) {
      attributes.add(element);
    }
  }
}
