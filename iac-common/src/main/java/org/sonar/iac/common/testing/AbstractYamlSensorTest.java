/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.common.testing;

import org.junit.jupiter.api.Test;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.config.internal.MapSettings;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class contains tests for sensors inheriting from the YAML sensor.
 * It checks the correct handling of syntax errors and whether they convert to issues based on the profile.
 * The names of the tests do not follow the convention for normal Java classes.
 */
@SuppressWarnings("java:S100")
public abstract class AbstractYamlSensorTest extends AbstractSensorTest{

  private static final String PARSING_ERROR_KEY = "S2260";

  @Test
  void yaml_file_with_invalid_syntax_should_not_raise_parsing_if_rule_is_deactivated() {
    analyse(sensor(checkFactory()), inputFile("error.yaml", "a: b: c"));

    assertThat(context.allAnalysisErrors()).hasSize(1);
    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  void yaml_file_with_invalid_syntax_should_raise_parsing() {
    analyse(sensor(checkFactory(PARSING_ERROR_KEY)), inputFile("error.yaml", "a: b: c"));

    assertThat(context.allAnalysisErrors()).hasSize(1);
    assertThat(context.allIssues()).hasSize(1);
    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.ruleKey().rule()).as("A parsing error must be raised").isEqualTo(PARSING_ERROR_KEY);
  }

  @Test
  void yaml_file_with_invalid_syntax_should_not_raise_issue_when_sensor_deactivated() {
    MapSettings settings = new MapSettings();
    settings.setProperty(getActivationSettingKey(), false);
    context.setSettings(settings);

    analyse(sensor(checkFactory(PARSING_ERROR_KEY)), inputFile("parserError.json", "\"a'"));
    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  void yaml_file_with_recursive_anchor_reference_should_raise_parsing_issue() {
    analyse(sensor(checkFactory(PARSING_ERROR_KEY)), inputFile("loop.yaml", "foo: &fooanchor\n" +
      " bar: *fooanchor"));

    assertThat(context.allAnalysisErrors()).hasSize(1);
    assertThat(context.allIssues()).hasSize(1);
    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.ruleKey().rule()).as("A parsing error must be raised").isEqualTo(PARSING_ERROR_KEY);
  }


}
