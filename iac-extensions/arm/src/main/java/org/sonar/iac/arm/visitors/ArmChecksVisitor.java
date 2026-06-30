/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.arm.visitors;

import java.util.List;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.rule.RuleKey;
import org.sonar.iac.arm.plugin.ArmSensor;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.visitors.ChecksVisitor;

/**
 * ARM-specific {@link ChecksVisitor} that selects the rule description context based on the format of the analyzed file.
 * <p>
 * A number of ARM rules document their fix guidance in two {@code How to fix it in <X>} sections, one for Bicep and one for JSON/ARM
 * templates (see {@code org.sonar.api.server.rule.Context#getKey()}). The matching context key is the same for every issue raised in a
 * given file, so rather than passing it at each report site, it is injected centrally here for the rules that have such contexts, unless
 * the check already provided an explicit key.
 */
public class ArmChecksVisitor extends ChecksVisitor {

  static final String BICEP_CONTEXT_KEY = "bicep";
  static final String JSON_TEMPLATES_CONTEXT_KEY = "json_templates";
  static final String ARM_TEMPLATES_CONTEXT_KEY = "arm_templates";

  /**
   * ARM rules whose description provides a {@code Bicep} context and a JSON/ARM-templates context. Kept in sync with the shipped rule
   * descriptions by {@code ArmChecksVisitorTest} (guard test), which fails the build if a rule is added or removed.
   */
  static final Set<String> RULES_WITH_BICEP_AND_TEMPLATES_CONTEXTS = Set.of(
    "S117", "S1192", "S1481", "S4507", "S6321", "S6329", "S6378", "S6380", "S6385", "S6387",
    "S6413", "S6437", "S6648", "S6656", "S6949", "S6952", "S6953", "S6954", "S6955", "S6956",
    "S6975", "S8679", "S8680", "S8681", "S8682", "S8683", "S8684");

  /**
   * Subset of {@link #RULES_WITH_BICEP_AND_TEMPLATES_CONTEXTS} whose non-Bicep context is {@code ARM Templates} ({@code arm_templates})
   * instead of {@code JSON templates} ({@code json_templates}).
   */
  static final Set<String> RULES_USING_ARM_TEMPLATES_CONTEXT = Set.of("S4507", "S6329", "S6413");

  public ArmChecksVisitor(Checks<IacCheck> checks, DurationStatistics statistics) {
    super(checks, statistics);
  }

  @Override
  protected InitContext context(RuleKey ruleKey) {
    return new ArmContextAdapter(ruleKey);
  }

  /**
   * Returns the rule description context key to use for the given rule and file format, or {@code null} when the rule does not split its
   * description into Bicep / JSON-ARM contexts.
   */
  @CheckForNull
  static String contextKeyFor(String ruleKey, boolean isBicepFile) {
    if (!RULES_WITH_BICEP_AND_TEMPLATES_CONTEXTS.contains(ruleKey)) {
      return null;
    }
    if (isBicepFile) {
      return BICEP_CONTEXT_KEY;
    }
    return RULES_USING_ARM_TEMPLATES_CONTEXT.contains(ruleKey) ? ARM_TEMPLATES_CONTEXT_KEY : JSON_TEMPLATES_CONTEXT_KEY;
  }

  public class ArmContextAdapter extends ContextAdapter {

    public ArmContextAdapter(RuleKey ruleKey) {
      super(ruleKey);
    }

    @Override
    public void reportIssue(@Nullable TextRange textRange, String message, List<SecondaryLocation> secondaryLocations,
      @Nullable String ruleDescriptionContextKey) {
      var contextKey = ruleDescriptionContextKey;
      if (contextKey == null && currentCtx != null) {
        contextKey = contextKeyFor(ruleKey.rule(), ArmSensor.isBicepFile(currentCtx));
      }
      super.reportIssue(textRange, message, secondaryLocations, contextKey);
    }
  }
}
