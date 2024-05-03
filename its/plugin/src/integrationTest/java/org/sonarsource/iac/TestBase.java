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

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.BuildResult;
import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.junit5.OrchestratorExtension;
import com.sonar.orchestrator.locator.FileLocation;
import com.sonar.orchestrator.locator.MavenLocation;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.sonarqube.ws.Hotspots;
import org.sonarqube.ws.Issues;
import org.sonarqube.ws.Measures.ComponentWsResponse;
import org.sonarqube.ws.Measures.Measure;
import org.sonarqube.ws.client.HttpConnector;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;
import org.sonarqube.ws.client.issues.SearchRequest;
import org.sonarqube.ws.client.measures.ComponentRequest;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class TestBase {

  static final String SQ_VERSION_PROPERTY = "sonar.runtimeVersion";
  static final String DEFAULT_SQ_VERSION = "LATEST_RELEASE";
  static final String KEEP_ORCHESTRATOR_RUNNING_ENV = "KEEP_ORCHESTRATOR_RUNNING";

  static final AtomicInteger REQUESTED_ORCHESTRATORS_KEY = new AtomicInteger();
  public static final FileLocation IAC_PLUGIN_LOCATION = FileLocation.byWildcardFilename(new File("../../sonar-iac-plugin/build/libs"), "sonar-iac-plugin-*-all.jar");
  public static boolean KEEP_ORCHESTRATOR_RUNNING = "true".equals(System.getenv(KEEP_ORCHESTRATOR_RUNNING_ENV));
  private static final String JAVA_VERSION = "7.34.0.35958";

  public static Orchestrator ORCHESTRATOR = OrchestratorExtension.builderEnv()
    .useDefaultAdminCredentialsForBuilds(true)
    .setSonarVersion(System.getProperty(SQ_VERSION_PROPERTY, DEFAULT_SQ_VERSION))
    .addPlugin(IAC_PLUGIN_LOCATION)
    .addPlugin(MavenLocation.of("org.sonarsource.java", "sonar-java-plugin", JAVA_VERSION))
    .restoreProfileAtStartup(FileLocation.of("src/integrationTest/resources/nosonar-terraform.xml"))
    .restoreProfileAtStartup(FileLocation.of("src/integrationTest/resources/aws-provider-terraform.xml"))
    .restoreProfileAtStartup(FileLocation.of("src/integrationTest/resources/no_rules-docker.xml"))
    .restoreProfileAtStartup(FileLocation.of("src/integrationTest/resources/no_rules-json.xml"))
    .restoreProfileAtStartup(FileLocation.of("src/integrationTest/resources/no_rules-yaml.xml"))
    .restoreProfileAtStartup(FileLocation.of("src/integrationTest/resources/no_rules-cloudformation.xml"))
    .restoreProfileAtStartup(FileLocation.of("src/integrationTest/resources/java-springconfig.xml"))
    .build();

  @BeforeAll
  public static void startOrchestrator() {
    // This is to avoid multiple starts when using nested tests
    // See https://github.com/junit-team/junit5/issues/2421
    if (REQUESTED_ORCHESTRATORS_KEY.getAndIncrement() == 0) {
      ORCHESTRATOR.start();
    }
  }

  @AfterAll
  public static void stopOrchestrator() {
    if (!KEEP_ORCHESTRATOR_RUNNING && REQUESTED_ORCHESTRATORS_KEY.decrementAndGet() == 0) {
      ORCHESTRATOR.stop();
    }
  }

  private static final String SCANNER_VERSION = "5.0.1.3006";

  protected SonarScanner getSonarScanner(String projectKey, String directoryToScan, String languageKey) {
    return getSonarScanner(projectKey, directoryToScan, languageKey, null);
  }

  protected static SonarScanner getSonarScanner(String projectKey, String directoryToScan, String languageKey, @Nullable String profileName) {
    ORCHESTRATOR.getServer().provisionProject(projectKey, projectKey);
    if (profileName != null) {
      ORCHESTRATOR.getServer().associateProjectToQualityProfile(projectKey, languageKey, profileName);
    }
    return SonarScanner.create()
      .setScannerVersion(SCANNER_VERSION)
      .setProjectDir(new File(directoryToScan, languageKey))
      .setProjectKey(projectKey)
      .setProjectName(projectKey)
      .setProjectVersion("1")
      .setSourceDirs(".")
      .setEnvironmentVariable("SONAR_SCANNER_OPTS", "-Xmx4G");
  }

  protected Measure getMeasure(String projectKey, String metricKey) {
    return getMeasure(projectKey, null, metricKey);
  }

  protected Measure getMeasure(String projectKey, @Nullable String componentKey, String metricKey) {
    String component;
    if (componentKey != null) {
      component = projectKey + ":" + componentKey;
    } else {
      component = projectKey;
    }
    ComponentWsResponse response = newWsClient().measures().component(new ComponentRequest()
      .setComponent(component)
      .setMetricKeys(singletonList(metricKey)));
    List<Measure> measures = response.getComponent().getMeasuresList();
    return Optional.of(measures).filter(m -> m.size() == 1).map(m -> m.get(0)).orElse(null);
  }

  protected Map<String, Measure> getMeasures(String projectKey, String... metricKeys) {
    return newWsClient().measures().component(new ComponentRequest()
      .setComponent(projectKey)
      .setMetricKeys(Arrays.asList(metricKeys)))
      .getComponent().getMeasuresList()
      .stream()
      .collect(Collectors.toMap(Measure::getMetric, Function.identity()));
  }

  protected List<Issues.Issue> getIssuesForRule(String componentKey, String rule) {
    return newWsClient().issues().search(new SearchRequest()
      .setRules(Collections.singletonList(rule))
      .setComponentKeys(Collections.singletonList(componentKey))).getIssuesList();
  }

  static List<Issues.Issue> issuesForComponent(String componentKey) {
    return newWsClient()
      .issues()
      .search(new SearchRequest().setComponentKeys(Collections.singletonList(componentKey)))
      .getIssuesList();
  }

  protected List<Hotspots.SearchWsResponse.Hotspot> getHotspotsForProject(String projectKey) {
    return newWsClient().hotspots().search(new org.sonarqube.ws.client.hotspots.SearchRequest()
      .setProjectKey(projectKey)).getHotspotsList();
  }

  protected Integer getMeasureAsInt(String componentKey, String metricKey) {
    var measure = getMeasure(componentKey, metricKey);
    return Optional.ofNullable(measure).map(m -> Integer.parseInt(measure.getValue())).orElse(null);
  }

  protected static WsClient newWsClient() {
    return WsClientFactories.getDefault().newClient(HttpConnector.newBuilder()
      .url(ORCHESTRATOR.getServer().getUrl())
      .build());
  }

  public static void executeBuildWithExpectedWarnings(Orchestrator orchestrator, SonarScanner build) {
    BuildResult result = orchestrator.executeBuild(build);
    assertAnalyzerLogs(result.getLogs());
  }

  private static void assertAnalyzerLogs(String logs) {
    List<String> lines = new ArrayList<>(Arrays.asList(logs.split("[\r\n]+")));

    assertThat(lines).hasSizeBetween(25, 190);

    Set<String> allowedStrings = Set.of(
      "INFO: ",
      "WARN: SonarQube scanners will require Java 11+ starting on next version",
      "WARN: The sonar.modules is a deprecated property and should not be used anymore",
      "WARN: sonar.plugins.downloadOnlyRequired is false",
      "WARNING: An illegal reflective access operation has occurred",
      "WARNING: Illegal reflective access",
      "WARNING: Please consider reporting this to the maintainers",
      "WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations",
      "WARNING: All illegal access operations will be denied in a future release",
      "WARN: The property 'sonar.login' is deprecated and will be removed in the future. Please use the 'sonar.token' property instead when passing a token.",
      "Picked up JAVA_TOOL_OPTIONS:");

    lines.removeIf(logElement -> allowedStrings.stream().anyMatch(logElement::startsWith));

    Set<String> temporaryToleratedStrings = Set.of(
      "org.eclipse.jgit.internal.util.ShutdownHook.cleanup",
      "at java.base/java.lang.Thread.run",
      "java.lang.NoClassDefFoundError: ch/qos/logback/classic/spi/ThrowableProxy",
      "at ch.qos.logback.classic.spi.LoggingEvent.<init>",
      "at ch.qos.logback.classic.Logger.buildLoggingEventAndAppend",
      "at ch.qos.logback.classic.Logger.filterAndLog_0_Or3Plus",
      "at ch.qos.logback.classic.Logger.error",
      "Caused by: java.lang.ClassNotFoundException: ch.qos.logback.classic.spi.ThrowableProxy",
      "at java.base/java.net.URLClassLoader.findClass",
      "at org.sonarsource.scanner.api.internal.IsolatedClassloader.loadClass",
      "at java.base/java.lang.ClassLoader.loadClass",
      "... 6 more");

    lines.removeIf(logElement -> temporaryToleratedStrings.stream().anyMatch(logElement::contains));

    assertThat(lines).isEmpty();
  }
}
