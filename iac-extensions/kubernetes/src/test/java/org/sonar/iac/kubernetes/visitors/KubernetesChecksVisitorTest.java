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

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.helm.HelmFileSystem;
import org.sonar.iac.kubernetes.plugin.filesystem.FileSystemProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class KubernetesChecksVisitorTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.TRACE);
  private FileSystemProvider fileSystemProvider = mock(FileSystemProvider.class);

  private static final ProjectContext PROJECT_CONTEXT = mock(ProjectContext.class);
  private final KubernetesChecksVisitor visitor = new KubernetesChecksVisitor(mock(Checks.class),
    new DurationStatistics(mock(Configuration.class)), PROJECT_CONTEXT, fileSystemProvider);
  private KubernetesChecksVisitor.KubernetesContextAdapter context;
  private static final TextRange TREE_TEXT_RANGE = range(1, 0, 1, 1);
  private final Tree tree = mock(Tree.class);

  @BeforeEach
  void setUp() {
    context = (KubernetesChecksVisitor.KubernetesContextAdapter) visitor.context(RuleKey.of("testRepo", "testRule"));
    IacCheck validCheck = init -> init.register(Tree.class, (ctx, tree) -> ctx.reportIssue(tree.textRange(), "testIssue"));
    validCheck.initialize(context);
    when(tree.textRange()).thenReturn(TREE_TEXT_RANGE);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void shouldReportSecondaryInValuesWithProperty(boolean isPropertyEnabled) {
    var inputFileContext = createHelmInputFileContextMock(isPropertyEnabled);

    visitor.scan(inputFileContext, tree);
    assertTraceLog(isPropertyEnabled);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void shouldReportSecondaryLocationAccordingToContext(boolean shouldReport) {
    var inputFileContext = createHelmInputFileContextMock(false);
    context.setShouldReportSecondaryInValues(shouldReport);

    visitor.scan(inputFileContext, tree);
    assertTraceLog(shouldReport);
  }

  @Test
  void shouldReturnCurrentInputFileContext() {
    var checkContext = (KubernetesCheckContext) visitor.context(RuleKey.of("kubernetes", "S0000"));
    assertThat(checkContext.inputFileContext()).isNull();
    var inputFileContext = createHelmInputFileContextMock(false);

    ((KubernetesChecksVisitor.KubernetesContextAdapter) checkContext).register(Tree.class, (ctx, node) -> {
    });
    visitor.scan(inputFileContext, tree);
    assertThat(checkContext.inputFileContext()).isEqualTo(inputFileContext);
  }

  @Test
  void shouldReturnProjectContext() {
    KubernetesCheckContext checkContext = (KubernetesCheckContext) visitor.context(null);
    assertThat(checkContext.projectContext()).isEqualTo(PROJECT_CONTEXT);
  }

  @Test
  void shouldReportWhenTextRangeIsNull() {
    var inputFileContext = createHelmInputFileContextMock(false);

    when(tree.textRange()).thenReturn(null);
    visitor.scan(inputFileContext, tree);
    verify(inputFileContext).reportIssue(RuleKey.of("testRepo", "testRule"), null, "testIssue", List.of());
  }

  @Test
  void shouldReportWithSecondaryLocationOnSameFile() {
    var inputFileContext = createHelmInputFileContextMock(false);
    var secondaryLocation = new SecondaryLocation(range(1, 0, 2, 3), "testIssueSecondary");
    var customVisitor = prepareVisitorToRaise("testIssue", List.of(secondaryLocation));
    customVisitor.scan(inputFileContext, tree);
    verify(inputFileContext).reportIssue(RuleKey.of("testRepo", "testRule"), TREE_TEXT_RANGE, "testIssue", List.of(secondaryLocation));
  }

  @Test
  void shouldReportWithSecondaryLocationOnDifferentFile() {
    var inputFileContext = createHelmInputFileContextMock(false);
    var secondaryLocation = new SecondaryLocation(range(1, 0, 2, 3), "testIssueSecondary", "my/other/file.yaml");
    var customVisitor = prepareVisitorToRaise("testIssue", List.of(secondaryLocation));
    customVisitor.scan(inputFileContext, tree);
    verify(inputFileContext).reportIssue(RuleKey.of("testRepo", "testRule"), TREE_TEXT_RANGE, "testIssue", List.of(secondaryLocation));
  }

  @Test
  void shouldDiscardSecondaryLocationOnDifferentFileWhenDisabled() {
    var inputFileContext = createHelmInputFileContextMock(false);
    when(inputFileContext.sensorContext.config().getBoolean(KubernetesChecksVisitor.DISABLE_SECONDARY_LOCATIONS_IN_OTHER_YAML_KEY + ".testRule")).thenReturn(Optional.of(true));
    TextRange range = range(1, 0, 1, 1);
    var secondaryLocationSameFile = new SecondaryLocation(range(1, 0, 2, 3), "testIssueSecondary");
    var secondaryLocationOtherFile = new SecondaryLocation(range(1, 0, 2, 3), "testIssueSecondary", "my/other/file.yaml");
    var customVisitor = prepareVisitorToRaise("testIssue", List.of(secondaryLocationSameFile, secondaryLocationOtherFile));
    customVisitor.scan(inputFileContext, tree);
    verify(inputFileContext).reportIssue(RuleKey.of("testRepo", "testRule"), range, "testIssue", List.of(secondaryLocationSameFile));
  }

  @Test
  void shouldReportWithSecondaryLocationOnDifferentFileWhenProvidedNormalInputFileContext() {
    var inputFileContext = createHelmInputFileContextMock(false);
    var secondaryInputFileContext = createHelmInputFileContextMock(false);
    var secondaryLocation = new SecondaryLocation(range(1, 0, 2, 3), "testIssueSecondary", "my/other/file.yaml");
    var customVisitor = prepareVisitorToRaise("testIssue", List.of(secondaryLocation), prepareProjectContext("my/other/file.yaml", secondaryInputFileContext));
    customVisitor.scan(inputFileContext, tree);
    verify(inputFileContext).reportIssue(RuleKey.of("testRepo", "testRule"), TREE_TEXT_RANGE, "testIssue", List.of(secondaryLocation));
  }

  @Test
  void shouldReportWithSecondaryLocationOnDifferentFileWithShiftedLocation() {
    var inputFileContext = createHelmInputFileContextMock(false);
    var secondaryInputFileContext = createHelmInputFileContextMock("my/other/file.yaml");
    secondaryInputFileContext.sourceMap().addLineData(1, 3, 3);
    var providedSecondaryLocation = new SecondaryLocation(range(1, 0, 2, 3), "testIssueSecondary", "my/other/file.yaml");
    var expectedSecondaryLocation = new SecondaryLocation(range(3, 0, 0, 0), "testIssueSecondary", "my/other/file.yaml");
    var customVisitor = prepareVisitorToRaise("testIssue", List.of(providedSecondaryLocation), prepareProjectContext("my/other/file.yaml", secondaryInputFileContext));
    customVisitor.scan(inputFileContext, tree);
    verify(inputFileContext).reportIssue(RuleKey.of("testRepo", "testRule"), TREE_TEXT_RANGE, "testIssue", List.of(expectedSecondaryLocation));
  }

  @Test
  void shouldCallReportIssueNoLineShift() {
    var textRange = range(1, 2, 3, 4);
    var customVisitor = prepareVisitorToRaiseNoLineShift("message", textRange, mock(ProjectContext.class));
    var inputFileContext = createHelmInputFileContextMock(false);

    customVisitor.scan(inputFileContext, tree);

    verify(inputFileContext).reportIssue(RuleKey.of("testRepo", "testRule"), textRange, "message", List.of());
  }

  @Test
  void shouldNotCalculateLineShiftingIfNotHelmInputFileContext() {
    var inputFile = createInputFileMock("foo.yaml");
    var sensorContext = createSensorContextMock(inputFile);
    var inputFileContext = spy(new InputFileContext(sensorContext, inputFile));
    doNothing().when(inputFileContext).reportIssue(any(), any(), any(), any());
    var customVisitor = prepareVisitorToRaise("message", List.of());

    customVisitor.scan(inputFileContext, tree);

    verify(inputFileContext).reportIssue(RuleKey.of("testRepo", "testRule"), TREE_TEXT_RANGE, "message", List.of());
  }

  private void assertTraceLog(boolean shouldContainLog) {
    var traceLogs = logTester.logs(Level.TRACE);
    if (shouldContainLog) {
      assertThat(traceLogs).containsExactly("Find secondary location for issue in additional files for textRange [1:0/1:1] in file " +
        "dir1/dir2/testFile");
    } else {
      assertThat(traceLogs).isEmpty();
    }
  }

  private InputFileContext createHelmInputFileContextMock(boolean isPropertyEnabled) {
    var inputFileContext = spy(createHelmInputFileContextMock("testFile"));
    var config = mock(Configuration.class);
    when(config.getBoolean(KubernetesChecksVisitor.ENABLE_SECONDARY_LOCATIONS_IN_VALUES_YAML_KEY)).thenReturn(Optional.of(isPropertyEnabled));
    when(inputFileContext.sensorContext.config()).thenReturn(config);
    var fileSystem = mock(FileSystem.class);
    when(fileSystem.predicates()).thenReturn(mock(FilePredicates.class));
    when(inputFileContext.sensorContext.fileSystem()).thenReturn(fileSystem);
    doNothing().when(inputFileContext).reportIssue(any(), any(), any(), any());

    return inputFileContext;
  }

  public static HelmInputFileContext createHelmInputFileContextMock(String filename) {
    var inputFile = createInputFileMock(filename);
    var sensorContext = createSensorContextMock(inputFile);
    try (var ignored = mockStatic(HelmFileSystem.class)) {
      when(HelmFileSystem.retrieveHelmProjectFolder(any(), any())).thenReturn(Path.of("dir1"));
      return new HelmInputFileContext(sensorContext, inputFile);
    }
  }

  private static InputFile createInputFileMock(String filename) {
    var inputFile = mock(InputFile.class);
    when(inputFile.toString()).thenReturn("dir1/dir2/" + filename);
    when(inputFile.filename()).thenReturn(filename);
    when(inputFile.uri()).thenReturn(URI.create("file:///dir1/dir2/" + filename));
    return inputFile;
  }

  private static SensorContext createSensorContextMock(InputFile inputFile) {
    var sensorContext = mock(SensorContext.class);
    var fileSystem = mock(FileSystem.class);
    var filePredicates = mock(FilePredicates.class);
    when(filePredicates.is(any())).thenReturn(input -> true);
    when(fileSystem.inputFile(any())).thenReturn(inputFile);
    when(fileSystem.predicates()).thenReturn(filePredicates);
    when(sensorContext.fileSystem()).thenReturn(fileSystem);
    return sensorContext;
  }

  private KubernetesChecksVisitor prepareVisitorToRaise(String message, List<SecondaryLocation> secondaryLocations) {
    return prepareVisitorToRaise(message, secondaryLocations, PROJECT_CONTEXT);
  }

  private KubernetesChecksVisitor prepareVisitorToRaise(String message, List<SecondaryLocation> secondaryLocations, ProjectContext projectContext) {
    KubernetesChecksVisitor specificVisitor = new KubernetesChecksVisitor(mock(Checks.class), new DurationStatistics(mock(Configuration.class)), projectContext,
      fileSystemProvider);
    KubernetesChecksVisitor.KubernetesContextAdapter specificContext = (KubernetesChecksVisitor.KubernetesContextAdapter) specificVisitor
      .context(RuleKey.of("testRepo", "testRule"));
    IacCheck validCheckWithSecondaryLocation = init -> init.register(Tree.class, (ctx, node) -> ctx.reportIssue(node, message, secondaryLocations));
    validCheckWithSecondaryLocation.initialize(specificContext);
    return specificVisitor;
  }

  private KubernetesChecksVisitor prepareVisitorToRaiseNoLineShift(String message, TextRange textRange, ProjectContext projectContext) {
    KubernetesChecksVisitor specificVisitor = new KubernetesChecksVisitor(mock(Checks.class), new DurationStatistics(mock(Configuration.class)), projectContext);
    KubernetesChecksVisitor.KubernetesContextAdapter specificContext = (KubernetesChecksVisitor.KubernetesContextAdapter) specificVisitor
      .context(RuleKey.of("testRepo", "testRule"));
    IacCheck validCheckWithSecondaryLocation = init -> init.register(Tree.class, (ctx, node) -> ((KubernetesCheckContext) ctx).reportIssueNoLineShift(textRange, message));
    validCheckWithSecondaryLocation.initialize(specificContext);
    return specificVisitor;
  }

  private ProjectContext prepareProjectContext(String path, InputFileContext associatedInputFileContext) {
    ProjectContext projectContext = mock(ProjectContext.class);
    when(projectContext.getInputFileContext(path)).thenReturn(associatedInputFileContext);
    return projectContext;
  }
}
