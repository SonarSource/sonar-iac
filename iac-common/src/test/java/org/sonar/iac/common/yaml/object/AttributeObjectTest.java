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
package org.sonar.iac.common.yaml.object;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.yaml.YamlTreeTest;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

import static org.assertj.core.api.Assertions.assertThat;

class AttributeObjectTest extends YamlTreeTest {

  static final List<TestIssue> raisedIssues = new ArrayList<>();
  CheckContext checkContext = new TestContext();

  @BeforeEach
  public void init() {
    raisedIssues.clear();
  }

  @Test
  void testFromPresent() {
    TupleTree tree = parseTuple("a: b");
    AttributeObject attributeObjectStatusPresent = AttributeObject.fromPresent(checkContext, tree, "a");
    assertThat(attributeObjectStatusPresent.key).isEqualTo("a");
    assertThat(attributeObjectStatusPresent.status).isEqualTo(YamlObject.Status.PRESENT);
    assertThat(attributeObjectStatusPresent.tree).isEqualTo(tree);
    assertThat(attributeObjectStatusPresent.ctx).isEqualTo(checkContext);
  }

  @Test
  void testFromPresentUnknown() {
    YamlTree tree = parse("a:b", YamlTree.class);
    AttributeObject attributeObjectStatusUnknown = AttributeObject.fromPresent(checkContext, tree, "a");
    assertThat(attributeObjectStatusUnknown.key).isEqualTo("a");
    assertThat(attributeObjectStatusUnknown.status).isEqualTo(YamlObject.Status.UNKNOWN);
    assertThat(attributeObjectStatusUnknown.tree).isNull();
    assertThat(attributeObjectStatusUnknown.ctx).isEqualTo(checkContext);
  }

  @Test
  void testFromAbsent() {
    AttributeObject attributeObjectStatusAbsent = AttributeObject.fromAbsent(checkContext, "a");
    assertThat(attributeObjectStatusAbsent.key).isEqualTo("a");
    assertThat(attributeObjectStatusAbsent.status).isEqualTo(YamlObject.Status.ABSENT);
    assertThat(attributeObjectStatusAbsent.tree).isNull();
    assertThat(attributeObjectStatusAbsent.ctx).isEqualTo(checkContext);
  }

  @Test
  void testReportIfValue() {
    TupleTree tree = parseTuple("a: b");
    AttributeObject attributeObjectStatusPresent = AttributeObject.fromPresent(checkContext, tree, "a");
    attributeObjectStatusPresent.reportIfValue(t -> true, "message");
    assertThat(raisedIssues).hasSize(1);
    TestIssue issue = raisedIssues.get(0);
    assertThat(issue.message).isEqualTo("message");
    assertThat(issue.secondaryLocations).isEmpty();
    assertThat(issue.textRange).isEqualTo(tree.value().textRange());
  }

  @Test
  void reportIfAbsentShouldReportIssueOnAbsentObject() {
    TupleTree tree = parseTuple("a: b");
    AttributeObject attributeObjectStatusPresent = AttributeObject.fromAbsent(checkContext, "b");
    attributeObjectStatusPresent.reportIfAbsent(tree.metadata(), "message");
    assertThat(raisedIssues).hasSize(1);
    TestIssue issue = raisedIssues.get(0);
    assertThat(issue.message).isEqualTo("message");
    assertThat(issue.secondaryLocations).isEmpty();
    assertThat(issue.textRange).isEqualTo(tree.textRange());
  }

  @Test
  void reportIfAbsentShouldNotReportIssueOnPresentObject() {
    TupleTree tree = parseTuple("a: b");
    AttributeObject attributeObjectStatusPresent = AttributeObject.fromPresent(checkContext, tree, "b");
    attributeObjectStatusPresent.reportIfAbsent(tree.metadata(), "message");
    assertThat(raisedIssues).isEmpty();
  }

  @Test
  void shouldReportOnKey() {
    TupleTree tree = parseTuple("a: b");
    AttributeObject attributeObject = AttributeObject.fromPresent(checkContext, tree, "a");
    attributeObject.reportOnKey("message");

    assertThat(raisedIssues).hasSize(1);
    TestIssue issue = raisedIssues.get(0);
    assertThat(issue.message).isEqualTo("message");
    assertThat(issue.secondaryLocations).isEmpty();
    assertThat(issue.textRange).isEqualTo(tree.key().textRange());
  }

  @Test
  void shouldNotReportOnKeyForMissingTree() {
    AttributeObject attributeObject = AttributeObject.fromAbsent(checkContext, "resources");
    attributeObject.reportOnKey("message");

    assertThat(raisedIssues).isEmpty();
  }

  @Test
  void shouldReportOnValue() {
    TupleTree tree = parseTuple("a: b");
    AttributeObject attributeObject = AttributeObject.fromPresent(checkContext, tree, "a");
    attributeObject.reportOnValue("message");

    assertThat(raisedIssues).hasSize(1);
    TestIssue issue = raisedIssues.get(0);
    assertThat(issue.message).isEqualTo("message");
    assertThat(issue.secondaryLocations).isEmpty();
    assertThat(issue.textRange).isEqualTo(tree.value().textRange());
  }

  @Test
  void shouldNotReportOnValueForMissingTree() {
    AttributeObject attributeObject = AttributeObject.fromAbsent(checkContext, "resources");
    attributeObject.reportOnValue("message");

    assertThat(raisedIssues).isEmpty();
  }

  private static class TestContext implements CheckContext {

    @Override
    public void reportIssue(TextRange textRange, String message) {
      raisedIssues.add(new TestIssue(textRange, message, Collections.emptyList()));
    }

    @Override
    public void reportIssue(HasTextRange toHighlight, String message) {
      reportIssue(toHighlight, message, Collections.emptyList());
    }

    @Override
    public void reportIssue(HasTextRange toHighlight, String message, SecondaryLocation secondaryLocation) {
      reportIssue(toHighlight, message, List.of(secondaryLocation));
    }

    @Override
    public void reportIssue(HasTextRange toHighlight, String message, List<SecondaryLocation> secondaryLocations) {
      raisedIssues.add(new AttributeObjectTest.TestIssue(toHighlight.textRange(), message, secondaryLocations));
    }
  }

  private static class TestIssue {

    final TextRange textRange;
    final String message;
    final List<SecondaryLocation> secondaryLocations;

    private TestIssue(TextRange textRange, String message, List<SecondaryLocation> secondaryLocations) {
      this.textRange = textRange;
      this.message = message;
      this.secondaryLocations = secondaryLocations;
    }
  }

}
