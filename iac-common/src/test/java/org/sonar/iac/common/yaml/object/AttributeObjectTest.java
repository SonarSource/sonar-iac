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
package org.sonar.iac.common.yaml.object;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.yaml.YamlTreeTest;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

import static org.assertj.core.api.Assertions.assertThat;

class AttributeObjectTest extends YamlTreeTest {

  static final List<TestIssue> raisedIssues = new ArrayList<>();
  CheckContext ctx = new TestContext();

  @Test
  void fromPresent() {
    TupleTree tree = parseTuple("a: b");
    AttributeObject attr = AttributeObject.fromPresent(ctx, tree, "a");
    assertThat(attr.key).isEqualTo("a");
    assertThat(attr.status).isEqualTo(YamlObject.Status.PRESENT);
    assertThat(attr.tree).isEqualTo(tree);
    assertThat(attr.ctx).isEqualTo(ctx);
  }

  @Test
  void fromPresent_unknown() {
    YamlTree tree = parse("a:b", YamlTree.class);
    AttributeObject attr = AttributeObject.fromPresent(ctx, tree, "a");
    assertThat(attr.key).isEqualTo("a");
    assertThat(attr.status).isEqualTo(YamlObject.Status.UNKNOWN);
    assertThat(attr.tree).isNull();
    assertThat(attr.ctx).isEqualTo(ctx);
  }

  @Test
  void fromAbsent() {
    AttributeObject attr = AttributeObject.fromAbsent(ctx,"a");
    assertThat(attr.key).isEqualTo("a");
    assertThat(attr.status).isEqualTo(YamlObject.Status.ABSENT);
    assertThat(attr.tree).isNull();
    assertThat(attr.ctx).isEqualTo(ctx);
  }

  @Test
  void reportIfValue() {
    TupleTree tree = parseTuple("a: b");
    AttributeObject attr = AttributeObject.fromPresent(ctx, tree, "a");
    attr.reportIfValue(t -> true, "message");
    assertThat(raisedIssues).hasSize(1);
    TestIssue issue = raisedIssues.get(0);
    assertThat(issue.message).isEqualTo("message");
    assertThat(issue.secondaryLocations).isEmpty();
    assertThat(issue.textRange).isEqualTo(tree.textRange());
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
