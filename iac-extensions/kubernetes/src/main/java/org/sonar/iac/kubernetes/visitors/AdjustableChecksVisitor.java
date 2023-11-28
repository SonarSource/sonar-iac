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
package org.sonar.iac.kubernetes.visitors;

import java.util.Collection;
import java.util.Objects;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.rule.RuleKey;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;

public class AdjustableChecksVisitor extends TreeVisitor<InputFileContext> {

  private final Checks<IacCheck> checks;
  private final DurationStatistics statistics;
  private final LocationShifter locationShifter;

  public AdjustableChecksVisitor(Checks<IacCheck> checks, DurationStatistics statistics, LocationShifter locationShifter) {
    this.checks = checks;
    this.statistics = statistics;
    this.locationShifter = locationShifter;
  }

  public void initialize() {
    Collection<IacCheck> activeChecks = checks.all();
    for (IacCheck check : activeChecks) {
      var ruleKey = checks.ruleKey(check);
      Objects.requireNonNull(ruleKey);
      check.initialize(context(ruleKey));
    }
  }

  protected InitContext context(RuleKey ruleKey) {
    return new AdaptableContextAdapter(this, statistics, ruleKey, locationShifter);
  }
}
