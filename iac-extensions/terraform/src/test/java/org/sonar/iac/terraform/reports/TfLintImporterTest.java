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

    File someFile = new File("src/test/resources/tflint/exampleIssues.tf");
    context.fileSystem().add(new TestInputFileBuilder("project", baseDir, someFile).setContents(new String(Files.readAllBytes(someFile.toPath()))).build());
  }

  @Test
  void shouldImportExampleReport() {
    File reportFile = new File("src/test/resources/tflint/exampleIssues.json");
    TfLintImporter importer = new TfLintImporter(context, mockAnalysisWarnings);

    importer.importReport(reportFile);

    context.allExternalIssues().forEach(System.out::println);
    logTester.logs(LoggerLevel.WARN).forEach(System.out::println);

    assertThat(context.allExternalIssues()).hasSize(1);
    ExternalIssue issue = context.allExternalIssues().iterator().next();
    AssertionsForClassTypes.assertThat(issue.ruleId()).isEqualTo("terraform_comment_syntax");
    assertThat(issue.type()).isEqualTo(RuleType.CODE_SMELL);
    AssertionsForClassTypes.assertThat(issue.primaryLocation().message()).isEqualTo("Single line comments should begin with #");
    // TODO text range assert
    AssertionsForClassTypes.assertThat(issue.primaryLocation().textRange().start().line()).isEqualTo(2);
    AssertionsForClassTypes.assertThat(issue.primaryLocation().textRange().start().lineOffset()).isEqualTo(2);
    verifyNoInteractions(mockAnalysisWarnings);
  }
}
