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
package org.sonar.iac.kubernetes.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.rule.RuleKey;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.visitors.ChecksVisitor;
import org.sonar.iac.common.extension.visitors.InputFileContext;

public class AdjustableChecksVisitor extends ChecksVisitor {

  /**
   * TODO SONARIAC-1352 Remove property "secondaryLocationsInValuesEnable"
   */
  protected static final String ENABLE_SECONDARY_LOCATIONS_IN_VALUES_YAML_KEY = "sonar.kubernetes.internal.helm.secondaryLocationsInValuesEnable";
  private final SecondaryLocationLocator secondaryLocationLocator;

  public AdjustableChecksVisitor(Checks<IacCheck> checks, DurationStatistics statistics, SecondaryLocationLocator secondaryLocationLocator) {
    super(checks, statistics);
    this.secondaryLocationLocator = secondaryLocationLocator;
  }

  @Override
  protected InitContext context(RuleKey ruleKey) {
    return new AdjustableContextAdapter(ruleKey);
  }

  public class AdjustableContextAdapter extends ContextAdapter implements HelmAwareCheckContext {

    private HelmInputFileContext currentCtx;
    private boolean shouldReportSecondaryInValues;

    public AdjustableContextAdapter(RuleKey ruleKey) {
      super(ruleKey);
    }

    @Override
    public <T extends Tree> void register(Class<T> cls, BiConsumer<CheckContext, T> visitor) {
      AdjustableChecksVisitor.this.register(cls, statistics.time(ruleKey.rule(), (InputFileContext ctx, T tree) -> {
        currentCtx = (HelmInputFileContext) ctx;
        visitor.accept(this, tree);
      }));
    }

    @Override
    protected void reportIssue(@Nullable TextRange textRange, String message, List<SecondaryLocation> secondaryLocations) {
      var shiftedTextRange = textRange;
      List<SecondaryLocation> allSecondaryLocations = new ArrayList<>();
      if (textRange != null) {
        shiftedTextRange = LocationShifter.shiftLocation(currentCtx, textRange);

        boolean isReportingEnabled = currentCtx.sensorContext.config().getBoolean(ENABLE_SECONDARY_LOCATIONS_IN_VALUES_YAML_KEY).orElse(false);
        if (isReportingEnabled || shouldReportSecondaryInValues()) {
          allSecondaryLocations = secondaryLocationLocator.findSecondaryLocationsInAdditionalFiles(currentCtx, shiftedTextRange);
        }
      }
      List<SecondaryLocation> shiftedSecondaryLocations = secondaryLocations.stream()
        .map(secondaryLocation -> LocationShifter.computeShiftedSecondaryLocation(currentCtx, secondaryLocation))
        .toList();

      allSecondaryLocations.addAll(shiftedSecondaryLocations);
      currentCtx.reportIssue(ruleKey, shiftedTextRange, message, allSecondaryLocations);
    }

    @Override
    public boolean shouldReportSecondaryInValues() {
      return this.shouldReportSecondaryInValues;
    }

    @Override
    public void setShouldReportSecondaryInValues(boolean shouldReport) {
      this.shouldReportSecondaryInValues = shouldReport;
    }
  }
}
