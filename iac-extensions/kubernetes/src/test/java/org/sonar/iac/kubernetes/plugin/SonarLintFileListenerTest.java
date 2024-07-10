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
package org.sonar.iac.kubernetes.plugin;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.kubernetes.visitors.ProjectContext;
import org.sonarsource.sonarlint.core.analysis.container.module.DefaultModuleFileEvent;
import org.sonarsource.sonarlint.plugin.api.module.file.ModuleFileEvent;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.sonar.iac.common.testing.IacTestUtils.inputFile;

class SonarLintFileListenerTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  private final static Path BASE_DIR = Path.of("src/test/resources/SonarLintFileListener");

  private SonarLintFileListener sonarLintFileListener;
  private SensorContext context;
  private KubernetesAnalyzer analyzer;
  private ProjectContext projectContext;
  private InputFile inputFile1;
  private InputFile inputFile2;
  private List<InputFile> inputFiles;

  @BeforeEach
  public void init() {
    inputFile1 = inputFile("limit_range.yaml", BASE_DIR, "yaml");
    inputFile2 = inputFile("memory_limit_pod.yaml", BASE_DIR, "yaml");
    inputFiles = List.of(inputFile1, inputFile2);
    var moduleFileSystem = new TestModuleFileSystem(inputFiles);
    sonarLintFileListener = new SonarLintFileListener(moduleFileSystem);
    context = SensorContextTester.create(BASE_DIR);
    projectContext = mock(ProjectContext.class);
    analyzer = mock(KubernetesAnalyzer.class);
  }

  @Test
  void shouldCallAnalyseFilesWhenInit() {
    sonarLintFileListener.initContext(context, analyzer, projectContext);

    verify(analyzer).analyseFiles(eq(context), eq(inputFiles), eq("kubernetes"));
  }

  @Test
  void shouldCallRemoveResourceWhenRemoveEvent() {
    sonarLintFileListener.initContext(context, analyzer, projectContext);
    var event = DefaultModuleFileEvent.of(inputFile1, ModuleFileEvent.Type.DELETED);

    sonarLintFileListener.process(event);

    verify(projectContext).removeResource(uri(inputFile1));
  }

  static List<ModuleFileEvent.Type> shouldCallRemoveResourceAndAnalyseFilesWhenEvent() {
    return List.of(ModuleFileEvent.Type.CREATED, ModuleFileEvent.Type.MODIFIED);
  }

  @ParameterizedTest
  @MethodSource
  void shouldCallRemoveResourceAndAnalyseFilesWhenEvent(ModuleFileEvent.Type eventType) {
    sonarLintFileListener.initContext(context, analyzer, projectContext);
    var event = DefaultModuleFileEvent.of(inputFile2, eventType);

    sonarLintFileListener.process(event);

    verify(projectContext).removeResource(uri(inputFile2));
  }

  private String uri(InputFile inputFile) {
    return Path.of(inputFile.uri()).normalize().toUri().toString();
  }
}
