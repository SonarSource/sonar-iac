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
package org.sonar.iac.kubernetes.checks;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.CommonExcludedPatterns;
import org.sonarsource.analyzer.commons.EntropyDetector;
import org.sonarsource.analyzer.commons.HumanLanguageDetector;

@Rule(key = "S6418")
public class HardcodedSecretsCheck extends AbstractEnvCheck {

  private static final String MESSAGE = "Make sure this is not a hard-coded secret.";
  private static final String SECONDARY_MESSAGE = "\"%s\" detected here";
  private static final String DEFAULT_SECRET_WORDS = "api[_.-]?key,auth,credential,secret,token";
  private static final String DEFAULT_RANDOMNESS_SENSIBILITY = "3.0";
  private static final int MAX_RANDOMNESS_SENSIBILITY = 10;
  private static final double LANGUAGE_SCORE_INCREMENT = 0.3;
  private static final Predicate<String> VALID_SECRET_FORMAT_PATTERN = Pattern.compile("[a-zA-Z0-9_.+/~$-]([a-zA-Z0-9_.+/=~$-]|\\\\\\\\(?![ntr\"])){14,1022}[a-zA-Z0-9_.+/=~$-]")
    .asMatchPredicate();
  private static final Predicate<String> PATH_PATTERN = Pattern.compile(
    // Home directory paths (e.g., ~/.ssh/id_rsa)
    "~(/[\\w\\-.]++)++" +
    // Directory paths (e.g., data/proxy/secret)
      "|([\\w\\-.]++/){2,}+([\\w\\-.])?")
    .asPredicate();

  @RuleProperty(
    key = "secretWords",
    description = "Comma-separated list of words identifying potential secrets",
    defaultValue = DEFAULT_SECRET_WORDS)
  public String secretWords = DEFAULT_SECRET_WORDS;

  @RuleProperty(
    key = "randomnessSensibility",
    description = "Randomness sensibility (from 0 to 10) required for a value to be considered a secret",
    defaultValue = DEFAULT_RANDOMNESS_SENSIBILITY)
  public double randomnessSensibility = Double.parseDouble(DEFAULT_RANDOMNESS_SENSIBILITY);

  private EntropyDetector entropyDetector;
  private Double maxLanguageScore;

  @Override
  protected void init() {
    entropyDetector = new EntropyDetector(randomnessSensibility);
    maxLanguageScore = (MAX_RANDOMNESS_SENSIBILITY - randomnessSensibility) * LANGUAGE_SCORE_INCREMENT;
  }

  @Override
  protected String sensitiveKeywords() {
    return secretWords;
  }

  @Override
  protected boolean isValueSensitive(String nameFieldValue, String valueFieldValue) {
    return VALID_SECRET_FORMAT_PATTERN.test(valueFieldValue)
      && !isPath(valueFieldValue)
      && !isPlaceholder(nameFieldValue, valueFieldValue)
      && !CommonExcludedPatterns.isCommonExcludedPattern(valueFieldValue)
      && isRandomEnough(valueFieldValue)
      && isNotHumanLanguage(valueFieldValue);
  }

  private static boolean isPlaceholder(String nameFieldValue, String valueFieldValue) {
    return valueFieldValue.contains(nameFieldValue);
  }

  private boolean isRandomEnough(String value) {
    return entropyDetector.hasEnoughEntropy(value);
  }

  private boolean isNotHumanLanguage(String value) {
    return HumanLanguageDetector.humanLanguageScore(value) < maxLanguageScore;
  }

  private static boolean isPath(String value) {
    return PATH_PATTERN.test(value);
  }

  @Override
  protected void reportSensitiveEnvVariable(CheckContext ctx, SensitiveEnvVariable sensitiveEnvVariable) {
    var secondary = new SecondaryLocation(sensitiveEnvVariable.nameTree(), SECONDARY_MESSAGE.formatted(sensitiveEnvVariable.nameKeyword()));
    ctx.reportIssue(sensitiveEnvVariable.valueTree(), MESSAGE, secondary);
  }
}
