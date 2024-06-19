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
package org.sonar.iac.common.extension.analyzer;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.IacSensor;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.testing.IacTestUtils;
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

abstract class AbstractAnalyzerTest {

  @TempDir
  protected File tmpDir;
  protected File baseDir;

  protected SensorContextTester context;
  protected TreeParser parser;
  protected DurationStatistics durationStatistics;

  protected InputFile emptyFile;
  protected InputFile fileWithContent;
  protected TreeVisitor<InputFileContext> checksVisitor;

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  @BeforeEach
  void init() throws IOException {
    parser = parser();
    durationStatistics = new DurationStatistics(mock(Configuration.class));
    baseDir = tmpDir.toPath().toRealPath().resolve("test-project").toFile();
    FileUtils.forceMkdir(baseDir);
    context = SensorContextTester.create(baseDir);
    emptyFile = IacTestUtils.inputFile("empty.txt", baseDir.toPath(), "  ", null);
    fileWithContent = IacTestUtils.inputFile("file.txt", baseDir.toPath(), "Some content", null);
    checksVisitor = mock(TreeVisitor.class);
  }

  abstract Analyzer analyzer(List<TreeVisitor<InputFileContext>> visitors, TreeVisitor<InputFileContext> checksVisitor);

  @Test
  void shouldParseEmptyFile() {
    Analyzer analyzer = analyzer(Collections.emptyList(), checksVisitor);
    List<InputFile> files = List.of(emptyFile);
    assertThat(analyzer.analyseFiles(context, files, "iac")).isTrue();
  }

  @Test
  void shouldFailWhenCancelled() {
    Analyzer analyzer = analyzer(Collections.emptyList(), checksVisitor);
    List<InputFile> files = List.of(emptyFile);
    context.setCancelled(true);
    assertThat(analyzer.analyseFiles(context, files, "iac")).isFalse();
  }

  @Test
  void shouldReportParsingErrorOnInvalidFile() throws IOException {
    Analyzer analyzer = analyzer(Collections.emptyList(), checksVisitor);
    List<InputFile> files = List.of(IacTestUtils.invalidInputFile());
    assertThat(analyzer.analyseFiles(context, files, "iac")).isTrue();
    assertThat(logTester.logs(Level.ERROR)).containsExactly("Cannot read 'InvalidFile'");

    List<String> debugLogs = logTester.logs(Level.DEBUG);
    assertThat(debugLogs).hasSize(2);
    assertThat(debugLogs.get(0)).isEqualTo("Invalid file mock");
    assertThat(debugLogs.get(1)).startsWith("org.sonar.iac.common.extension.ParseException: Cannot read 'InvalidFile'");
  }

  @Test
  void shouldReportOnParseException() {
    when(parser.parse(anyString(), any(InputFileContext.class))).thenThrow(new ParseException("Custom parse exception", null, null));
    Analyzer analyzer = analyzer(Collections.emptyList(), checksVisitor);
    List<InputFile> files = List.of(fileWithContent);
    analyzer.analyseFiles(context, files, "iac");

    assertThat(logTester.logs(Level.ERROR)).containsExactly("Custom parse exception");
    List<String> debugLogs = logTester.logs(Level.DEBUG);
    assertThat(debugLogs).hasSize(1);
    assertThat(debugLogs.get(0)).startsWith("org.sonar.iac.common.extension.ParseException: Custom parse exception");
  }

  @Test
  void shouldReportOnRuntimeException() {
    when(parser.parse(anyString(), any(InputFileContext.class))).thenThrow(new RuntimeException("Custom runtime exception"));
    Analyzer analyzer = analyzer(Collections.emptyList(), checksVisitor);
    List<InputFile> files = List.of(fileWithContent);
    analyzer.analyseFiles(context, files, "iac");

    assertThat(logTester.logs(Level.ERROR)).containsExactly("Cannot parse 'file.txt'");
    List<String> debugLogs = logTester.logs(Level.DEBUG);
    assertThat(debugLogs).hasSize(2);
    assertThat(debugLogs.get(0)).isEqualTo("Custom runtime exception");
    assertThat(debugLogs.get(1)).startsWith("org.sonar.iac.common.extension.ParseException: Cannot parse 'file.txt'");
  }

  @Test
  void shouldReportErrorOnVisitorScanException() {
    TreeVisitor<InputFileContext> visitorFail = mock(TreeVisitor.class);
    doThrow(new RuntimeException("Exception when scan mock"))
      .when(visitorFail).scan(any(InputFileContext.class), any(Tree.class));
    Analyzer analyzer = analyzer(List.of(visitorFail), checksVisitor);
    List<InputFile> files = List.of(fileWithContent);
    analyzer.analyseFiles(context, files, "iac");

    assertThat(logTester.logs(Level.ERROR)).containsExactly("Cannot analyse 'file.txt': Exception when scan mock");
  }

  @Test
  void shouldReportIllegalStateExceptionWhenFailFastIsTrue() {
    TreeVisitor<InputFileContext> visitorFail = mock(TreeVisitor.class);
    doThrow(new RuntimeException("Exception when scan mock"))
      .when(visitorFail).scan(any(InputFileContext.class), any(Tree.class));
    Analyzer analyzer = analyzer(List.of(visitorFail), checksVisitor);
    List<InputFile> files = List.of(fileWithContent);
    MapSettings settings = new MapSettings();
    settings.setProperty(IacSensor.FAIL_FAST_PROPERTY_NAME, true);
    context.setSettings(settings);

    assertThatThrownBy(() -> analyzer.analyseFiles(context, files, "iac"))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Exception when analyzing 'file.txt'");
    assertThat(logTester.logs(Level.ERROR)).containsExactly("Cannot analyse 'file.txt': Exception when scan mock");
  }

  @Test
  void shouldParseAndVisitWithSuccess() {
    TreeVisitor<InputFileContext> visitor = mock(TreeVisitor.class);
    Analyzer analyzer = analyzer(List.of(visitor), checksVisitor);
    List<InputFile> files = List.of(fileWithContent);

    assertThat(analyzer.analyseFiles(context, files, "iac")).isTrue();
  }

  TreeParser parser() {
    TreeParser treeParser = mock(TreeParser.class);
    when(treeParser.parse(anyString(), any(InputFileContext.class))).thenReturn(mock(Tree.class));
    return treeParser;
  }
}
