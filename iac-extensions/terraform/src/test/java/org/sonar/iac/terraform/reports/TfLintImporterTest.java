package org.sonar.iac.terraform.reports;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.ExternalIssue;
import org.sonar.api.rules.RuleType;
import org.sonar.api.utils.log.LogTesterJUnit5;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class TfLintImporterTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();

  private SensorContextTester context;
  private final AnalysisWarningsWrapper mockAnalysisWarnings = mock(AnalysisWarningsWrapper.class);

  @BeforeEach
  void setUp() {
    File baseDir = new File("src/test/resources/tflint");
    context = SensorContextTester.create(baseDir);
  }

  @Test
  void shouldImportExampleReport() {
    File reportFile = new File("src/test/resources/tflint/exampleTfLintReport.json");
    TfLintImporter importer = new TfLintImporter(context, mockAnalysisWarnings);

    importer.importReport(reportFile);

    assertThat(context.allExternalIssues()).hasSize(1);
    ExternalIssue issue = context.allExternalIssues().iterator().next();
    AssertionsForClassTypes.assertThat(issue.ruleId()).isEqualTo("E0000");
    assertThat(issue.type()).isEqualTo(RuleType.BUG);
    AssertionsForClassTypes.assertThat(issue.primaryLocation().message()).isEqualTo("Null value at line 8 column 20");
    AssertionsForClassTypes.assertThat(issue.primaryLocation().textRange().start().line()).isEqualTo(8);
    verifyNoInteractions(mockAnalysisWarnings);
  }
}
