/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.yaml.object.BlockObject;
import org.sonar.iac.common.yaml.tree.YamlTree;

/**
 * This class is about providing easy way to check env variables in Kubernetes containers.
 */
public abstract class AbstractEnvCheck extends AbstractKubernetesObjectCheck {
  private static final String NAME_BLOCK = "name";
  private static final String VALUE_BLOCK = "value";
  private static final String CAPTURE_NAME = "name";

  private Pattern namePattern;

  @Override
  protected final void registerObjectCheck() {
    init();
    namePattern = buildNamePattern();
    registerOnAnyKind(this::checkEnvVariables);
  }

  private void checkEnvVariables(BlockObject block) {
    block.blocks("containers")
      .forEach(container -> container.blocks("env")
        .forEach(this::checkEnvBlock));

    block.block("template")
      .block("spec")
      .blocks("containers")
      .forEach(container -> container.blocks("env")
        .forEach(this::checkEnvBlock));
  }

  private void checkEnvBlock(BlockObject envBlock) {
    var nameAttribute = envBlock.attribute(NAME_BLOCK);
    var nameValue = nameAttribute.asStringValue();
    if (nameValue != null) {
      var matcher = namePattern.matcher(nameValue);
      if (matcher.find()) {
        var nameSensitiveKeyword = matcher.group(CAPTURE_NAME);
        var valueBlock = envBlock.attribute(VALUE_BLOCK)
          .filterOnValue((String value) -> isValueSensitive(nameValue, value));
        if (valueBlock.isPresent()) {
          reportSensitiveEnvVariable(valueBlock.ctx,
            new SensitiveEnvVariable(nameAttribute.tree.value(), nameValue, nameSensitiveKeyword, valueBlock.tree.value(), valueBlock.asStringValue()));
        }
      }
    }
  }

  private Pattern buildNamePattern() {
    var pattern = Arrays.stream(sensitiveKeywords().split(","))
      .map(String::trim)
      .collect(Collectors.joining("|"));
    return Pattern.compile("(?<%s>%s)".formatted(CAPTURE_NAME, pattern), Pattern.CASE_INSENSITIVE);
  }

  protected void init() {
    // This method can be overridden by subclasses to perform any necessary initialization.
  }

  // This expects subclasses to provide a comma-separated list of sensitive keywords.
  protected abstract String sensitiveKeywords();

  protected abstract boolean isValueSensitive(String nameFieldValue, String valueFieldValue);

  protected abstract void reportSensitiveEnvVariable(CheckContext ctx, SensitiveEnvVariable sensitiveEnvVariable);

  protected record SensitiveEnvVariable(YamlTree nameTree, String nameValue, String nameKeyword, YamlTree valueTree, String value) {
  }
}
