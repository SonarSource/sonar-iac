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
package org.sonar.iac.kubernetes.checks;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.helm.tree.api.GoTemplateTree;
import org.sonar.iac.helm.tree.api.PipeNode;
import org.sonar.iac.helm.tree.api.VariableNode;
import org.sonar.iac.kubernetes.visitors.KubernetesCheckContext;

@Rule(key = "S117")
public class VariableNameConventionCheck implements IacCheck {
  private static final String MESSAGE = "Rename this variable \"%s\" to match the regular expression '%s'.";
  private static final String DEFAULT_FORMAT = "^\\$([a-z][a-zA-Z0-9]*)?$";

  @RuleProperty(
    key = "format",
    description = "Regular expression used to check the names of variables and parameters against.",
    defaultValue = DEFAULT_FORMAT)
  public String format = DEFAULT_FORMAT;
  private Pattern pattern = Pattern.compile(format);
  private final Set<String> checkedNames = new HashSet<>();

  @Override
  public void initialize(InitContext init) {
    this.pattern = Pattern.compile(format);
    init.register(GoTemplateTree.class, (ctx, tree) -> this.checkedNames.clear());
    init.register(PipeNode.class, (ctx, node) -> node.declarations().forEach(variable -> checkVariable(ctx, variable)));
  }

  private void checkVariable(CheckContext ctx, VariableNode variable) {
    // Variables in pipelines should always have at least one identifier, even if it's just a `$` sign.
    // However, initialization is indistinguishable from reassignment, so we check it additionally.
    var name = variable.idents().get(0);
    if (checkedNames.contains(name)) {
      return;
    }
    if (!pattern.matcher(name).matches()) {
      ((KubernetesCheckContext) ctx).reportIssueNoLineShift(variable.textRange(), MESSAGE.formatted(name, format));
    }
    checkedNames.add(name);
  }
}
