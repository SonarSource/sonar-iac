/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
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
    assertThat(descriptor.name()).isEqualTo("IaC CloudFormation Sensor");
    assertThat(descriptor.languages()).containsExactly("json", "yaml");
  }

  @Test
  void empty_file_should_raise_no_issue() {
    analyse(sensor("S2260"), inputFile("empty.json", ""));
    assertThat(context.allIssues()).as("No issue must be raised").isEmpty();
  }

  @Test
  void yaml_only_comment_should_raise_no_issue() {
    analyse(sensor("S2260"), inputFile("comment.yaml", "# Some Comment"));
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

  @Test
  void should_raise_no_issue_when_sensor_deactivated() {
    MapSettings settings = new MapSettings();
    settings.setProperty(getActivationSettingKey(), false);
    context.setSettings(settings);

    analyse(sensor("S2260"), inputFile("parserError.json", "\"a'"));
    assertThat(context.allIssues()).as("One issue must be raised").isEmpty();
  }

  @Test
  void should_raise_no_parsing_issue_in_file_without_identifier() {
    MapSettings settings = new MapSettings();
    settings.setProperty(CloudformationSettings.FILE_IDENTIFIER_KEY, "myIdentifier");
    settings.setProperty(getActivationSettingKey(), true);
    context.setSettings(settings);

    analyse(sensor("S2260"), inputFile("parserError.json", "\"noIdentifier'"));
    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  void should_raise_parsing_issue_in_file_with_identifier() {
    MapSettings settings = new MapSettings();
    settings.setProperty(CloudformationSettings.FILE_IDENTIFIER_KEY, "myIdentifier");
    settings.setProperty(getActivationSettingKey(), true);
    context.setSettings(settings);

    analyse(sensor("S2260"), inputFile("parserError.json", "\"myIdentifier'"));
    assertThat(context.allIssues()).hasSize(1);
  }

  private CloudformationSensor sensor(String... rules) {
    return sensor(checkFactory(rules));
  }

  @Override
  protected String getActivationSettingKey() {
    return CloudformationSettings.ACTIVATION_KEY;
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
    return new CloudformationLanguage();
  }

  @Override
  protected String fileLanguageKey() {
    return "json";
  }
}
