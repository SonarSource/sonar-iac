/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.kubernetes.plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.kubernetes.model.LimitRange;
import org.sonarsource.sonarlint.core.analysis.container.module.DefaultModuleFileEvent;
import org.sonarsource.sonarlint.plugin.api.module.file.ModuleFileEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.testing.IacTestUtils.inputFile;
import static org.sonarsource.sonarlint.plugin.api.module.file.ModuleFileEvent.Type.CREATED;

class SonarLintFileListenerTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  private static final Path BASE_DIR = Path.of("src/test/resources/SonarLintFileListener");

  private SonarLintFileListener sonarLintFileListener;
  private SensorContext context;
  private InputFile inputFile1;
  private InputFile inputFile2;
  private InputFile inputFileJava;
  private InputFile inputFileNoLanguage;
  private InputFile inputFileTOException;

  @BeforeEach
  public void init() throws IOException {
    inputFile1 = inputFile("limit_range.yaml", BASE_DIR, "yaml");
    inputFile2 = inputFile("memory_limit_pod.yaml", BASE_DIR, "yaml");
    inputFileJava = inputFile("FactoryBuilder.java", BASE_DIR, "java");
    inputFileNoLanguage = inputFile("FactoryBuilder.java", BASE_DIR, null);
    inputFileTOException = spy(inputFile2);
    when(inputFileTOException.contents()).thenThrow(new IOException("Boom"));
    var inputFiles = List.of(inputFile1, inputFile2);
    var moduleFileSystem = new TestModuleFileSystem(inputFiles);
    sonarLintFileListener = new SonarLintFileListener(moduleFileSystem);
    context = SensorContextTester.create(BASE_DIR);
  }

  @Test
  void shouldStoreFilesContentsWhenInit() {
    sonarLintFileListener.initContext(context, null);

    assertThat(sonarLintFileListener.inputFilesContents().keySet())
      .allMatch(key -> key.endsWith("limit_range.yaml") || key.endsWith("memory_limit_pod.yaml"));
    assertThat(logTester.logs(Level.INFO)).contains("Finished building Kubernetes Project Context");
  }

  @Test
  void shouldRemoveResourceWhenRemoveEvent() {
    sonarLintFileListener.initContext(context, null);
    var event = DefaultModuleFileEvent.of(inputFile1, ModuleFileEvent.Type.DELETED);

    sonarLintFileListener.process(event);

    assertThat(sonarLintFileListener.inputFilesContents().keySet())
      .allMatch(key -> key.endsWith("memory_limit_pod.yaml"));
    assertThat(logTester.logs(Level.INFO)).contains(
      "Module file event DELETED for file limit_range.yaml",
      "Kubernetes Project Context updated");
  }

  @Test
  void shouldNotModifyProjectContextOrContentsWhenNotInitialized() {
    var event = DefaultModuleFileEvent.of(inputFile1, ModuleFileEvent.Type.MODIFIED);

    sonarLintFileListener.process(event);

    var inputFileContext2 = new InputFileContext(context, inputFile2);
    var projectResources = sonarLintFileListener.getProjectContext().getProjectResources(
      "with-global-limit", inputFileContext2, LimitRange.class);
    assertThat(projectResources).isEmpty();
    assertThat(sonarLintFileListener.inputFilesContents()).isEmpty();
    assertThat(logTester.logs(Level.INFO)).contains(
      "Module file event MODIFIED for file limit_range.yaml, ignored as context was not initialized");
  }

  @Test
  void shouldIgnoreJavaFile() {
    var event = DefaultModuleFileEvent.of(inputFileJava, ModuleFileEvent.Type.MODIFIED);

    sonarLintFileListener.process(event);

    assertThat(logTester.logs(Level.INFO))
      .contains("Module file event for MODIFIED for file FactoryBuilder.java has been ignored because it's not a Kubernetes file.");
  }

  @Test
  void shouldIgnoreFileWithoutLanguage() {
    var event = DefaultModuleFileEvent.of(inputFileNoLanguage, ModuleFileEvent.Type.MODIFIED);

    sonarLintFileListener.process(event);

    assertThat(logTester.logs(Level.INFO))
      .contains("Module file event for MODIFIED for file FactoryBuilder.java has been ignored because it's not a Kubernetes file.");
  }

  @ParameterizedTest
  @EnumSource(value = ModuleFileEvent.Type.class, names = {"CREATED", "MODIFIED"})
  void shouldCallRemoveResourceAndAnalyseFilesWhenEvent(ModuleFileEvent.Type eventType) {
    sonarLintFileListener.initContext(context, null);
    var event = DefaultModuleFileEvent.of(inputFile2, eventType);

    sonarLintFileListener.process(event);

    var inputFileContext2 = new InputFileContext(context, inputFile2);
    var projectResources = sonarLintFileListener.getProjectContext().getProjectResources(
      "with-global-limit", inputFileContext2, LimitRange.class);
    assertThat(projectResources).isNotEmpty();
  }

  @Test
  void shouldThrowParseExceptionWhenIOException() {
    sonarLintFileListener.initContext(context, null);
    var event = DefaultModuleFileEvent.of(inputFileTOException, CREATED);

    var throwable = catchThrowable(() -> sonarLintFileListener.process(event));

    assertThat(throwable)
      .isInstanceOf(ParseException.class)
      .hasMessage("Cannot read 'memory_limit_pod.yaml'");
  }

  @Test
  void shouldNotCallAnalyseFilesWhenAlreadyInitialized() {
    sonarLintFileListener.initContext(context, null);
    sonarLintFileListener.initContext(context, null);
    assertThat(logTester.logs(Level.INFO))
      .containsOnly(
        "2 source files to be parsed",
        "2/2 source files have been parsed",
        "2 source files to be analyzed",
        "2/2 source files have been analyzed",
        "2 source files to be checked",
        "2/2 source files have been checked",
        "Finished building Kubernetes Project Context");
  }
}
