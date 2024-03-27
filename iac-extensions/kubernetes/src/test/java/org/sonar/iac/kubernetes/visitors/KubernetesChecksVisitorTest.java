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
package org.sonar.iac.kubernetes.visitors;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.visitors.InputFileContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class KubernetesChecksVisitorTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.TRACE);

  private static final ProjectContext PROJECT_CONTEXT = mock(ProjectContext.class);
  private final KubernetesChecksVisitor visitor = new KubernetesChecksVisitor(mock(Checks.class), new DurationStatistics(mock(Configuration.class)), PROJECT_CONTEXT);
  private KubernetesChecksVisitor.KubernetesContextAdapter context;
  private Tree tree = mock(Tree.class);

  @BeforeEach
  void setUp() {
    context = (KubernetesChecksVisitor.KubernetesContextAdapter) visitor.context(RuleKey.of("testRepo", "testRule"));
    IacCheck validCheck = init -> init.register(Tree.class, (ctx, tree) -> ctx.reportIssue(tree.textRange(), "testIssue"));
    validCheck.initialize(context);
    when(tree.textRange()).thenReturn(range(1, 0, 1, 1));
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void shouldReportSecondaryInValuesWithProperty(boolean isPropertyEnabled) {
    var inputFileContext = mockInputFileContext(isPropertyEnabled);

    visitor.scan(inputFileContext, tree);
    assertTraceLog(isPropertyEnabled);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void shouldReportSecondaryLocationAccordingToContext(boolean shouldReport) {
    var inputFileContext = mockInputFileContext(false);
    context.setShouldReportSecondaryInValues(shouldReport);

    visitor.scan(inputFileContext, tree);
    assertTraceLog(shouldReport);
  }

  @Test
  void shouldReturnProjectContext() {
    KubernetesCheckContext checkContext = (KubernetesCheckContext) visitor.context(null);
    assertThat(checkContext.projectContext()).isEqualTo(PROJECT_CONTEXT);
  }

  private void assertTraceLog(boolean shouldContainLog) {
    var traceLogs = logTester.logs(Level.TRACE);
    if (shouldContainLog) {
      assertThat(traceLogs).containsExactly("Find secondary location for issue in additional files for textRange [1:0/1:1] in file dir1/dir2/testFile");
    } else {
      assertThat(traceLogs).isEmpty();
    }
  }

  private InputFileContext mockInputFileContext(boolean isPropertyEnabled) {
    var inputFileContext = spy(createInputFileContextMock("testFile"));
    var config = mock(Configuration.class);
    when(config.getBoolean(KubernetesChecksVisitor.ENABLE_SECONDARY_LOCATIONS_IN_VALUES_YAML_KEY)).thenReturn(Optional.of(isPropertyEnabled));
    when(inputFileContext.sensorContext.config()).thenReturn(config);
    doNothing().when(inputFileContext).reportIssue(any(), any(), any(), any());

    return inputFileContext;
  }

  public static HelmInputFileContext createInputFileContextMock(String filename) {
    var inputFile = mock(InputFile.class);
    var inputFileContext = new HelmInputFileContext(mock(SensorContext.class), inputFile);
    when(inputFile.toString()).thenReturn("dir1/dir2/" + filename);
    when(inputFile.filename()).thenReturn(filename);
    return inputFileContext;
  }
}
