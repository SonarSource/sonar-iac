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
package org.sonar.iac.common.extension.visitors;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.DefaultTextPointer;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.batch.sensor.issue.IssueLocation;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.testing.TextRangeAssert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.IacCommonAssertions.assertThat;

class InputFileContextTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  private static final TextRange INVALID_RANGE = range(1, 2, 0, 1);
  private static final TextRange EMPTY_RANGE = range(1, 1, 1, 1);
  private static final TextRange VALID_RANGE = range(1, 1, 1, 2);
  private static final TextRange OUT_OF_TEXT_RANGE = range(5, 1, 5, 2);
  @TempDir
  Path tempDir;

  private SensorContextTester sensorContext;
  private InputFileContext inputFileContext;

  @BeforeEach
  void beforeEach() {
    DefaultInputFile inputFile = new TestInputFileBuilder("moduleKey", "file")
      .setCharset(StandardCharsets.UTF_8)
      .initMetadata("The content of the file.")
      .build();

    sensorContext = SensorContextTester.create(tempDir);
    inputFileContext = new InputFileContext(sensorContext, inputFile);
  }

  @Test
  void primaryLocationRangeShouldNotBeAddedWhenRangeIsNull() {
    inputFileContext.reportIssue(RuleKey.parse("s:42"), null, "message", List.of());
    List<Issue> issues = new ArrayList<>(sensorContext.allIssues());
    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).primaryLocation().textRange()).isNull();
  }

  @Test
  void primaryLocationRangeShouldNotBeAddedWhenRangeIsInvalidOrEmpty() {
    inputFileContext.reportIssue(RuleKey.parse("s:42"), EMPTY_RANGE, "message", List.of());
    inputFileContext.reportIssue(RuleKey.parse("s:43"), INVALID_RANGE, "message", List.of());

    List<Issue> issues = new ArrayList<>(sensorContext.allIssues());
    assertThat(issues).hasSize(2)
      .allSatisfy(i -> assertThat(i.primaryLocation().textRange()).isNull());
  }

  @Test
  void secondaryLocationShouldNotBeAddedWhenItsRangeIsInvalidOrEmpty() {
    inputFileContext.reportIssue(RuleKey.parse("s:42"), VALID_RANGE, "message", List.of(
      new SecondaryLocation(EMPTY_RANGE, "message"),
      new SecondaryLocation(INVALID_RANGE, "message")));

    List<Issue> issues = new ArrayList<>(sensorContext.allIssues());
    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).flows()).isEmpty();
  }

  @Test
  void secondaryLocationShouldNotBeAddedWhenItIsNull() {
    ArrayList<SecondaryLocation> secondaries = new ArrayList<>();
    secondaries.add(null);
    inputFileContext.reportIssue(RuleKey.parse("s:42"), VALID_RANGE, "message", secondaries);

    List<Issue> issues = new ArrayList<>(sensorContext.allIssues());
    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).flows()).isEmpty();
  }

  @Test
  void secondaryLocationShouldBeAddedOnSecondaryFile() {
    String secondaryFilePath = "secondaryFile";
    DefaultInputFile secondaryFile = new TestInputFileBuilder("moduleKey", secondaryFilePath)
      .setCharset(StandardCharsets.UTF_8)
      .initMetadata("The content of the file.")
      .build();
    sensorContext.fileSystem().add(secondaryFile);

    inputFileContext.reportIssue(RuleKey.parse("s:42"), VALID_RANGE, "message", List.of(
      new SecondaryLocation(VALID_RANGE, "messageSecondary", secondaryFilePath)));

    List<Issue> issues = new ArrayList<>(sensorContext.allIssues());
    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).flows().get(0).locations()).hasSize(1);

    IssueLocation secondaryIssueLocation = issues.get(0).flows().get(0).locations().get(0);
    assertThat(secondaryIssueLocation.inputComponent()).isEqualTo(secondaryFile);
    assertThat(secondaryIssueLocation.message()).isEqualTo("messageSecondary");

    org.sonar.api.batch.fs.TextRange sonarApiTextRange = secondaryIssueLocation.textRange();
    TextRangeAssert.assertThat(VALID_RANGE).hasRange(
      sonarApiTextRange.start().line(),
      sonarApiTextRange.start().lineOffset(),
      sonarApiTextRange.end().line(),
      sonarApiTextRange.end().lineOffset());
  }

  @Test
  void secondaryLocationShouldNotBeAddedWhenFileNotFound() {
    inputFileContext.reportIssue(RuleKey.parse("s:42"), VALID_RANGE, "message", List.of(
      new SecondaryLocation(VALID_RANGE, "messageSecondary", "nonExistingPath")));

    List<Issue> issues = new ArrayList<>(sensorContext.allIssues());
    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).flows()).isEmpty();
  }

  @Test
  void shouldReturnNewPointer() {
    var textPointer = inputFileContext.newPointer(1, 0);
    assertThat(textPointer.line()).isEqualTo(1);
    assertThat(textPointer.lineOffset()).isZero();
    assertThat(logTester.logs(Level.WARN)).isEmpty();
  }

  @Test
  void shouldReturnDefaultTextPointerDoNotFailFast() {
    var textPointer = inputFileContext.newPointer(1000, 2000);
    assertThat(textPointer.line()).isEqualTo(1);
    assertThat(textPointer.lineOffset()).isZero();
    assertThat(logTester.logs(Level.WARN)).contains("Unable to create new pointer for file position 1000:2000");
  }

  @Test
  void shouldReturnDefaultTextPointerWhenNewPointerAndFailFastEnabled() {
    MapSettings mapSettings = new MapSettings();
    mapSettings.setProperty("sonar.internal.analysis.failFast", true);
    sensorContext.setSettings(mapSettings);

    var exception = catchException(() -> inputFileContext.newPointer(1000, 2000));
    assertThat(exception)
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Unable to create new pointer for file position 1000:2000");
  }

  @Test
  void shouldThrowExceptionWhenReportingIssueWithOutOfTextRangeAndFailFastEnabled() {
    MapSettings mapSettings = new MapSettings();
    mapSettings.setProperty("sonar.internal.analysis.failFast", true);
    sensorContext.setSettings(mapSettings);

    var exception = catchException(() -> inputFileContext.reportIssue(
      RuleKey.parse("s:42"),
      OUT_OF_TEXT_RANGE,
      "msg",
      List.of()));
    assertThat(exception)
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Unable to create new range for file and range [5:1/5:2]");
  }

  @Test
  void shouldReturnDefaultTextRangeWhenReportIssueWithOutOfTextRangeWhenFailFastDisabled() {
    inputFileContext.reportIssue(RuleKey.parse("s:42"), OUT_OF_TEXT_RANGE, "msg", List.of());
    List<Issue> issues = new ArrayList<>(sensorContext.allIssues());
    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).primaryLocation().textRange()).hasRange(1, 0, 1, 1);
  }

  @Test
  void shouldFallbackToBeginOfTheFileWhenInvalidLocation() {
    inputFileContext.reportAnalysisError("Error message", new DefaultTextPointer(5, 10));
    assertThat(logTester.logs(Level.DEBUG))
      .contains("Error when creating valid line offset of pointer, fallback to beginning of the file.");
  }

  @Test
  void shouldReportTwoIssuesEvenIfThyHaveTheSameHash() {
    var rule1 = RuleKey.of("kubernetes", "S6897");
    var textRange1 = TextRanges.range(10, 6, 10, 10);
    var rule2 = RuleKey.of("kubernetes", "S6864");
    var textRange2 = range(13, 6, 13, 10);
    var hash1 = Objects.hash(rule1, textRange1, List.of());
    var hash2 = Objects.hash(rule2, textRange2, List.of());
    // Those 2 Issue reports have the same hash
    assertThat(hash1).isEqualTo(hash2);

    DefaultInputFile inputFile = new TestInputFileBuilder("moduleKey", "file")
      .setCharset(StandardCharsets.UTF_8)
      .initMetadata("\n\n\n\n\n\n\n\n\nLine 10 content\n\n\nLine 13 content")
      .build();
    inputFileContext = new InputFileContext(sensorContext, inputFile);

    inputFileContext.reportIssue(rule1, textRange1, "message 1", List.of());
    inputFileContext.reportIssue(rule2, textRange2, "message 2", List.of());

    List<Issue> issues = new ArrayList<>(sensorContext.allIssues());
    assertThat(issues).hasSize(2);
  }
}
