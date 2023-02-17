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
package org.sonar.iac.cloudformation.checks;

import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.common.yaml.tree.YamlTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.cloudformation.checks.utils.XPathUtils;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.InitContext;

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
  protected void checkResource(CheckContext ctx, Resource resource) {
    YamlTree properties = resource.properties();
    if (properties != null) {
      getTagKeyStream(properties)
        .filter(this::isMismatchingKey)
        .forEach(key -> ctx.reportIssue(key, String.format(MESSAGE, key.value(), format)));
    }
  }

  private static Stream<ScalarTree> getTagKeyStream(YamlTree properties) {
    return XPathUtils.getTrees(properties, "/Tags[]/Key").stream()
      .filter(ScalarTree.class::isInstance).map(ScalarTree.class::cast);
  }

  private boolean isMismatchingKey(ScalarTree tagKey) {
    return !pattern.matcher(tagKey.value()).matches();
  }
}
