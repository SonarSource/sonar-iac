/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.kubernetes.visitors;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.rule.RuleKey;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.visitors.ChecksVisitor;
import org.sonar.iac.common.extension.visitors.InputFileContext;

public class KubernetesChecksVisitor extends ChecksVisitor {

  private static final Logger LOG = LoggerFactory.getLogger(KubernetesChecksVisitor.class);

  /**
   * TODO SONARIAC-1352 Remove property "secondaryLocationsInValuesEnable"
   */
  protected static final String ENABLE_SECONDARY_LOCATIONS_IN_VALUES_YAML_KEY = "sonar.kubernetes.internal.helm" +
    ".secondaryLocationsInValuesEnable";
  protected static final String DISABLE_SECONDARY_LOCATIONS_IN_OTHER_YAML_KEY = "sonar.kubernetes.internal.helm" +
    ".secondaryLocationsInOtherFilesDisable";

  private final ProjectContext projectContext;

  public KubernetesChecksVisitor(Checks<IacCheck> checks,
    DurationStatistics statistics,
    ProjectContext projectContext) {
    super(checks, statistics);
    this.projectContext = projectContext;
  }

  @Override
  protected InitContext context(RuleKey ruleKey) {
    return new KubernetesContextAdapter(ruleKey);
  }

  public class KubernetesContextAdapter extends ContextAdapter implements KubernetesCheckContext {

    private boolean shouldReportSecondaryInValues;

    public KubernetesContextAdapter(RuleKey ruleKey) {
      super(ruleKey);
    }

    @Override
    public ProjectContext projectContext() {
      return projectContext;
    }

    @Override
    public InputFileContext inputFileContext() {
      return currentCtx;
    }

    @Override
    public void reportIssue(@Nullable TextRange textRange, String message, List<SecondaryLocation> secondaryLocations) {
      if (currentCtx instanceof HelmInputFileContext helmCtx) {
        var shiftedTextRange = textRange;
        List<SecondaryLocation> allSecondaryLocations = new ArrayList<>();
        if (textRange != null) {
          shiftedTextRange = LocationShifter.shiftLocation(helmCtx, textRange);

          boolean isReportingEnabled = helmCtx.sensorContext.config().getBoolean(ENABLE_SECONDARY_LOCATIONS_IN_VALUES_YAML_KEY).orElse(false);
          if (isReportingEnabled || shouldReportSecondaryInValues()) {
            allSecondaryLocations = SecondaryLocationLocator.findSecondaryLocationsInAdditionalFiles(helmCtx, shiftedTextRange);
          }
        }

        var streamSecondaryLocations = secondaryLocations.stream();
        boolean isReportingDisabledOnOtherFile = helmCtx.sensorContext.config().getBoolean(DISABLE_SECONDARY_LOCATIONS_IN_OTHER_YAML_KEY + "." + ruleKey.rule()).orElse(false);
        if (isReportingDisabledOnOtherFile) {
          LOG.debug("External secondary locations for rule {} are disabled !", ruleKey.rule());
          streamSecondaryLocations = streamSecondaryLocations.filter(secondaryLocation -> secondaryLocation.filePath == null);
        }
        List<SecondaryLocation> shiftedSecondaryLocations = streamSecondaryLocations
          .map(secondaryLocation -> LocationShifter.computeShiftedSecondaryLocation(computeInputFileContext(secondaryLocation, helmCtx), secondaryLocation))
          .distinct()
          .toList();

        allSecondaryLocations.addAll(shiftedSecondaryLocations);
        helmCtx.reportIssue(ruleKey, shiftedTextRange, message, allSecondaryLocations);
      } else {
        currentCtx.reportIssue(ruleKey, textRange, message, secondaryLocations);
      }
    }

    private HelmInputFileContext computeInputFileContext(SecondaryLocation secondaryLocation, HelmInputFileContext defaultHelmContext) {
      InputFileContext context = null;
      if (secondaryLocation.filePath != null) {
        context = projectContext.getInputFileContext(secondaryLocation.filePath);
      }
      if (!(context instanceof HelmInputFileContext)) {
        context = defaultHelmContext;
      }
      return (HelmInputFileContext) context;
    }

    @Override
    public boolean shouldReportSecondaryInValues() {
      return this.shouldReportSecondaryInValues;
    }

    @Override
    public void setShouldReportSecondaryInValues(boolean shouldReport) {
      this.shouldReportSecondaryInValues = shouldReport;
    }

    @Override
    public void reportIssueNoLineShift(TextRange toHighlight, String message) {
      currentCtx.reportIssue(ruleKey, toHighlight, message, List.of());
    }
  }
}
