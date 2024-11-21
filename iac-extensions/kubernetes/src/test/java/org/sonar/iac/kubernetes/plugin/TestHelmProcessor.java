/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.kubernetes.plugin;

import java.util.Map;
import javax.annotation.CheckForNull;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;

/**
 * The test helper allows to simulate the template processing for Helm charts.
 * The expected output of the file evaluation can be provided.
 * All other functionalities of the HelmProcessor can be tested without the need to mock every behaviour.
 */
public class TestHelmProcessor extends HelmProcessor {

  static String SINGLE_INPUT = "SingleInputKey";

  final Map<String, String> expectedInputOutputMapping;

  public TestHelmProcessor(String expectedOutput) {
    this(Map.of(SINGLE_INPUT, expectedOutput));
  }

  public TestHelmProcessor(Map<String, String> expectedInputOutputMapping) {
    super(null, null);
    this.expectedInputOutputMapping = expectedInputOutputMapping;
  }

  @Override
  public void initialize() {
    // do nothing
  }

  @Override
  public boolean isHelmEvaluatorInitialized() {
    return true;
  }

  @CheckForNull
  @Override
  String processHelmTemplate(String source, HelmInputFileContext inputFileContext) {
    if (expectedInputOutputMapping.containsKey(SINGLE_INPUT)) {
      return expectedInputOutputMapping.get(SINGLE_INPUT);
    } else {
      return expectedInputOutputMapping.get(source);
    }
  }
}
