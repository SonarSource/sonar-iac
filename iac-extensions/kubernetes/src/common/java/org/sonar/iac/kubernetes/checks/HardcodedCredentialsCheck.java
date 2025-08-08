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

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.checks.CommonExcludedPatterns;
import org.sonar.iac.common.yaml.object.BlockObject;

@Rule(key = "S2068")
public class HardcodedCredentialsCheck extends AbstractKubernetesObjectCheck {

  private static final String MESSAGE = "Make sure this is not a hard-coded credential.";
  private static final String SECONDARY_MESSAGE = "\"%s\" detected here";
  private static final String NAME_BLOCK = "name";
  private static final String VALUE_BLOCK = "value";
  private static final String CAPTURE_NAME = "name";
  private static final String DEFAULT_CREDENTIAL_WORDS = "password,passwd,pwd,passphrase";
  protected static final Predicate<String> VALUE_INCLUSION_PREDICATE = Pattern.compile("[\\w\\p{Punct} ]{6,}", Pattern.CASE_INSENSITIVE)
    .asMatchPredicate();

  @RuleProperty(
    key = "credentialWords",
    description = "Comma-separated list of words identifying potential credentials",
    defaultValue = DEFAULT_CREDENTIAL_WORDS)
  public String credentialWords = DEFAULT_CREDENTIAL_WORDS;
  private Pattern credentialsPattern;

  @Override
  protected void registerObjectCheck() {
    credentialsPattern = buildCredentialsPattern();
    registerOnAnyKind(this::checkHardcodedCredentials);
  }

  private void checkHardcodedCredentials(BlockObject block) {
    block.blocks("containers")
      .forEach(container -> container.blocks("env")
        .forEach(this::reportOnHardcodedCredentials));

    block.block("template")
      .block("spec")
      .blocks("containers")
      .forEach(container -> container.blocks("env")
        .forEach(this::reportOnHardcodedCredentials));
  }

  private void reportOnHardcodedCredentials(BlockObject envBlock) {
    var sensitiveNameAttribute = envBlock.attribute(NAME_BLOCK);
    var sensitiveName = sensitiveNameAttribute.asStringValue();
    if (sensitiveName != null) {
      var matcher = credentialsPattern.matcher(sensitiveName);
      if (matcher.find()) {
        var sensitiveKeyword = matcher.group(CAPTURE_NAME);
        var secondary = new SecondaryLocation(sensitiveNameAttribute.tree.value(), SECONDARY_MESSAGE.formatted(sensitiveKeyword));
        envBlock.attribute(VALUE_BLOCK)
          .reportIfValue(
            tree -> tree instanceof TextTree textTree && isValueSensitive(sensitiveName, textTree.value()),
            MESSAGE, List.of(secondary));
      }
    }
  }

  private Pattern buildCredentialsPattern() {
    var pattern = Arrays.stream(credentialWords.split(","))
      .map(String::trim)
      .collect(Collectors.joining("|"));
    return Pattern.compile("(?<%s>%s)".formatted(CAPTURE_NAME, pattern), Pattern.CASE_INSENSITIVE);
  }

  private static boolean isValueSensitive(String nameFieldValue, String valueFieldValue) {
    return VALUE_INCLUSION_PREDICATE.test(valueFieldValue)
      && !isPlaceholder(nameFieldValue, valueFieldValue)
      && !CommonExcludedPatterns.isCommonExcludedPattern(valueFieldValue);
  }

  private static boolean isPlaceholder(String nameFieldValue, String valueFieldValue) {
    return valueFieldValue.contains(nameFieldValue);
  }
}
