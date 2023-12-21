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
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonarsource.analyzer.commons.ProfileGenerator;

import static org.assertj.core.api.Assertions.assertThat;

class IacRulingTest {

  private static final String SQ_VERSION_PROPERTY = "sonar.runtimeVersion";
  private static final String DEFAULT_SQ_VERSION = "LATEST_RELEASE";
  private static final String LITS_VERSION = "0.11.0.2659";
  private static final String SCANNER_VERSION = "5.0.1.3006";

  @RegisterExtension
  static OrchestratorExtension orchestrator = OrchestratorExtension.builderEnv()
    .useDefaultAdminCredentialsForBuilds(true)
    .setSonarVersion(System.getProperty(SQ_VERSION_PROPERTY, DEFAULT_SQ_VERSION))
    .addPlugin(FileLocation.byWildcardFilename(new File("../../sonar-iac-plugin/build/libs"), "sonar-iac-plugin-*-all.jar"))
    .addPlugin(MavenLocation.of("org.sonarsource.sonar-lits-plugin", "sonar-lits-plugin", LITS_VERSION))
    .build();

  private static final boolean keepSonarqubeRunning = "true".equals(System.getProperty("keepSonarqubeRunning"));

  private static final Set<String> LANGUAGES = Set.of(
    "terraform",
    "cloudformation",
    "kubernetes",
    "docker",
    "azureresourcemanager");

  @BeforeAll
  public static void setUp() {
    LANGUAGES.forEach(language -> {
      ProfileGenerator.RulesConfiguration languageRulesConfiguration = new ProfileGenerator.RulesConfiguration();
      File languageProfile = ProfileGenerator.generateProfile(orchestrator.getServer().getUrl(), language, language, languageRulesConfiguration, Collections.emptySet());
      orchestrator.getServer().restoreProfile(FileLocation.of(languageProfile));
    });
  }

  @Test
  void test_terraform() throws IOException {
    Map<String, String> properties = new HashMap<>();
    properties.put("sonar.inclusions", "sources/terraform/**/*.tf, ruling/src/test/resources/sources/terraform/**/*.tf");
    run_ruling_test("terraform", properties);
  }

  @Test
  void test_cloudformation() throws IOException {
    Map<String, String> properties = new HashMap<>();
    properties.put("sonar.inclusions", "sources/cloudformation/**/*.json, ruling/src/test/resources/sources/cloudformation/**/*.json," +
      "sources/cloudformation/**/*.yaml, ruling/src/test/resources/sources/cloudformation/**/*.yaml," +
      "sources/cloudformation/**/*.yml, ruling/src/test/resources/sources/cloudformation/**/*.yml,");
    properties.put("sonar.cloudformation.file.identifier", "");
    run_ruling_test("cloudformation", properties);
  }

  @Test
  void test_kubernetes() throws IOException {
    Map<String, String> properties = new HashMap<>();
    properties.put("sonar.inclusions", "sources/kubernetes/**/*.yaml, ruling/src/test/resources/sources/kubernetes/**/*.yaml," +
      "sources/kubernetes/**/*.yml, ruling/src/test/resources/sources/kubernetes/**/*.yml");
    run_ruling_test("kubernetes", properties);
  }

  @Test
  void test_docker() throws IOException {
    Map<String, String> properties = new HashMap<>();
    properties.put("sonar.inclusions", "sources/docker/**/Dockerfile*, ruling/src/test/resources/sources/docker/**/**");
    run_ruling_test("docker", properties);
  }

  @Test
  void test_arm() throws IOException {
    Map<String, String> properties = new HashMap<>();
    properties.put("sonar.inclusions", "sources/azureresourcemanager/**/*.json, ruling/src/test/resources/sources/azureresourcemanager/**/*.json," +
      "sources/azureresourcemanager/**/*.bicep, ruling/src/test/resources/sources/azureresourcemanager/**/*.bicep");
    run_ruling_test("azureresourcemanager", properties);
  }

  @Disabled("This test is only a helper to diagnose failures on the local system")
  @Test
  void test_local() throws IOException {
    Map<String, String> properties = new HashMap<>();
    properties.put("sonar.sources", "sources/tmp");
    run_ruling_test("tmp", properties);
  }

  private void run_ruling_test(String project, Map<String, String> projectProperties) throws IOException {
    Map<String, String> properties = new HashMap<>(projectProperties);
    properties.put("sonar.iac.duration.statistics", "true");

    String projectKey = project.replace("/", "-") + "-project";
    orchestrator.getServer().provisionProject(projectKey, projectKey);
    LANGUAGES.forEach(lang -> orchestrator.getServer().associateProjectToQualityProfile(projectKey, lang, "rules"));

    File actualDirectory = FileLocation.of("target/actual/" + project).getFile();
    actualDirectory.mkdirs();

    File litsDifferencesFile = FileLocation.of("target/" + projectKey + "-differences").getFile();
    SonarScanner build = SonarScanner.create(FileLocation.of("../").getFile())
      .setScannerVersion(SCANNER_VERSION)
      .setProjectKey(projectKey)
      .setProjectName(projectKey)
      .setProjectVersion("1")
      .setSourceDirs("./")
      .setSourceEncoding("utf-8")
      .setProperties(properties)
      .setProperty("sonar.lits.dump.old", FileLocation.of("src/test/resources/expected/" + project).getFile().getAbsolutePath())
      .setProperty("sonar.lits.dump.new", actualDirectory.getAbsolutePath())
      .setProperty("sonar.lits.differences", litsDifferencesFile.getAbsolutePath())
      .setProperty("sonar.scm.disabled", "true")
      .setProperty("sonar.internal.analysis.failFast", "true")
      .setProperty("sonar.project", project)
      .setEnvironmentVariable("SONAR_RUNNER_OPTS", "-Xmx1024m");

    orchestrator.executeBuild(build);

    String litsDifference = new String(Files.readAllBytes(litsDifferencesFile.toPath()));
    assertThat(litsDifference).isEmpty();
  }

  @AfterAll
  public static void after() {
    if (keepSonarqubeRunning) {
      // keep server running, use CTRL-C to stop it
      new Scanner(System.in).next();
    }
  }
}