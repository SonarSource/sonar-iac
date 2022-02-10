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
package org.sonar.iac.terraform.checks;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.AttributeAccessTree;

class ResourceVisitorTest {

  @Test
  void test() {
    TerraformVerifier.verify("ResourceVisitor/test.tf", new TestResourceCheck());
  }

  static class TestResourceCheck extends ResourceVisitor {
    @Override
    protected void registerResourceConsumer() {
      register(List.of("my_resource"), this::resourceConsumer);
    }

    private void resourceConsumer(Resource resource) {
      resource.block("my_block").ifPresentOrElse(
        block -> block.report("my_block is present"),
        () -> resource.report("my_block is missing")
      );

      resource.blocks("multi_block").forEach(
        block -> {
          block.attribute("my_attribute_1")
            .reportIfValueMatches("sensitive_value", "my_attribute_1 is sensitive_value")
            .reportIfValueMatches(e -> e instanceof AttributeAccessTree, "my_attribute_1 is a AttributeAccessTree");
          block.attribute("my_attribute_2")
            .reportIfValueDoesNotMatch("expected_value", "my_attribute_2 is not expected_value")
            .reportIfValueDoesNotMatch(e -> e instanceof TextTree, "my_attribute_2 is not a TextTree");
          block.attribute("my_attribute_3")
            .reportIfTrue("my_attribute_3 is true")
            .reportIfFalse("my_attribute_3 is false")
            .reportIfAbsence("%s is missing");
          block.list("my_list_1")
            .reportItemsWhichMatch(item -> TextUtils.isValue(item, "unsafe1").isTrue(), "my_list_1 contains unsafe value");
        }
      );

      resource.list("unkown")
        .streamItemsWhichMatch(e -> TextUtils.isValue(e, "item1").isTrue())
        .forEach(e -> resource.ctx.reportIssue(e, "streamed item"));

      resource.list("my_list")
        .streamItemsWhichMatch(e -> TextUtils.isValue(e, "item1").isTrue())
        .forEach(e -> resource.ctx.reportIssue(e, "streamed item"));
    }
  }

}
