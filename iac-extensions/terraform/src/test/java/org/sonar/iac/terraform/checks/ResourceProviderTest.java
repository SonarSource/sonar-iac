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
import org.sonar.iac.terraform.api.tree.AttributeAccessTree;

class ResourceProviderTest {

  @Test
  void test() {
    TerraformVerifier.verify("ResourceProvider/test.tf", new TestResourceProvider());
  }

  static class TestResourceProvider extends ResourceProvider {
    @Override
    protected void registerResourceConsumer() {
      resourceConsumer(List.of("my_resource"), this::resourceConsumer);
    }

    private void resourceConsumer(Resource resource) {
      resource.block("my_block").ifPresentOrElse(
        block -> block.report("my_block is present"),
        () -> resource.report("my_block is missing")
      );

      resource.blocks("multi_block").forEach(
        block -> {
          block.attribute("my_attribute_1")
            .reportSensitiveValue("sensitive_value", "my_attribute_1 is sensitive_value")
            .reportSensitiveValue(e -> e instanceof AttributeAccessTree, "my_attribute_1 is a AttributeAccessTree");
          block.attribute("my_attribute_2")
            .reportUnexpectedValue("expected_value", "my_attribute_2 is not expected_value")
            .reportUnexpectedValue(e -> e instanceof TextTree, "my_attribute_2 is not a TextTree");
          block.attribute("my_attribute_3")
            .reportOnTrue("my_attribute_3 is true")
            .reportOnFalse("my_attribute_3 is false")
            .reportAbsence("%s is missing");
        }

      );

    }
  }

}
