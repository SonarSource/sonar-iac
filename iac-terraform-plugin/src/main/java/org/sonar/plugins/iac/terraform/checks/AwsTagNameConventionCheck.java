/*
 * SonarQube IaC Terraform Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.plugins.iac.terraform.checks;

import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.iac.terraform.api.checks.CheckContext;
import org.sonar.plugins.iac.terraform.api.checks.IacCheck;
import org.sonar.plugins.iac.terraform.api.checks.InitContext;
import org.sonar.plugins.iac.terraform.api.tree.AttributeTree;
import org.sonar.plugins.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.plugins.iac.terraform.api.tree.ObjectElementTree;
import org.sonar.plugins.iac.terraform.api.tree.ObjectTree;
import org.sonar.plugins.iac.terraform.api.tree.Tree;

@Rule(key = "S6273")
public class AwsTagNameConventionCheck implements IacCheck {

  private static final String MESSAGE = "Rename tag key \"%s\" to match the regular expression \"%s\".";
  public static final String DEFAULT = "^([A-Z][A-Za-z]*:)*([A-Z][A-Za-z]*)$";
  private Pattern pattern;

  @RuleProperty(
    key = "format",
    description = "Regular expression used to check the tag keys against.",
    defaultValue = DEFAULT)
  String format = DEFAULT;

  @Override
  public void initialize(InitContext init) {
    pattern = Pattern.compile(format);
    init.register(AttributeTree.class, (ctx, tree) -> {
      if ("tags".equals(tree.name().value()) && tree.value() instanceof ObjectTree) {
        check(ctx, (ObjectTree) tree.value());
      }
    });
  }

  private void check(CheckContext ctx, ObjectTree tree) {
    tree.elements().trees().stream()
      .map(ObjectElementTree::name)
      .filter(i -> i.is(Tree.Kind.STRING_LITERAL))
      .forEach(i -> {
        String value = ((LiteralExprTree) i).value();
        if (!pattern.matcher(value).matches()) {
          ctx.reportIssue(i, String.format(MESSAGE, value, format));
        }
      });
  }
}
