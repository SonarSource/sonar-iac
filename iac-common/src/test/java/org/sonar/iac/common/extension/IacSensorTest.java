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
package org.sonar.iac.common.extension;

import com.sonar.sslr.api.RecognitionException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.error.AnalysisError;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.batch.sensor.issue.IssueLocation;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.resources.Language;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.Version;
import org.sonar.iac.common.AbstractTestTree;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.extension.visitors.ChecksVisitor;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.testing.AbstractSensorTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.testing.IacCommonAssertions.assertThat;

class IacSensorTest extends AbstractSensorTest {

  TreeParser<Tree> testParserThrowsRuntimeException = (source, inputFileContext) -> {
    throw new RuntimeException("RuntimeException message");
  };

  TreeParser<Tree> testParserThrowsParseException = (source, inputFileContext) -> {
    throw new ParseException("ParseException message", new BasicTextPointer(1, 2), "Details of error");
  };

  TreeParser<Tree> testParserThrowsRecognitionException = (source, inputFileContext) -> {
    throw new RecognitionException(1, "RecognitionException message");
  };

  @Test
  void testDescriptorSonarqube8_9() {
    DefaultSensorDescriptor sensorDescriptor = new DefaultSensorDescriptor();
    IacSensor sensor = sensor();
    sensor.describe(sensorDescriptor);
    assertThat(sensorDescriptor.languages()).hasSize(1);
    assertThat(sensorDescriptor.languages()).containsExactly("iac");
    assertThat(sensorDescriptor.name()).isEqualTo("IaC Common Sensor");
  }

  @Test
  void testDescriptorSonarqube9_3() {
    final boolean[] called = {false};
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor() {
      public SensorDescriptor processesFilesIndependently() {
        called[0] = true;
        return this;
      }
    };
    sensor(SonarRuntimeImpl.forSonarQube(Version.create(9, 3), SonarQubeSide.SCANNER, SonarEdition.DEVELOPER), null)
      .describe(descriptor);
    assertThat(called[0]).isTrue();
  }

  @Test
  void testEmptyFile() {
    analyse(sensor("S2260"), inputFile("emptyFile.iac", ""));

    Collection<Issue> issues = context.allIssues();
    assertThat(issues).isEmpty();
  }

  @Test
  void testParsingErrorShouldRaiseAnIssueIfCheckRuleIsActivated() {
    InputFile inputFile = inputFile("file1.iac", "\n{}");
    analyse(sensor("S2260"), inputFile);

    Collection<Issue> issues = context.allIssues();
    assertThat(issues).hasSize(1);
    Issue issue = issues.iterator().next();
    assertThat(issue.ruleKey().rule()).isEqualTo("S2260");
    IssueLocation location = issue.primaryLocation();
    assertThat(location.inputComponent()).isEqualTo(inputFile);
    assertThat(location.message()).isEqualTo("A parsing error occurred in this file.");
    assertThat(location.textRange()).isNull();

    Collection<AnalysisError> analysisErrors = context.allAnalysisErrors();
    assertThat(analysisErrors).hasSize(1);
    AnalysisError analysisError = analysisErrors.iterator().next();
    assertThat(analysisError.inputFile()).isEqualTo(inputFile);
    assertThat(analysisError.message()).isEqualTo("Unable to parse file: file1.iac");
    TextPointer textPointer = analysisError.location();
    assertThat(textPointer).isNull();

    assertThat(logTester.logs(Level.ERROR))
      .containsExactly("Cannot parse 'file1.iac'");
    assertThat(logTester.logs(Level.DEBUG).get(0))
      .isEqualTo("RuntimeException message");
    assertThat(logTester.logs(Level.DEBUG).get(1))
      .startsWith("org.sonar.iac.common.extension.ParseException: Cannot parse 'file1.iac'" +
        System.lineSeparator() +
        "\tat org.sonar.iac");
    assertThat(logTester.logs(Level.DEBUG)).hasSize(2);
  }

  @Test
  void testParsingErrorShouldRaiseNoIssueIfCheckRuleIsDeactivated() {
    analyse(inputFile("file1.iac", "\n{}"));

    Collection<Issue> issues = context.allIssues();
    assertThat(issues).isEmpty();

    Collection<AnalysisError> analysisErrors = context.allAnalysisErrors();
    assertThat(analysisErrors).hasSize(1);
  }

  @Test
  void testParsingErrorShouldRaiseOnCorruptedFile() throws IOException {
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

    assertThat(logTester.logs()).contains(String.format("Cannot read '%s'", inputFile.filename()));
  }

  @Test
  void testCancellation() {
    context.setCancelled(true);
    analyse(inputFile("file1.iac", "{}"));
    Collection<Issue> issues = context.allIssues();
    assertThat(issues).isEmpty();
  }

  @Test
  void testValidCheck() {
    CheckFactory checkFactory = mock(CheckFactory.class);
    Checks checks = mock(Checks.class);
    IacCheck validCheck = init -> init.register(Tree.class, (ctx, tree) -> ctx.reportIssue(tree, "testIssue"));

    when(checks.ruleKey(validCheck)).thenReturn(RuleKey.of(repositoryKey(), "valid"));
    when(checkFactory.create(repositoryKey())).thenReturn(checks);
    when(checks.all()).thenReturn(Collections.singletonList(validCheck));
    testParserThrowsRuntimeException = (source, inputFileContext) -> new TestTree();

    InputFile inputFile = inputFile("file1.iac", "foo");
    analyse(sensor(checkFactory), inputFile);

    Collection<Issue> issues = context.allIssues();
    assertThat(issues).hasSize(1);
    Issue issue = issues.iterator().next();
    assertThat(issue.ruleKey().rule()).isEqualTo("valid");
    IssueLocation location = issue.primaryLocation();
    assertThat(location.inputComponent()).isEqualTo(inputFile);
    assertThat(location.message()).isEqualTo("testIssue");
    assertThat(TextRanges.range(location.textRange().start().line(), location.textRange().start().lineOffset(), location.textRange().end().line(),
      location.textRange().end().lineOffset())).hasRange(1, 0, 1, 2);
  }

  @Test
  void testValidCheckWithSecondary() {
    CheckFactory checkFactory = mock(CheckFactory.class);
    Checks checks = mock(Checks.class);
    IacCheck validCheck = init -> init.register(Tree.class, (ctx, tree) -> ctx.reportIssue(tree, "testIssue", SecondaryLocation.secondary(tree.textRange(), "testSecondary")));

    when(checks.ruleKey(validCheck)).thenReturn(RuleKey.of(repositoryKey(), "valid"));
    when(checkFactory.create(repositoryKey())).thenReturn(checks);
    when(checks.all()).thenReturn(Collections.singletonList(validCheck));
    testParserThrowsRuntimeException = (source, inputFileContext) -> new TestTree();

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
    });
  }

  @Test
  void testIssueNotRaisedTwiceOnSameRange() {
    CheckFactory checkFactory = mock(CheckFactory.class);
    Checks checks = mock(Checks.class);
    IacCheck validCheck = init -> init.register(Tree.class, (ctx, tree) -> {
      ctx.reportIssue(tree.textRange(), "testIssue");
      ctx.reportIssue(tree.textRange(), "testIssue");
    });

    when(checks.ruleKey(validCheck)).thenReturn(RuleKey.of(repositoryKey(), "valid"));
    when(checkFactory.create(repositoryKey())).thenReturn(checks);
    when(checks.all()).thenReturn(Collections.singletonList(validCheck));
    testParserThrowsRuntimeException = (source, inputFileContext) -> new TestTree();

    InputFile inputFile = inputFile("file1.iac", "foo");
    analyse(sensor(checkFactory), inputFile);

    Collection<Issue> issues = context.allIssues();
    assertThat(issues).hasSize(1);
  }

  @Test
  void testFailureInCheck() {
    CheckFactory checkFactory = mock(CheckFactory.class);
    Checks checks = mock(Checks.class);
    IacCheck failingCheck = init -> init.register(Tree.class, (ctx, tree) -> {
      throw new IllegalStateException("Crash");
    });
    when(checks.ruleKey(failingCheck)).thenReturn(RuleKey.of(repositoryKey(), "failing"));
    when(checkFactory.create(repositoryKey())).thenReturn(checks);
    when(checks.all()).thenReturn(Collections.singletonList(failingCheck));
    testParserThrowsRuntimeException = (source, inputFileContext) -> new TestTree();

    InputFile inputFile = inputFile("file1.iac", "foo");
    analyse(sensor(checkFactory), inputFile);

    Collection<AnalysisError> analysisErrors = context.allAnalysisErrors();
    assertThat(analysisErrors).hasSize(1);
    AnalysisError analysisError = analysisErrors.iterator().next();
    assertThat(analysisError.inputFile()).isEqualTo(inputFile);
    assertThat(logTester.logs()).contains("Cannot analyse 'file1.iac': Crash");
  }

  @Test
  void failureInCheckShouldStopAnalysisWithFailFastEnabled() {
    CheckFactory checkFactory = mock(CheckFactory.class);
    Checks checks = mock(Checks.class);
    IacCheck failingCheck = init -> init.register(Tree.class, (ctx, tree) -> {
      throw new IllegalStateException("Crash");
    });
    when(checks.ruleKey(failingCheck)).thenReturn(RuleKey.of(repositoryKey(), "failing"));
    when(checkFactory.create(repositoryKey())).thenReturn(checks);
    when(checks.all()).thenReturn(Collections.singletonList(failingCheck));
    testParserThrowsRuntimeException = (source, inputFileContext) -> new TestTree();

    InputFile inputFile = inputFile("file1.iac", "foo");
    IacSensor sensor = sensor(checkFactory);

    MapSettings settings = new MapSettings();
    settings
      .setProperty(getActivationSettingKey(), true)
      .setProperty("sonar.internal.analysis.failFast", true);
    context.setSettings(settings);

    assertThatThrownBy(() -> analyse(sensor, inputFile))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Exception when analyzing 'file1.iac'");
  }

  @Test
  void shouldNotRaiseIssueWhenSensorIsDeactivated() {
    MapSettings settings = new MapSettings();
    settings.setProperty(getActivationSettingKey(), false);
    context.setSettings(settings);
    InputFile inputFile = inputFile("file1.iac", "\n{}");
    analyse(sensor("S2260"), inputFile);

    Collection<Issue> issues = context.allIssues();
    assertThat(issues).isEmpty();
  }

  @Test
  void shouldRethrowParseExceptionAndLogIt() {
    CheckFactory checkFactory = mock(CheckFactory.class);
    Checks checks = mock(Checks.class);
    IacCheck validCheck = init -> init.register(Tree.class, (ctx, tree) -> ctx.reportIssue(tree, "testIssue"));
    when(checks.ruleKey(validCheck)).thenReturn(RuleKey.of(repositoryKey(), "valid"));
    when(checkFactory.create(repositoryKey())).thenReturn(checks);
    when(checks.all()).thenReturn(Collections.singletonList(validCheck));
    InputFile inputFile = inputFile("file1.iac", "foo");

    analyse(sensorParseException(checkFactory), inputFile);

    assertThat(logTester.logs(Level.ERROR))
      .containsExactly("ParseException message");
    assertThat(logTester.logs(Level.DEBUG)).hasSize(2);
    assertThat(logTester.logs(Level.DEBUG).get(0))
      .startsWith("Details of error");
    assertThat(logTester.logs(Level.DEBUG).get(1))
      .startsWith("org.sonar.iac.common.extension.ParseException: ParseException message"
        + System.lineSeparator() +
        "\tat org.sonar.iac");
  }

  @Test
  void shouldRethrowRecognitionExceptionAndLogIt() {
    CheckFactory checkFactory = mock(CheckFactory.class);
    Checks checks = mock(Checks.class);
    IacCheck validCheck = init -> init.register(Tree.class, (ctx, tree) -> ctx.reportIssue(tree, "testIssue"));
    when(checks.ruleKey(validCheck)).thenReturn(RuleKey.of(repositoryKey(), "valid"));
    when(checkFactory.create(repositoryKey())).thenReturn(checks);
    when(checks.all()).thenReturn(Collections.singletonList(validCheck));
    InputFile inputFile = inputFile("file1.iac", "foo");

    analyse(sensorRecognitionException(checkFactory), inputFile);

    assertThat(logTester.logs(Level.ERROR))
      .containsExactly("Cannot parse 'file1.iac:1:1'");
    assertThat(logTester.logs(Level.DEBUG).get(0))
      .startsWith("RecognitionException message");
    assertThat(logTester.logs(Level.DEBUG).get(1))
      .startsWith("org.sonar.iac.common.extension.ParseException: Cannot parse 'file1.iac:1:1'" +
        System.lineSeparator() +
        "\tat org.sonar.iac.common");
    assertThat(logTester.logs(Level.DEBUG)).hasSize(2);
  }

  @Test
  void testValidCheckWithSecondaryOnOtherFile() {
    CheckFactory checkFactory = mock(CheckFactory.class);
    Checks checks = mock(Checks.class);
    String secondaryFilePath = "secondary" + File.separator + "file.iac";
    IacCheck validCheck = init -> init.register(Tree.class,
      (ctx, tree) -> ctx.reportIssue(
        tree,
        "testIssue",
        new SecondaryLocation(tree.textRange(), "testSecondary", secondaryFilePath)));

    when(checks.ruleKey(validCheck)).thenReturn(RuleKey.of(repositoryKey(), "valid"));
    when(checkFactory.create(repositoryKey())).thenReturn(checks);
    when(checks.all()).thenReturn(Collections.singletonList(validCheck));
    testParserThrowsRuntimeException = (source, inputFileContext) -> new TestTree();

    InputFile inputFile = inputFile("file1.iac", "foo");

    // this file has no language to not be analyzed by the sensor
    InputFile secondaryFile = new TestInputFileBuilder("moduleKey", secondaryFilePath)
      .setModuleBaseDir(baseDir.toPath())
      .setType(InputFile.Type.MAIN)
      .setCharset(StandardCharsets.UTF_8)
      .setContents("bar")
      .build();

    analyse(sensor(checkFactory), inputFile, secondaryFile);

    Collection<Issue> issues = context.allIssues();
    assertThat(issues).hasSize(1);
    Issue issue = issues.iterator().next();

    assertThat(issue.primaryLocation().inputComponent()).isEqualTo(inputFile);
    assertThat(issue.flows()).satisfies(flows -> {
      assertThat(flows.size()).isOne();
      assertThat(flows.get(0).locations()).satisfies(flow -> {
        assertThat(flow.size()).isOne();
        assertThat(flow.get(0).message()).isEqualTo("testSecondary");
        assertThat(flow.get(0).inputComponent()).isEqualTo(secondaryFile);
      });
    });
  }

  @Test
  void shouldNotImportExternalReportsInSonarLintContext() {
    IacSensor sensor = sensor();
    sensor.execute(context);
    assertThat(((TestIacSensor) sensor).didImportExternalReports).isTrue();
    sensor = sensor();
    sensor.execute(sonarLintContext);
    assertThat(((TestIacSensor) sensor).didImportExternalReports).isFalse();
  }

  @Test
  void shouldNotIncludeStackTraceInLogsWhenIssueIsReportedOnInvalidLineOffset() {
    CheckFactory checkFactory = mock(CheckFactory.class);
    Checks checks = mock(Checks.class);
    IacCheck validCheck = init -> init.register(Tree.class, (ctx, tree) -> ctx.reportIssue(TextRanges.range(1, 100, "foo"), "test"));
    when(checks.ruleKey(validCheck)).thenReturn(RuleKey.of(repositoryKey(), "valid"));
    when(checkFactory.create(repositoryKey())).thenReturn(checks);
    when(checks.all()).thenReturn(Collections.singletonList(validCheck));
    testParserThrowsRuntimeException = (source, inputFileContext) -> new TestTree();

    InputFile inputFile = inputFile("file1.iac", "foo");
    analyse(sensor(checkFactory), inputFile);

    Collection<Issue> issues = context.allIssues();
    assertThat(issues).isEmpty();
    assertThat(logTester.logs(Level.ERROR))
      .containsExactly("Cannot analyse 'file1.iac': 100 is not a valid line offset for pointer. File file1.iac has 3 character(s) at line 1");
  }

  @Override
  protected String repositoryKey() {
    return "iac";
  }

  @Override
  protected String fileLanguageKey() {
    return IacLanguage.IAC.getKey();
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
    return sensor(SONAR_RUNTIME_8_9, checkFactory);
  }

  protected IacSensor sensor(SonarRuntime sonarRuntime, CheckFactory checkFactory) {
    return new TestIacSensor(sonarRuntime,
      fileLinesContextFactory,
      noSonarFilter,
      IacLanguage.IAC,
      testParserThrowsRuntimeException,
      checkFactory);
  }

  private IacSensor sensorParseException(CheckFactory checkFactory) {
    return new TestIacSensor(SONAR_RUNTIME_8_9,
      fileLinesContextFactory,
      noSonarFilter,
      IacLanguage.IAC,
      testParserThrowsParseException,
      checkFactory);
  }

  private IacSensor sensorRecognitionException(CheckFactory checkFactory) {
    return new TestIacSensor(SONAR_RUNTIME_8_9,
      fileLinesContextFactory,
      noSonarFilter,
      IacLanguage.IAC,
      testParserThrowsRecognitionException,
      checkFactory);
  }

  class TestIacSensor extends IacSensor {
    private final TreeParser<Tree> treeParser;
    private CheckFactory checkFactory;
    private boolean didImportExternalReports = false;

    protected TestIacSensor(SonarRuntime sonarRuntime,
      FileLinesContextFactory fileLinesContextFactory,
      NoSonarFilter noSonarFilter,
      Language language,
      TreeParser<Tree> treeParser,
      CheckFactory checkFactory) {

      super(sonarRuntime, fileLinesContextFactory, noSonarFilter, language);
      this.treeParser = treeParser;
      this.checkFactory = checkFactory;
    }

    @Override
    protected TreeParser<Tree> treeParser() {
      return treeParser;
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
    protected void importExternalReports(SensorContext sensorContext) {
      didImportExternalReports = true;
    }
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
      return new String[] {".iac"};
    }
  }

  private static class TestTree extends AbstractTestTree {
    @Override
    public TextRange textRange() {
      return TextRanges.range(1, 0, 1, 2);
    }
  }
}
