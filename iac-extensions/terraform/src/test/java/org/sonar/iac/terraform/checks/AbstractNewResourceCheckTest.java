/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.terraform.checks;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.BlockTree;

class AbstractNewResourceCheckTest {

  @Test
  void test() {
    TerraformVerifier.verify("newAbstractResourceCheckTest.tf", new TestAbstractNewResourceCheck());
  }

  static class TestAbstractNewResourceCheck extends AbstractNewResourceCheck {

    @Override
    protected void registerResourceConsumer() {
      // Check reportIfAbsent on AttributeSymbol
      register("missing_attribute",
        resource -> resource.attribute("expected_attribute").reportIfAbsent("attribute is missing"));

      // Check reportIfAbsent on BlockSymbol
      register("missing_block",
        resource -> resource.block("expected_block").reportIfAbsent("block is missing"));

      // Check reportIf on AttributeSymbol
      register("attribute_reportIf",
        resource -> resource.attribute("attribute")
          .reportIf(expr -> TextUtils.isValue(expr, "expected_value").isFalse(), "attribute has not expected value"));
    }

    @Override
    protected void provideResource(CheckContext ctx, BlockTree blockTree) {
      super.provideResource(ctx, blockTree);
      if (isResource(blockTree) && resourceType(blockTree) == null) {
        ctx.reportIssue(blockTree, "missing resource type");
      }
    }
  }
}
