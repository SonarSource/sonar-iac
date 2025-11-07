/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.testing;

import javax.annotation.Nullable;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.sonar.api.batch.sensor.issue.ExternalIssue;
import org.sonar.api.issue.impact.Severity;
import org.sonar.api.issue.impact.SoftwareQuality;
import org.sonar.api.rules.CleanCodeAttribute;

public class ExternalIssueAssert extends AbstractAssert<ExternalIssueAssert, ExternalIssue> {
  private ExternalIssueAssert(@Nullable ExternalIssue issue) {
    super(issue, ExternalIssueAssert.class);
  }

  public static ExternalIssueAssert assertThat(@Nullable ExternalIssue actual) {
    return new ExternalIssueAssert(actual);
  }

  public ExternalIssueAssert hasRuleId(String expectedRuleId) {
    isNotNull();
    Assertions.assertThat(actual.ruleId()).as("ruleId mismatch").isEqualTo(expectedRuleId);
    return this;
  }

  public ExternalIssueAssert hasImpact(SoftwareQuality expectedSoftwareQuality, Severity expectedSeverity) {
    isNotNull();
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(actual.impacts()).as("softwareQuality mismatch").containsKey(expectedSoftwareQuality);
      softly.assertThat(actual.impacts().get(expectedSoftwareQuality)).as("severity mismatch").isEqualTo(expectedSeverity);
    });
    return this;
  }

  public ExternalIssueAssert hasSecurityImpact(Severity expectedSeverity) {
    return hasImpact(SoftwareQuality.SECURITY, expectedSeverity);
  }

  public ExternalIssueAssert hasMaintainabilityImpact(Severity expectedSeverity) {
    return hasImpact(SoftwareQuality.MAINTAINABILITY, expectedSeverity);
  }

  public ExternalIssueAssert hasReliabilityImpact(Severity expectedSeverity) {
    return hasImpact(SoftwareQuality.RELIABILITY, expectedSeverity);
  }

  public ExternalIssueAssert hasRemediationEffort(Long expectedEffort) {
    isNotNull();
    Assertions.assertThat(actual.remediationEffort()).as("remediationEffort mismatch").isEqualTo(expectedEffort);
    return this;
  }

  public ExternalIssueAssert hasCleanCodeAttribute(CleanCodeAttribute expectedCleanCodeAttribute) {
    isNotNull();
    Assertions.assertThat(actual.cleanCodeAttribute()).as("cleanCodeAttribute mismatch").isEqualTo(expectedCleanCodeAttribute);
    return this;
  }
}
