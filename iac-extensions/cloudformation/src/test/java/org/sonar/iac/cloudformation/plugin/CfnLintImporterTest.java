/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
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
}
