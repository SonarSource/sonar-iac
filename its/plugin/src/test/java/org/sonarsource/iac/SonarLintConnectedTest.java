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
package org.sonarsource.iac;

import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.junit5.OrchestratorExtension;
import com.sonar.orchestrator.locator.FileLocation;
import com.sonar.orchestrator.locator.MavenLocation;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonarsource.sonarlint.core.ConnectedSonarLintEngineImpl;
import org.sonarsource.sonarlint.core.StandaloneSonarLintEngineImpl;
import org.sonarsource.sonarlint.core.analysis.api.AnalysisResults;
import org.sonarsource.sonarlint.core.analysis.api.ClientInputFile;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;
import org.sonarsource.sonarlint.core.client.api.connected.ConnectedAnalysisConfiguration;
import org.sonarsource.sonarlint.core.client.api.connected.ConnectedGlobalConfiguration;
import org.sonarsource.sonarlint.core.client.api.connected.ConnectedSonarLintEngine;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneAnalysisConfiguration;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneGlobalConfiguration;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneSonarLintEngine;
import org.sonarsource.sonarlint.core.commons.Language;
import org.sonarsource.sonarlint.core.serverconnection.ProjectBinding;

import static org.assertj.core.api.Assertions.assertThat;

public class SonarLintConnectedTest {

  public static final Path BASE_DIR = Paths.get("projects/sonarlint");

  @TempDir
  static Path sonarLintUserHome;

  @TempDir
  static File tmp;

  private static ConnectedSonarLintEngine sonarlintEngineConnected;
  public static final Language[] ENABLED_LANGUAGES = {Language.TERRAFORM, Language.CLOUDFORMATION, Language.KUBERNETES, Language.DOCKER};

  private static final String SQ_VERSION_PROPERTY = "sonar.runtimeVersion";
  private static final String DEFAULT_SQ_VERSION = "LATEST_RELEASE";
  private static final String LITS_VERSION = "0.11.0.2659";
  private static final String SONAR_CONFIG_VERSION = "DEV";

  @RegisterExtension
  static OrchestratorExtension orchestrator = OrchestratorExtension.builderEnv()
    .useDefaultAdminCredentialsForBuilds(true)
    .setSonarVersion(System.getProperty(SQ_VERSION_PROPERTY, DEFAULT_SQ_VERSION))
    .addPlugin(FileLocation.byWildcardMavenFilename(new File("../../sonar-iac-plugin/target"), "sonar-iac-plugin-*.jar"))
    .addPlugin(MavenLocation.of("org.sonarsource.sonar-lits-plugin", "sonar-lits-plugin", LITS_VERSION))
    .addPlugin(MavenLocation.of("org.sonarsource.config", "sonar-config-plugin", SONAR_CONFIG_VERSION))
    .build();

  @BeforeAll
  public static void prepare() {
    ConnectedGlobalConfiguration configConnected = ConnectedGlobalConfiguration.sonarQubeBuilder()
      .setConnectionId(orchestrator.getServer().getUrl())
//      .setConnectionId("orchestrator")
//      .setConnectionId("random connection id")
      .enableHotspots()
      .useEmbeddedPlugin(Language.DOCKER.getPluginKey(), TestsSetup.IAC_PLUGIN_LOCATION.getFile().toPath())
      //.useEmbeddedPlugin("", TestsSetup.IAC_PLUGIN_LOCATION.getFile().toPath())
      .addEnabledLanguages(ENABLED_LANGUAGES)
      .setSonarLintUserHome(sonarLintUserHome)
      .setLogOutput((formattedMessage, level) -> {
        /* Don't pollute logs */ })
      .build();
    sonarlintEngineConnected = new ConnectedSonarLintEngineImpl(configConnected);
  }

  @ParameterizedTest
  @MethodSource
  void shouldRaiseIssuesAndHotspots(Path inputFile) {
    // trigger analysis on sonarqube
    Map<String, String> properties = new HashMap<>();
    properties.put("sonar.inclusions", "/**/*, **");
    properties.put("sonar.iac.duration.statistics", "true");
    String projectKey = inputFile.getFileName().toString() + "-project";
    orchestrator.getServer().provisionProject(projectKey, projectKey);

    SonarScanner build = SonarScanner.create(FileLocation.of(inputFile.toFile()).getFile())
      .setProjectKey(projectKey)
      .setProjectName(projectKey)
      .setProjectVersion("1")
      .setSourceDirs("./")
      .setSourceEncoding("utf-8")
      .setProperties(properties)
      .setProperty("sonar.scm.disabled", "true")
      .setProperty("sonar.internal.analysis.failFast", "true")
      .setProperty("sonar.project", projectKey)
      .setEnvironmentVariable("SONAR_RUNNER_OPTS", "-Xmx1024m");

    orchestrator.executeBuild(build);

    ClientInputFile clientInputFile = createInputFile(inputFile, false);
    final List<Issue> issues = new ArrayList<>();
    ConnectedAnalysisConfiguration connectedAnalysisConfiguration = ConnectedAnalysisConfiguration.builder()
      .setBaseDir(tmp.toPath())
      .addInputFile(clientInputFile)
      .setProjectKey(projectKey)
      .build();

    //sonarlintEngineConnected.analyze(connectedAnalysisConfiguration, issues::add, null, null);
    ProjectBinding projectBinding = sonarlintEngineConnected.calculatePathPrefixes(projectKey, List.of(inputFile.toString()));
    var iss = sonarlintEngineConnected.getServerIssues(projectBinding, "", "");
    assertThat(issues).hasSize(1);
  }

  static List<Path> shouldRaiseIssuesAndHotspots() {
    return provideTestDirs("issue");
  }

  private static List<Path> provideTestDirs(String testFileDir) {
    List<Path> testDirs = new ArrayList<>();

    for (Language language : ENABLED_LANGUAGES) {
      testDirs.add(BASE_DIR.resolve(language.getLanguageKey() + "/" + testFileDir));
    }

    return testDirs;
  }

  private ClientInputFile createInputFile(final Path path, final boolean isTest) {
    return new ClientInputFile() {

      @Override
      public String getPath() {
        return path.toString();
      }

      @Override
      public String relativePath() {
        return path.toString();
      }

      @Override
      public URI uri() {
        return path.toUri();
      }

      @Override
      public boolean isTest() {
        return isTest;
      }

      @Override
      public Charset getCharset() {
        return StandardCharsets.UTF_8;
      }

      @Override
      public <G> G getClientObject() {
        return null;
      }

      @Override
      public InputStream inputStream() throws IOException {
        return new FileInputStream(path.toFile());
      }

      @Override
      public String contents() throws IOException {
        return FileUtils.readFileToString(path.toFile(), getCharset());
      }
    };
  }
}
