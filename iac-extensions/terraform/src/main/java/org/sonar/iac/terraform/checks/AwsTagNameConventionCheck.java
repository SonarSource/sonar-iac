/*
 * SonarQube IaC Plugin
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
package org.sonar.iac.terraform.checks;

import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.ObjectElementTree;
import org.sonar.iac.terraform.api.tree.ObjectTree;

@Rule(key = "S6273")
public class AwsTagNameConventionCheck extends AbstractResourceCheck {
  protected static final String MESSAGE = "Rename tag key \"%s\" to match the regular expression \"%s\".";
  public static final String DEFAULT = "^([A-Z][A-Za-z]*:)*([A-Z][A-Za-z]*)$";
  protected Pattern pattern;

  @RuleProperty(
    key = "format",
    description = "Regular expression used to check the tag keys against.",
    defaultValue = DEFAULT)
  public String format = DEFAULT;

  @Override
  public void initialize(InitContext init) {
    pattern = Pattern.compile(format);
    super.initialize(init);
  }

  @Override
  protected void checkResource(CheckContext ctx, BlockTree resource) {
    getTagKeyStream(resource)
      .filter(this::isMismatchingKey)
      .forEach(tagKey -> ctx.reportIssue(tagKey, String.format(MESSAGE, tagKey.value(), format)));
  }

  private static Stream<LiteralExprTree> getTagKeyStream(BlockTree resource) {
    return PropertyUtils.value(resource, "tags", ObjectTree.class)
      .map(o -> o.elements().trees().stream()).orElse(Stream.empty())
      .map(ObjectElementTree::key).filter(LiteralExprTree.class::isInstance).map(LiteralExprTree.class::cast);
  }

  private boolean isMismatchingKey(LiteralExprTree tagKey) {
    return !pattern.matcher(tagKey.value()).matches();
  }
}
