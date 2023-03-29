/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
package org.sonar.iac.terraform.reports;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.ExternalIssue;
import org.sonar.api.rules.RuleType;
import org.sonar.api.utils.log.LogTesterJUnit5;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class TfLintImporterTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();

  private SensorContextTester context;
  private final AnalysisWarningsWrapper mockAnalysisWarnings = mock(AnalysisWarningsWrapper.class);

  @BeforeEach
  void setUp() throws IOException {
    File baseDir = new File("src/test/resources/tflint");
    context = SensorContextTester.create(baseDir);

    addFileToContext(baseDir, "src/test/resources/tflint/exampleIssues.tf");
    addFileToContext(baseDir, "src/test/resources/tflint/exampleError.tf");
  }

  private void addFileToContext(File baseDir, String path) throws IOException {
    File someFile = new File(path);
    context.fileSystem().add(new TestInputFileBuilder("project", baseDir, someFile).setContents(new String(Files.readAllBytes(someFile.toPath()))).build());
  }

  @Test
  void shouldImportExampleIssue() {
    File reportFile = new File("src/test/resources/tflint/exampleIssues.json");
    TfLintImporter importer = new TfLintImporter(context, mockAnalysisWarnings);

    importer.importReport(reportFile);

    assertThat(context.allExternalIssues()).hasSize(1);
    ExternalIssue issue = context.allExternalIssues().iterator().next();
    assertThat(issue.ruleId()).isEqualTo("terraform_comment_syntax");
    assertThat(issue.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(issue.primaryLocation().message()).isEqualTo("Single line comments should begin with #");
    assertTextRange(issue.primaryLocation().textRange(), 2, 0, 3, 0);
    verifyNoInteractions(mockAnalysisWarnings);
  }

  @Test
  void shouldImportExampleError() {
    File reportFile = new File("src/test/resources/tflint/exampleError.json");
    TfLintImporter importer = new TfLintImporter(context, mockAnalysisWarnings);

    importer.importReport(reportFile);

    context.allExternalIssues().forEach(System.out::println);

    assertThat(context.allExternalIssues()).hasSize(1);
    ExternalIssue issue = context.allExternalIssues().iterator().next();
    assertThat(issue.ruleId()).isEqualTo("tflint.error");
    assertThat(issue.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(issue.primaryLocation().message()).isEqualTo(
      "Failed to check ruleset; Failed to check `aws_instance_previous_type` rule: exampleError.tf:2,21-29: Reference to undeclared input variable; An input variable with the name \"type\" has not been declared. This variable can be declared with a variable \"type\" {} block.");
    assertTextRange(issue.primaryLocation().textRange(), 2, 0, 2, 26);
    verifyNoInteractions(mockAnalysisWarnings);
  }

  @ParameterizedTest
  @CsvSource({
    "src/test/resources/tflint/doesNotExist.json, TFLint report importing: path does not seem to point to a file %s",
    "src/test/resources/tflint/parseError.json, TFLint report importing: could not parse file as JSON %s"})
  void shouldLogWarningWhenImport(String reportPath, String expectedLog) {
    String path = File.separatorChar == '/' ? reportPath : Paths.get(reportPath).toString();
    File reportFile = new File(path);
    TfLintImporter importer = new TfLintImporter(context, mockAnalysisWarnings);

    importer.importReport(reportFile);

    context.allExternalIssues().forEach(System.out::println);
    assertThat(logTester.logs(LoggerLevel.WARN)).containsExactly(String.format(expectedLog, path));
  }

  private void assertTextRange(TextRange actual, int startLine, int startLineOffset, int endLine, int endLineOffset) {
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(actual.start().line()).as("startLine mismatch").isEqualTo(startLine);
      softly.assertThat(actual.start().lineOffset()).as("startLineOffset mismatch").isEqualTo(startLineOffset);
      softly.assertThat(actual.end().line()).as("endLine mismatch").isEqualTo(endLine);
      softly.assertThat(actual.end().lineOffset()).as("endLineOffset mismatch").isEqualTo(endLineOffset);
    });
  }
}
