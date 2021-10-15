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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.ExternalIssue;
import org.sonar.api.rules.RuleType;
import org.sonar.api.utils.log.LogTesterJUnit5;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.iac.cloudformation.reports.CfnLintImporter;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class CfnLintImporterTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();
  private SensorContextTester context;

  @BeforeEach
  void setUp() throws IOException {
    File baseDir = new File("src/test/resources/cfn-lint");
    context = SensorContextTester.create(baseDir);

    File someFile = new File("src/test/resources/cfn-lint/template.yaml");
    context.fileSystem().add(new TestInputFileBuilder("project", baseDir, someFile).setContents(new String(Files.readAllBytes(someFile.toPath()))).build());
  }

  @ParameterizedTest
  @CsvSource({
    "src/test/resources/cfn-lint/doesNotExist.json, Cfn-lint report importing: path does not seem to point to a file %s",
    "src/test/resources/cfn-lint/parseError.json, Cfn-lint report importing: could not parse file as JSON %s",
    "src/test/resources/cfn-lint/noArray.json, Cfn-lint report importing: file is expected to contain a JSON array but didn't %s",
  })
  void problem_when_reading_or_parsing_file(String reportPath, String expectedLog) {
    File reportFile = new File(reportPath);
    CfnLintImporter.importReport(context, reportFile);
    assertThat(logTester.logs(LoggerLevel.WARN))
      .containsExactly(String.format(expectedLog, reportPath));
  }

  @Test
  void no_issues() {
    File reportFile = new File("src/test/resources/cfn-lint/emptyArray.json");
    CfnLintImporter.importReport(context, reportFile);
    assertThat(context.allExternalIssues()).isEmpty();
  }

  @Test
  void invalid_issue() {
    File reportFile = new File("src/test/resources/cfn-lint/invalidIssue.json");
    CfnLintImporter.importReport(context, reportFile);
    assertThat(context.allExternalIssues()).isEmpty();
    assertThat(logTester.logs(LoggerLevel.WARN))
      .containsExactly(String.format("Cfn-lint report importing: could not save 1 out of 1 issues from %s", reportFile.getPath()));
  }

  @Test
  void valid_issue() {
    File reportFile = new File("src/test/resources/cfn-lint/validIssue.json");
    CfnLintImporter.importReport(context, reportFile);
    assertThat(context.allExternalIssues()).hasSize(1);
    ExternalIssue issue = context.allExternalIssues().iterator().next();
    assertThat(issue.ruleId()).isEqualTo("E0000");
    assertThat(issue.type()).isEqualTo(RuleType.BUG);
    assertThat(issue.primaryLocation().message()).isEqualTo("Null value at line 8 column 20");
    assertThat(issue.primaryLocation().textRange().start().line()).isEqualTo(8);
  }

  @Test
  void one_invalid_and_one_valid_issue() {
    File reportFile = new File("src/test/resources/cfn-lint/validAndInvalid.json");
    CfnLintImporter.importReport(context, reportFile);
    assertThat(context.allExternalIssues()).hasSize(1);
    assertThat(logTester.logs(LoggerLevel.WARN))
      .containsExactly(String.format("Cfn-lint report importing: could not save 1 out of 2 issues from %s", reportFile.getPath()));
  }

  @Test
  void unknown_rule() {
    File reportFile = new File("src/test/resources/cfn-lint/unknownRule.json");
    CfnLintImporter.importReport(context, reportFile);
    assertThat(context.allExternalIssues()).hasSize(1);
    ExternalIssue issue = context.allExternalIssues().iterator().next();
    assertThat(issue.ruleId()).isEqualTo("cfn-lint.fallback");
    assertThat(issue.type()).isEqualTo(RuleType.CODE_SMELL);
  }
}
