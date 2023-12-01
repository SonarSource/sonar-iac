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

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.api.rule.RuleKey;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;

public class AdjustableContextAdapter implements InitContext, CheckContext {

  private final TreeVisitor<InputFileContext> treeVisitor;
  private final DurationStatistics statistics;
  public final RuleKey ruleKey;
  private final LocationShifter locationShifter;
  private InputFileContext currentCtx;

  public AdjustableContextAdapter(TreeVisitor<InputFileContext> visitor, DurationStatistics statistics, RuleKey ruleKey, LocationShifter locationShifter) {
    this.treeVisitor = visitor;
    this.statistics = statistics;
    this.ruleKey = ruleKey;
    this.locationShifter = locationShifter;
  }

  @Override
  public <T extends Tree> void register(Class<T> cls, BiConsumer<CheckContext, T> visitor) {
    this.treeVisitor.register(cls, statistics.time(ruleKey.rule(), (InputFileContext ctx, T tree) -> {
      currentCtx = ctx;
      visitor.accept(this, tree);
    }));
  }

  @Override
  public void reportIssue(TextRange textRange, String message) {
    reportIssue(textRange, message, Collections.emptyList());
  }

  @Override
  public void reportIssue(HasTextRange toHighlight, String message) {
    reportIssue(toHighlight.textRange(), message, Collections.emptyList());
  }

  @Override
  public void reportIssue(HasTextRange toHighlight, String message, SecondaryLocation secondaryLocation) {
    reportIssue(toHighlight.textRange(), message, Collections.singletonList(secondaryLocation));
  }

  @Override
  public void reportIssue(HasTextRange toHighlight, String message, List<SecondaryLocation> secondaryLocations) {
    reportIssue(toHighlight.textRange(), message, secondaryLocations);
  }

  private void reportIssue(@Nullable TextRange textRange, String message, List<SecondaryLocation> secondaryLocations) {
    var shiftedTextRange = textRange;
    if (textRange != null) {
      shiftedTextRange = locationShifter.computeShiftedLocation(currentCtx, textRange);
    }
    List<SecondaryLocation> shiftedSecondaryLocations = secondaryLocations.stream().map(this::adaptSecondaryLocation).collect(Collectors.toList());
    currentCtx.reportIssue(ruleKey, shiftedTextRange, message, shiftedSecondaryLocations);
  }

  private SecondaryLocation adaptSecondaryLocation(SecondaryLocation secondaryLocation) {
    var shiftedTextRange = locationShifter.computeShiftedLocation(currentCtx, secondaryLocation.textRange);
    return new SecondaryLocation(shiftedTextRange, secondaryLocation.message);
  }
}
