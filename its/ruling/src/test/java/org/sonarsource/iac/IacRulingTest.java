/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonarsource.iac;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.OrchestratorBuilder;
import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.locator.FileLocation;
import com.sonar.orchestrator.locator.MavenLocation;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sonarsource.analyzer.commons.ProfileGenerator;

import static org.assertj.core.api.Assertions.assertThat;

class IacRulingTest {

  private static final String SQ_VERSION_PROPERTY = "sonar.runtimeVersion";
  private static final String DEFAULT_SQ_VERSION = "LATEST_RELEASE[8.9]";
  private static final String LITS_VERSION = "0.9.0.1682";
  private static final String SONAR_CONFIG_VERSION = "DEV";

  private static Orchestrator orchestrator;
  private static boolean keepSonarqubeRunning = "true".equals(System.getProperty("keepSonarqubeRunning"));

  private static final Set<String> LANGUAGES = new HashSet<>();
  static {
    LANGUAGES.add("terraform");
    LANGUAGES.add("cloudformation");
  }

  @BeforeAll
  public static void setUp() {
    OrchestratorBuilder builder = Orchestrator.builderEnv()
      .setSonarVersion(System.getProperty(SQ_VERSION_PROPERTY, DEFAULT_SQ_VERSION))
      .addPlugin(FileLocation.byWildcardMavenFilename(new File("../../sonar-iac-plugin/target"), "sonar-iac-plugin-*.jar"))
      .addPlugin(MavenLocation.of("org.sonarsource.sonar-lits-plugin", "sonar-lits-plugin", LITS_VERSION))
      .addPlugin(MavenLocation.of("org.sonarsource.config", "sonar-config-plugin", SONAR_CONFIG_VERSION));

    orchestrator = builder.build();
    orchestrator.start();

    ProfileGenerator.RulesConfiguration terraformRulesConfiguration = new ProfileGenerator.RulesConfiguration();
    File terraformProfile = ProfileGenerator.generateProfile(orchestrator.getServer().getUrl(), "terraform", "terraform", terraformRulesConfiguration, Collections.emptySet());
    orchestrator.getServer().restoreProfile(FileLocation.of(terraformProfile));

    ProfileGenerator.RulesConfiguration cloudformationRulesConfiguration = new ProfileGenerator.RulesConfiguration();
    File cloudformationProfile = ProfileGenerator.generateProfile(orchestrator.getServer().getUrl(), "cloudformation", "cloudformation", cloudformationRulesConfiguration, Collections.emptySet());
    orchestrator.getServer().restoreProfile(FileLocation.of(cloudformationProfile));
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
      .setProjectKey(projectKey)
      .setProjectName(projectKey)
      .setProjectVersion("1")
      .setSourceDirs("./")
      .setSourceEncoding("utf-8")
      .setProperties(properties)
      .setProperty("dump.old", FileLocation.of("src/test/resources/expected/" + project).getFile().getAbsolutePath())
      .setProperty("dump.new", actualDirectory.getAbsolutePath())
      .setProperty("lits.differences", litsDifferencesFile.getAbsolutePath())
      .setProperty("sonar.scm.disabled", "true")
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
