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
package org.sonarsource.iac;

import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.junit5.OrchestratorExtension;
import com.sonar.orchestrator.locator.FileLocation;
import com.sonar.orchestrator.locator.MavenLocation;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
  private static final File LITS_OUTPUT_DIRECTORY = FileLocation.of("build/reports/lits").getFile();
  private static final File LITS_DIFFERENCES_FILE = Path.of(LITS_OUTPUT_DIRECTORY.toURI()).resolve("differences").toFile();
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
  public static void setUp() throws IOException {
    LANGUAGES.forEach(language -> {
      ProfileGenerator.RulesConfiguration languageRulesConfiguration = new ProfileGenerator.RulesConfiguration();
      File languageProfile = ProfileGenerator.generateProfile(orchestrator.getServer().getUrl(), language, language, languageRulesConfiguration, Collections.emptySet());
      orchestrator.getServer().restoreProfile(FileLocation.of(languageProfile));
    });
    Files.createDirectories(Path.of(LITS_DIFFERENCES_FILE.getParentFile().toURI()));
  }

  @Test
  void testTerraform() throws IOException {
    Map<String, String> properties = new HashMap<>();
    properties.put("sonar.inclusions", "sources/terraform/**/*.tf, ruling/src/integrationTest/resources/sources/terraform/**/*.tf");
    runRulingTest("terraform", properties);
  }

  @Test
  void testCloudformation() throws IOException {
    Map<String, String> properties = new HashMap<>();
    properties.put("sonar.inclusions", "sources/cloudformation/**/*.json, ruling/src/integrationTest/resources/sources/cloudformation/**/*.json," +
      "sources/cloudformation/**/*.yaml, ruling/src/integrationTest/resources/sources/cloudformation/**/*.yaml," +
      "sources/cloudformation/**/*.yml, ruling/src/integrationTest/resources/sources/cloudformation/**/*.yml,");
    properties.put("sonar.cloudformation.file.identifier", "");
    runRulingTest("cloudformation", properties);
  }

  @Test
  void testKubernetes() throws IOException {
    Map<String, String> properties = new HashMap<>();
    properties.put("sonar.inclusions",
      // when required, scope can be increased to include more files (e.g. resources)
      "sources/kubernetes/**/*.yaml," +
        "sources/kubernetes/**/*.yml," +
        "sources/kubernetes/**/*.tpl," +
        "sources/kubernetes/**/*.toml," +
        "sources/kubernetes/**/*.txt," +
        "sources/kubernetes/**/*.properties," +
        "ruling/src/integrationTest/resources/sources/kubernetes/**");
    runRulingTest("kubernetes", properties);
  }

  @Test
  void testDocker() throws IOException {
    Map<String, String> properties = new HashMap<>();
    properties.put("sonar.inclusions", "sources/docker/**/Dockerfile*, ruling/src/integrationTest/resources/sources/docker/**/**");
    runRulingTest("docker", properties);
  }

  @Test
  void testArm() throws IOException {
    Map<String, String> properties = new HashMap<>();
    properties.put("sonar.inclusions", "sources/azureresourcemanager/**/*.json, ruling/src/integrationTest/resources/sources/azureresourcemanager/**/*.json," +
      "sources/azureresourcemanager/**/*.bicep, ruling/src/integrationTest/resources/sources/azureresourcemanager/**/*.bicep");
    runRulingTest("azureresourcemanager", properties);
  }

  @Disabled("This test is only a helper to diagnose failures on the local system")
  @Test
  void testLocal() throws IOException {
    Map<String, String> properties = new HashMap<>();
    properties.put("sonar.sources", "sources/tmp");
    runRulingTest("tmp", properties);
  }

  private static void runRulingTest(String project, Map<String, String> projectProperties) throws IOException {
    Map<String, String> properties = new HashMap<>(projectProperties);
    properties.put("sonar.iac.duration.statistics", "true");

    String projectKey = project.replace("/", "-") + "-project";
    orchestrator.getServer().provisionProject(projectKey, projectKey);
    LANGUAGES.forEach(lang -> orchestrator.getServer().associateProjectToQualityProfile(projectKey, lang, "rules"));

    SonarScanner build = SonarScanner.create(FileLocation.of("../").getFile())
      .setScannerVersion(SCANNER_VERSION)
      .setProjectKey(projectKey)
      .setProjectName(projectKey)
      .setProjectVersion("1")
      .setSourceDirs("./")
      .setSourceEncoding("utf-8")
      .setProperties(properties)
      .setProperty("sonar.lits.dump.old", FileLocation.of("src/integrationTest/resources/expected/" + project).getFile().getAbsolutePath())
      .setProperty("sonar.lits.dump.new", FileLocation.of(LITS_OUTPUT_DIRECTORY + "/actual").getFile().getAbsolutePath())
      .setProperty("sonar.lits.differences", LITS_DIFFERENCES_FILE.getAbsolutePath())
      .setProperty("sonar.scm.disabled", "true")
      .setProperty("sonar.internal.analysis.failFast", "true")
      .setProperty("sonar.project", project)
      .setEnvironmentVariable("SONAR_RUNNER_OPTS", "-Xmx1024m");

    orchestrator.executeBuild(build);

    var litsDifference = new String(Files.readAllBytes(LITS_DIFFERENCES_FILE.toPath()), StandardCharsets.UTF_8);
    assertThat(litsDifference).isEmpty();
  }

  @AfterAll
  public static void after() throws InterruptedException {
    if (keepSonarqubeRunning) {
      // keep server running, use CTRL-C to stop it
      Thread.sleep(Long.MAX_VALUE);
    }
  }
}
