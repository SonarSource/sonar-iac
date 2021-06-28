/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.checks.AbstractAwsTagNameConventionCheck;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.ObjectElementTree;
import org.sonar.iac.terraform.api.tree.ObjectTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;

@Rule(key = "S6273")
public class AwsTagNameConventionCheck extends AbstractAwsTagNameConventionCheck {

  @Override
  public void initialize(InitContext init) {
    pattern = Pattern.compile(format);
    init.register(AttributeTree.class, (ctx, tree) -> {
      if ("tags".equals(tree.identifier().value()) && tree.value() instanceof ObjectTree) {
        check(ctx, (ObjectTree) tree.value());
      }
    });
  }

  private void check(CheckContext ctx, ObjectTree tree) {
    tree.elements().trees().stream()
      .map(ObjectElementTree::name)
      .filter(i -> i.is(TerraformTree.Kind.STRING_LITERAL))
      .forEach(i -> {
        String value = ((LiteralExprTree) i).value();
        if (!pattern.matcher(value).matches()) {
          ctx.reportIssue(i, String.format(MESSAGE, value, format));
        }
      });
  }
}
