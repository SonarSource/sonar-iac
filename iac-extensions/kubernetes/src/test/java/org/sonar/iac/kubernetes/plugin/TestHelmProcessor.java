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
package org.sonar.iac.kubernetes.plugin;

import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;

import javax.annotation.CheckForNull;
import java.util.Map;

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
  protected void initialize() {
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
