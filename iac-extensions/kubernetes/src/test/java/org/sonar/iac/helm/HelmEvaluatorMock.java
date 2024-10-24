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
