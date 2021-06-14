/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.iac.cloudformation.plugin;

import org.junit.jupiter.api.Test;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.iac.common.testing.AbstractSensorTest;
import org.sonar.iac.common.testing.TextRangeAssert;

import static org.assertj.core.api.Assertions.assertThat;

class CloudformationSensorTest extends AbstractSensorTest {

  @Test
  void should_return_terraform_descriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    sensor().describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("IaC Cloudformation Sensor");
    assertThat(descriptor.languages()).containsOnly("cloudformation");
  }

  @Test
  void empty_file_should_raise_no_issue() {
    analyse(sensor("S2260"), inputFile("empty.json", ""));
    assertThat(context.allIssues()).as("No issue must be raised").isEmpty();
  }

  @Test
  void yaml_only_comment_should_raise_no_issue() {
    analyse(sensor("S2260"), inputFile("comment.json", "# Some Comment"));
    assertThat(context.allIssues()).as("No issue must be raised").isEmpty();
  }

  @Test
  void yaml_file_with_recursive_anchor_reference_should_raise_parsing_issue() {
    analyse(sensor("S2260"), inputFile("comment.json", "foo: &fooanchor\n" +
      " bar: *fooanchor"));

    assertThat(context.allIssues()).as("").hasSize(1);

    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.ruleKey().rule()).as("A parsing error must be raised").isEqualTo("S2260");

    TextRangeAssert.assertTextRange(issue.primaryLocation().textRange()).hasRange(1, 0, 1, 15);
  }

  @Test
  void parsing_error_should_raise_an_issue_if_check_rule_is_activated() {
    analyse(sensor("S2260"), inputFile("parserError.json", "\"a'"));

    assertThat(context.allIssues()).as("One issue must be raised").hasSize(1);

    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.ruleKey().rule()).as("A parsing error must be raised").isEqualTo("S2260");

    TextRangeAssert.assertTextRange(issue.primaryLocation().textRange()).hasRange(1, 0, 1, 3);
  }

  @Test
  void parsing_error_should_raise_issue_in_sensor_context() {
    analyse(inputFile("parserError.tf", "\"a'"));
    assertThat(context.allAnalysisErrors()).hasSize(1);
  }

  @Test
  void parsing_error_should_raise_no_issue_if_check_rule_is_not_activated() {
    analyse(inputFile("parserError.tf", "a {"));
    assertThat(context.allIssues()).as("One issue must be raised").isEmpty();
  }

  private CloudformationSensor sensor(String... rules) {
    return sensor(checkFactory(rules));
  }

  @Override
  protected CloudformationSensor sensor(CheckFactory checkFactory) {
    return new CloudformationSensor(fileLinesContextFactory, checkFactory, noSonarFilter, language());
  }

  @Override
  protected String repositoryKey() {
    return CloudformationExtension.REPOSITORY_KEY;
  }

  @Override
  protected CloudformationLanguage language() {
    return new CloudformationLanguage(new MapSettings().asConfig());
  }
}
