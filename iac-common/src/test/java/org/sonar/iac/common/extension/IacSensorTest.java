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
package org.sonar.iac.common.extension;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.fs.internal.DefaultTextPointer;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.error.AnalysisError;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.batch.sensor.issue.IssueLocation;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.resources.Language;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.iac.common.AbstractTestTree;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.extension.visitors.ChecksVisitor;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.testing.AbstractSensorTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;

class IacSensorTest extends AbstractSensorTest {

  TreeParser<Tree> testParser = (source, inputFileContext) -> {
    throw new RuntimeException();
  };

  @Test
  void test_descriptor() {
    DefaultSensorDescriptor sensorDescriptor = new DefaultSensorDescriptor();
    IacSensor sensor = sensor();
    sensor.describe(sensorDescriptor);
    assertThat(sensorDescriptor.languages()).hasSize(1);
    assertThat(sensorDescriptor.languages()).containsExactly("iac");
    assertThat(sensorDescriptor.name()).isEqualTo("IaC Common Sensor");
  }

  @Test
  void test_empty_file() {
    analyse(sensor("S2260"), inputFile("emptyFile.iac", ""));

    Collection<Issue> issues = context.allIssues();
    assertThat(issues).isEmpty();
  }

  @Test
  void test_parsing_error_should_raise_an_issue_if_check_rule_is_activated() {
    InputFile inputFile = inputFile("file1.iac", "\n{}");
    analyse(sensor("S2260"), inputFile);

    Collection<Issue> issues = context.allIssues();
    assertThat(issues).hasSize(1);
    Issue issue = issues.iterator().next();
    assertThat(issue.ruleKey().rule()).isEqualTo("S2260");
    IssueLocation location = issue.primaryLocation();
    assertThat(location.inputComponent()).isEqualTo(inputFile);
    assertThat(location.message()).isEqualTo("A parsing error occurred in this file.");
    assertTextRange(location.textRange()).hasRange(2, 0, 2, 2);

    Collection<AnalysisError> analysisErrors = context.allAnalysisErrors();
    assertThat(analysisErrors).hasSize(1);
    AnalysisError analysisError = analysisErrors.iterator().next();
    assertThat(analysisError.inputFile()).isEqualTo(inputFile);
    assertThat(analysisError.message()).isEqualTo("Unable to parse file: file1.iac");
    TextPointer textPointer = analysisError.location();
    assertThat(textPointer).isNotNull();
    assertThat(textPointer.line()).isEqualTo(2);
    assertThat(textPointer.lineOffset()).isEqualTo(1);

    assertThat(logTester.logs(LoggerLevel.ERROR).size()).isEqualTo(2);
    assertThat(logTester.logs(LoggerLevel.ERROR))
      .contains(String.format("Unable to parse file: %s. Parse error at position 2:1", inputFile.uri()))
      .contains("Cannot parse 'file1.iac': null");
  }

  @Test
  void test_parsing_error_should_raise_no_issue_if_check_rule_is_deactivated() {
    analyse(inputFile("file1.iac", "\n{}"));

    Collection<Issue> issues = context.allIssues();
    assertThat(issues).isEmpty();

    Collection<AnalysisError> analysisErrors = context.allAnalysisErrors();
    assertThat(analysisErrors).hasSize(1);
  }

  @Test
  void test_parsing_error_should_raise_on_corrupted_file() throws IOException {
    InputFile inputFile = inputFile("fakeFile.iac", "\n{}");
    InputFile spyInputFile = spy(inputFile);
    when(spyInputFile.contents()).thenThrow(IOException.class);
    analyse(spyInputFile);

    Collection<AnalysisError> analysisErrors = context.allAnalysisErrors();
    assertThat(analysisErrors).hasSize(1);
    AnalysisError analysisError = analysisErrors.iterator().next();
    assertThat(analysisError.inputFile()).isEqualTo(spyInputFile);
    assertThat(analysisError.message()).isEqualTo("Unable to parse file: fakeFile.iac");
    assertThat(analysisError.location()).isNull();

    assertThat(logTester.logs()).contains(String.format("Unable to parse file: %s. ", inputFile.uri()));
  }

  @Test
  void test_cancellation() {
    context.setCancelled(true);
    analyse(inputFile("file1.iac", "{}"));
    Collection<Issue> issues = context.allIssues();
    assertThat(issues).isEmpty();
  }

  @Test
  void test_valid_check() {
    CheckFactory checkFactory = mock(CheckFactory.class);
    Checks checks = mock(Checks.class);
    IacCheck validCheck = init -> init.register(Tree.class, (ctx, tree) -> ctx.reportIssue(tree, "testIssue"));

    when(checks.ruleKey(validCheck)).thenReturn(RuleKey.of(repositoryKey(), "valid"));
    when(checkFactory.create(repositoryKey())).thenReturn(checks);
    when(checks.all()).thenReturn(Collections.singletonList(validCheck));
    testParser = (source, inputFileContext) -> new TestTree();
    sensor(checkFactory).execute(context);

    InputFile inputFile = inputFile("file1.iac", "foo");
    analyse(sensor(checkFactory), inputFile);

    Collection<Issue> issues = context.allIssues();
    assertThat(issues).hasSize(1);
    Issue issue = issues.iterator().next();
    assertThat(issue.ruleKey().rule()).isEqualTo("valid");
    IssueLocation location = issue.primaryLocation();
    assertThat(location.inputComponent()).isEqualTo(inputFile);
    assertThat(location.message()).isEqualTo("testIssue");
    assertTextRange(location.textRange()).hasRange(1, 0, 1, 2);
  }

  @Test
  void test_valid_check_with_secondary() {
    CheckFactory checkFactory = mock(CheckFactory.class);
    Checks checks = mock(Checks.class);
    IacCheck validCheck = init ->
      init.register(Tree.class, (ctx, tree) ->
        ctx.reportIssue(tree, "testIssue", new SecondaryLocation(tree, "testSecondary")));

    when(checks.ruleKey(validCheck)).thenReturn(RuleKey.of(repositoryKey(), "valid"));
    when(checkFactory.create(repositoryKey())).thenReturn(checks);
    when(checks.all()).thenReturn(Collections.singletonList(validCheck));
    testParser = (source, inputFileContext) -> new TestTree();
    sensor(checkFactory).execute(context);

    InputFile inputFile = inputFile("file1.iac", "foo");
    analyse(sensor(checkFactory), inputFile);

    Collection<Issue> issues = context.allIssues();
    assertThat(issues).hasSize(1);
    Issue issue = issues.iterator().next();

    assertThat(issue.flows()).satisfies(flows -> {
        assertThat(flows.size()).isOne();
        assertThat(flows.get(0).locations()).satisfies(flow -> {
          assertThat(flow.size()).isOne();
          assertThat(flow.get(0).message()).isEqualTo("testSecondary");
        });
      }
    );
  }

  @Test
  void test_issue_not_raised_twice_on_same_range() {
    CheckFactory checkFactory = mock(CheckFactory.class);
    Checks checks = mock(Checks.class);
    IacCheck validCheck = init ->
      init.register(Tree.class, (ctx, tree) -> {
        ctx.reportIssue(tree.textRange(), "testIssue");
        ctx.reportIssue(tree.textRange(), "testIssue");
      });

    when(checks.ruleKey(validCheck)).thenReturn(RuleKey.of(repositoryKey(), "valid"));
    when(checkFactory.create(repositoryKey())).thenReturn(checks);
    when(checks.all()).thenReturn(Collections.singletonList(validCheck));
    testParser = (source, inputFileContext) -> new TestTree();
    sensor(checkFactory).execute(context);

    InputFile inputFile = inputFile("file1.iac", "foo");
    analyse(sensor(checkFactory), inputFile);

    Collection<Issue> issues = context.allIssues();
    assertThat(issues).hasSize(1);
  }

  @Test
  void test_failure_in_check() {
    CheckFactory checkFactory = mock(CheckFactory.class);
    Checks checks = mock(Checks.class);
    IacCheck failingCheck = init ->
      init.register(Tree.class, (ctx, tree) -> {
        throw new IllegalStateException("Crash");
      });
    when(checks.ruleKey(failingCheck)).thenReturn(RuleKey.of(repositoryKey(), "failing"));
    when(checkFactory.create(repositoryKey())).thenReturn(checks);
    when(checks.all()).thenReturn(Collections.singletonList(failingCheck));
    testParser = (source, inputFileContext) -> new TestTree();

    InputFile inputFile = inputFile("file1.iac", "foo");
    analyse(sensor(checkFactory), inputFile);

    Collection<AnalysisError> analysisErrors = context.allAnalysisErrors();
    assertThat(analysisErrors).hasSize(1);
    AnalysisError analysisError = analysisErrors.iterator().next();
    assertThat(analysisError.inputFile()).isEqualTo(inputFile);
    assertThat(logTester.logs()).contains("Cannot analyse 'file1.iac': Crash");
  }

  @Test
  void shoud_not_raise_issue_when_sensor_is_deactivated() {
    MapSettings settings = new MapSettings();
    settings.setProperty(getActivationSettingKey(), false);
    context.setSettings(settings);
    InputFile inputFile = inputFile("file1.iac", "\n{}");
    analyse(sensor("S2260"), inputFile);

    Collection<Issue> issues = context.allIssues();
    assertThat(issues).isEmpty();
  }

  @Override
  protected String repositoryKey() {
    return "iac";
  }

  @Override
  protected Language language() {
    return IacLanguage.IAC;
  }

  @Override
  protected String fileLanguageKey() {
    return language().getKey();
  }

  private IacSensor sensor(String... rules) {
    return sensor(checkFactory(rules));
  }

  @Override
  protected String getActivationSettingKey() {
    return "testsensor.active";
  }

  @Override
  protected IacSensor sensor(CheckFactory checkFactory) {

    return new IacSensor(fileLinesContextFactory, noSonarFilter, IacLanguage.IAC) {

      @Override
      protected TreeParser<Tree> treeParser() {
        return testParser;
      }

      @Override
      protected String repositoryKey() {
        return IacSensorTest.this.repositoryKey();
      }

      @Override
      protected List<TreeVisitor<InputFileContext>> visitors(SensorContext sensorContext, DurationStatistics statistics) {
        return Collections.singletonList(new ChecksVisitor(checkFactory.create(repositoryKey()), statistics));
      }

      @Override
      protected String getActivationSettingKey() {
        return "testsensor.active";
      }

      @Override
      protected ParseException toParseException(String action, InputFile inputFile, Exception cause) {
        if (!(cause instanceof IOException)) {
          TextPointer position = new DefaultTextPointer(2,1);
          return new ParseException("Cannot " + action + " '" + inputFile + "': " + cause.getMessage(), position);
        }
        return super.toParseException(action, inputFile, cause);
      }
    };
  }

  enum IacLanguage implements Language {
    IAC;

    @Override
    public String getKey() {
      return "iac";
    }

    @Override
    public String getName() {
      return "Common";
    }

    @Override
    public String[] getFileSuffixes() {
      return new String[]{".iac"};
    }
  }

  private static class TestTree extends AbstractTestTree {
    @Override
    public TextRange textRange() {
      return TextRanges.range(1,0, 1, 2);
    }
  }
}
