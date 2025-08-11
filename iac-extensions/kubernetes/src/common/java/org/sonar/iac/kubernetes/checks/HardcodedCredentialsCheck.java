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
package org.sonar.iac.kubernetes.checks;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.CommonExcludedPatterns;

@Rule(key = "S2068")
public class HardcodedCredentialsCheck extends AbstractEnvCheck {

  private static final String MESSAGE = "Make sure this is not a hard-coded credential.";
  private static final String SECONDARY_MESSAGE = "\"%s\" detected here";
  private static final String DEFAULT_CREDENTIAL_WORDS = "password,passwd,pwd,passphrase";
  protected static final Predicate<String> VALUE_INCLUSION_PREDICATE = Pattern.compile("[\\w\\p{Punct} ]{6,}", Pattern.CASE_INSENSITIVE)
    .asMatchPredicate();

  @RuleProperty(
    key = "credentialWords",
    description = "Comma-separated list of words identifying potential credentials",
    defaultValue = DEFAULT_CREDENTIAL_WORDS)
  public String credentialWords = DEFAULT_CREDENTIAL_WORDS;

  @Override
  protected String sensitiveKeywords() {
    return credentialWords;
  }

  @Override
  protected boolean isValueSensitive(String nameFieldValue, String valueFieldValue) {
    return VALUE_INCLUSION_PREDICATE.test(valueFieldValue)
      && !isPlaceholder(nameFieldValue, valueFieldValue)
      && !CommonExcludedPatterns.isCommonExcludedPattern(valueFieldValue);
  }

  private static boolean isPlaceholder(String nameFieldValue, String valueFieldValue) {
    return valueFieldValue.contains(nameFieldValue);
  }

  @Override
  protected void reportSensitiveEnvVariable(CheckContext ctx, SensitiveEnvVariable sensitiveEnvVariable) {
    var secondary = new SecondaryLocation(sensitiveEnvVariable.nameTree(), SECONDARY_MESSAGE.formatted(sensitiveEnvVariable.nameKeyword()));
    ctx.reportIssue(sensitiveEnvVariable.valueTree(), MESSAGE, secondary);
  }
}
