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
package org.sonar.iac.common.checks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.sonar.iac.common.AbstractTestTree;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.HasProperties;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.CommentImpl;

import static org.sonar.iac.common.checks.CommonTestUtils.TestTextTree.text;

public class CommonTestUtils {

  private CommonTestUtils() {

  }

  public static class TestTree extends AbstractTestTree {

    public static Tree tree() {
      return new TestTree();
    }
  }

  public static class TestTextTree extends AbstractTestTree implements TextTree {

    private final String value;
    private final List<Comment> comments;

    public static TextTree text(String value) {
      return new TestTextTree(value, Collections.emptyList());
    }

    public static TextTree text(String value, Comment comment) {
      return new TestTextTree(value, List.of(comment));
    }

    public TestTextTree(String value, List<Comment> comments) {
      this.value = value;
      this.comments = comments;
    }

    @Override
    public String value() {
      return value;
    }

    @Override
    public List<Comment> comments() {
      return comments;
    }
  }

  public static class TestAttributeTree extends AbstractTestTree implements PropertyTree {

    private final Tree key;
    private final Tree value;

    public TestAttributeTree(Tree key, Tree value) {
      this.key = key;
      this.value = value;
    }

    public static PropertyTree attribute(Tree key, Tree value) {
      return new TestAttributeTree(key, value);
    }

    public static PropertyTree attribute(String key, Tree value) {
      return attribute(text(key), value);
    }

    public static PropertyTree attribute(String key, String value) {
      return attribute(text(key), text(value));
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

  public static class TestPropertiesTree extends AbstractTestTree implements HasProperties {

    private final List<PropertyTree> attributes = new ArrayList<>();

    private TestPropertiesTree(PropertyTree... attributes) {
      this.attributes.addAll(Arrays.asList(attributes));
    }

    public static TestPropertiesTree properties(PropertyTree... attributes) {
      return new TestPropertiesTree(attributes);
    }

    @Override
    public List<PropertyTree> properties() {
      return attributes;
    }

    public void addElement(PropertyTree element) {
      attributes.add(element);
    }
  }

  public static class TestIterable extends AbstractTestTree implements Iterable<Tree> {

    final List<Tree> elements;

    private TestIterable(List<Tree> elements) {
      this.elements = elements;
    }

    public static TestIterable list(Tree... element) {
      return new TestIterable(Arrays.asList(element));
    }

    @Override
    public Iterator<Tree> iterator() {
      return elements.iterator();
    }
  }
}
