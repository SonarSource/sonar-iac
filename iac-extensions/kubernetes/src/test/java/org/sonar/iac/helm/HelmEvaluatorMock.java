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
package org.sonar.iac.helm;

import java.util.Map;
import org.sonar.api.utils.TempFolder;
import org.sonar.iac.helm.protobuf.TemplateEvaluationResult;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HelmEvaluatorMock extends HelmEvaluator {

  private String template;

  private HelmEvaluatorMock() {
    super(mock(TempFolder.class));
  }

  @Override
  public TemplateEvaluationResult evaluateTemplate(String path, String content, Map<String, String> templateDependencies) {
    var result = mock(TemplateEvaluationResult.class);
    when(result.getTemplate()).thenReturn(template);
    return result;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final HelmEvaluatorMock tester;

    private Builder() {
      tester = new HelmEvaluatorMock();
    }

    public Builder setResultTemplate(String template) {
      tester.template = template;
      return this;
    }

    public HelmEvaluatorMock build() {
      return tester;
    }
  }
}
