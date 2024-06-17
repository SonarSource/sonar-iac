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

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.error.internal.DefaultAnalysisError;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonarsource.analyzer.commons.ProgressReport;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnalyzerTest {

  @TempDir
  private File tmpDir;
  private File baseDir;

  private SensorContextTester context;
  private TreeParser parser;
  private DurationStatistics durationStatistics;
  private ProgressReport progressReport;

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  @BeforeEach
  void init() throws IOException {
    parser = parser();
    durationStatistics = new DurationStatistics(mock(Configuration.class));
    progressReport = mock(ProgressReport.class);
    baseDir = tmpDir.toPath().toRealPath().resolve("test-project").toFile();
    FileUtils.forceMkdir(baseDir);
    context = SensorContextTester.create(baseDir);
  }

  @Test
  void shouldParseEmptyFile() throws IOException {
    Analyzer analyzer = new Analyzer("iac", parser, Collections.emptyList(), durationStatistics);
    List<InputFile> files = List.of(emptyFile());
    assertThat(analyzer.analyseFiles(sensorContext(false), files, progressReport)).isTrue();
  }

  @Test
  void shouldFailWhenCancelled() throws IOException {
    Analyzer analyzer = new Analyzer("iac", parser, Collections.emptyList(), durationStatistics);
    List<InputFile> files = List.of(emptyFile());
    assertThat(analyzer.analyseFiles(sensorContext(true), files, progressReport)).isFalse();
  }

  @Test
  void shouldReportParsingErrorOnInvalidFile() throws IOException {
    Analyzer analyzer = new Analyzer("iac", parser, Collections.emptyList(), durationStatistics);
    List<InputFile> files = List.of(invalidFile());
    assertThat(analyzer.analyseFiles(context, files, progressReport)).isTrue();
    assertThat(logTester.logs(Level.ERROR)).containsExactly("Cannot read 'InvalidFile'");

    List<String> debugLogs = logTester.logs(Level.DEBUG);
    assertThat(debugLogs).hasSize(2);
    assertThat(debugLogs.get(0)).isEqualTo("Invalid file mock");
    assertThat(debugLogs.get(1)).startsWith("org.sonar.iac.common.extension.ParseException: Cannot read 'InvalidFile'");
  }

  @Test
  void shouldReportOnParseException() throws IOException {
    when(parser.parse(anyString(), any(InputFileContext.class))).thenThrow(new ParseException("Custom parse exception", null, null));
    Analyzer analyzer = new Analyzer("iac", parser, Collections.emptyList(), durationStatistics);
    List<InputFile> files = List.of(file("Some content"));
    analyzer.analyseFiles(context, files, progressReport);

    assertThat(logTester.logs(Level.ERROR)).containsExactly("Custom parse exception");
    List<String> debugLogs = logTester.logs(Level.DEBUG);
    assertThat(debugLogs).hasSize(1);
    assertThat(debugLogs.get(0)).startsWith("org.sonar.iac.common.extension.ParseException: Custom parse exception");
  }

  @Test
  void shouldReportOnRuntimeException() throws IOException {
    when(parser.parse(anyString(), any(InputFileContext.class))).thenThrow(new RuntimeException("Custom runtime exception"));
    Analyzer analyzer = new Analyzer("iac", parser, Collections.emptyList(), durationStatistics);
    List<InputFile> files = List.of(file("Some content"));
    analyzer.analyseFiles(context, files, progressReport);

    assertThat(logTester.logs(Level.ERROR)).containsExactly("Cannot parse 'FileWithContent'");
    List<String> debugLogs = logTester.logs(Level.DEBUG);
    assertThat(debugLogs).hasSize(2);
    assertThat(debugLogs.get(0)).isEqualTo("Custom runtime exception");
    assertThat(debugLogs.get(1)).startsWith("org.sonar.iac.common.extension.ParseException: Cannot parse 'FileWithContent'");
  }

  @Test
  void shouldReportErrorOnVisitorScanException() throws IOException {
    TreeVisitor<InputFileContext> visitorFail = mock(TreeVisitor.class);
    doThrow(new RuntimeException("Exception when scan mock"))
      .when(visitorFail).scan(any(InputFileContext.class), any(Tree.class));
    Analyzer analyzer = new Analyzer("iac", parser, List.of(visitorFail), durationStatistics);
    List<InputFile> files = List.of(file("Some content"));
    analyzer.analyseFiles(context, files, progressReport);

    assertThat(logTester.logs(Level.ERROR)).containsExactly("Cannot analyse 'FileWithContent': Exception when scan mock");
  }

  @Test
  void shouldReportIllegalStateExceptionWhenFailFastIsTrue() throws IOException {
    TreeVisitor<InputFileContext> visitorFail = mock(TreeVisitor.class);
    doThrow(new RuntimeException("Exception when scan mock"))
      .when(visitorFail).scan(any(InputFileContext.class), any(Tree.class));
    Analyzer analyzer = new Analyzer("iac", parser, List.of(visitorFail), durationStatistics);
    List<InputFile> files = List.of(file("Some content"));
    MapSettings settings = new MapSettings();
    settings.setProperty(IacSensor.FAIL_FAST_PROPERTY_NAME, true);
    context.setSettings(settings);

    assertThatThrownBy(() -> analyzer.analyseFiles(context, files, progressReport))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Exception when analyzing 'FileWithContent'");
    assertThat(logTester.logs(Level.ERROR)).containsExactly("Cannot analyse 'FileWithContent': Exception when scan mock");
  }

  @Test
  void shouldParseAndVisitWithSuccess() throws IOException {
    TreeVisitor<InputFileContext> visitor = mock(TreeVisitor.class);
    Analyzer analyzer = new Analyzer("iac", parser, List.of(visitor), durationStatistics);
    List<InputFile> files = List.of(file("Some content"));

    assertThat(analyzer.analyseFiles(context, files, progressReport)).isTrue();
  }

  SensorContext sensorContext(boolean cancelled) {
    SensorContext mock = mock(SensorContext.class);
    when(mock.isCancelled()).thenReturn(cancelled);
    var analysisError = mock(DefaultAnalysisError.class);
    when(analysisError.message(anyString())).thenReturn(analysisError);
    when(mock.newAnalysisError()).thenReturn(analysisError);
    return mock;
  }

  InputFile file(String content) throws IOException {
    InputFile file = mock(InputFile.class);
    when(file.contents()).thenReturn(content);
    when(file.toString()).thenReturn("FileWithContent");
    return file;
  }

  InputFile emptyFile() throws IOException {
    InputFile file = mock(InputFile.class);
    when(file.contents()).thenReturn("  ");
    when(file.toString()).thenReturn("EmptyFile");
    return file;
  }

  InputFile invalidFile() throws IOException {
    InputFile file = mock(InputFile.class);
    when(file.contents()).thenThrow(new IOException("Invalid file mock"));
    when(file.toString()).thenReturn("InvalidFile");
    return file;
  }

  TreeParser parser() {
    TreeParser parser = mock(TreeParser.class);
    when(parser.parse(anyString(), any(InputFileContext.class))).thenReturn(mock(Tree.class));
    return parser;
  }
}
