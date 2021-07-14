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
import org.sonar.iac.common.api.tree.AttributeTree;
import org.sonar.iac.common.api.tree.HasAttributes;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.api.tree.Tree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.checks.AttributeUtilsTest.TestAttribute.attribute;
import static org.sonar.iac.common.checks.AttributeUtilsTest.TestTree.tree;
import static org.sonar.iac.common.checks.TextUtilsTest.TestTextTree.text;

class AttributeUtilsTest {

  Tree value1 = text("value1");
  Tree value2 = text("value2");
  TextTree key2 = text("key2");
  AttributeTree attribute2 = attribute(key2, value2);
  TestTree tree = tree(attribute("key1", value1), attribute2);

  @Test
  void has() {
    assertThat(AttributeUtils.has(tree, "key2")).isEqualTo(Trilean.TRUE);
    assertThat(AttributeUtils.has(tree, "key3")).isEqualTo(Trilean.FALSE);

    tree.addElement(attribute((Tree) null, value2));
    assertThat(AttributeUtils.has(tree, "key3")).isEqualTo(Trilean.UNKNOWN);
    assertThat(AttributeUtils.has(null, "key2")).isEqualTo(Trilean.FALSE);
  }

  @Test
  void get() {
    assertThat(AttributeUtils.get(tree, "key2")).isPresent().get().isEqualTo(attribute2);
    assertThat(AttributeUtils.get(tree, "key3")).isNotPresent();
    assertThat(AttributeUtils.get(null, "key3")).isNotPresent();
  }

  @Test
  void valueOrNull() {
    assertThat(AttributeUtils.valueOrNull(tree, "key2")).isEqualTo(value2);
    assertThat(AttributeUtils.valueOrNull(tree, "key3")).isNull();
    assertThat(AttributeUtils.valueOrNull(null, "key3")).isNull();
    assertThat(AttributeUtils.valueOrNull(tree, "key2", TextTree.class)).isEqualTo(value2);
    assertThat(AttributeUtils.valueOrNull(tree, "key2", OtherTree.class)).isNull();
  }

  @Test
  void key() {
    assertThat(AttributeUtils.key(tree, "key2")).isPresent().get().isEqualTo(key2);
    assertThat(AttributeUtils.key(tree, "key3")).isNotPresent();
    assertThat(AttributeUtils.key(null, "key3")).isNotPresent();
  }

  @Test
  void value() {
    assertThat(AttributeUtils.value(tree, "key2")).isPresent().get().isEqualTo(value2);
    assertThat(AttributeUtils.value(tree, "key3")).isNotPresent();
    assertThat(AttributeUtils.value(null, "key3")).isNotPresent();
    assertThat(AttributeUtils.value(tree, "key2", TextTree.class)).isPresent().get().isEqualTo(value2);
    assertThat(AttributeUtils.value(tree, "key2", OtherTree.class)).isNotPresent();
  }

  static class OtherTree extends AbstractTestTree {

  }

  static class TestAttribute implements AttributeTree {

    private final Tree key;
    private final Tree value;

    public TestAttribute(Tree key, Tree value) {
      this.key = key;
      this.value = value;
    }

    static AttributeTree attribute(Tree key, Tree value) {
      return new TestAttribute(key, value);
    }

    static AttributeTree attribute(String key, Tree value) {
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

  static class TestTree extends AbstractTestTree implements HasAttributes {

    private final List<AttributeTree> attributes = new ArrayList<>();

    private TestTree(AttributeTree... attributes) {
      this.attributes.addAll(Arrays.asList(attributes));
    }

    static TestTree tree(AttributeTree... attributes) {
      return new TestTree(attributes);
    }

    @Override
    public List<AttributeTree> attributes() {
      return attributes;
    }

    public void addElement(AttributeTree element) {
      attributes.add(element);
    }
  }
}
