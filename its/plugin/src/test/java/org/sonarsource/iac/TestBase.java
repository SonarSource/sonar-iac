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

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.BuildResult;
import com.sonar.orchestrator.build.SonarScanner;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.extension.RegisterExtension;
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

  @RegisterExtension
  public static final Orchestrator ORCHESTRATOR = Tests.ORCHESTRATOR;

  protected SonarScanner getSonarScanner(String projectKey, String directoryToScan, String languageKey) {
    return getSonarScanner(projectKey, directoryToScan, languageKey, null);
  }

  protected static SonarScanner getSonarScanner(String projectKey, String directoryToScan, String languageKey, @Nullable String profileName) {
    ORCHESTRATOR.getServer().provisionProject(projectKey, projectKey);
    if (profileName != null) {
      ORCHESTRATOR.getServer().associateProjectToQualityProfile(projectKey, languageKey, profileName);
    }
    return SonarScanner.create()
      .setProjectDir(new File(directoryToScan, languageKey))
      .setProjectKey(projectKey)
      .setProjectName(projectKey)
      .setProjectVersion("1")
      .setSourceDirs(".");
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
    return measures.size() == 1 ? measures.get(0) : null;
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

  protected List<Hotspots.SearchWsResponse.Hotspot> getHotspotsForProject(String projectkey) {
    return newWsClient().hotspots().search(new org.sonarqube.ws.client.hotspots.SearchRequest()
      .setProjectKey(projectkey)).getHotspotsList();
  }

  protected Integer getMeasureAsInt(String componentKey, String metricKey) {
    Measure measure = getMeasure(componentKey, metricKey);
    return (measure == null) ? null : Integer.parseInt(measure.getValue());
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
    List<String> lines = Arrays.asList(logs.split("[\r\n]+"));

    assertThat(lines.size()).isBetween(25, 150);

    List<String> unexpectedLogs = lines.stream()
      .filter(line -> !line.startsWith("INFO: "))
      .filter(line -> !line.startsWith("WARN: SonarQube scanners will require Java 11+ starting on next version"))
      .filter(line -> !line.startsWith("WARN: The sonar.modules is a deprecated property and should not be used anymore"))
      .filter(line -> !line.startsWith("WARNING: An illegal reflective access operation has occurred"))
      .filter(line -> !line.startsWith("WARNING: Illegal reflective access"))
      .filter(line -> !line.startsWith("WARNING: Please consider reporting this to the maintainers"))
      .filter(line -> !line.startsWith("WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations"))
      .filter(line -> !line.startsWith("WARNING: All illegal access operations will be denied in a future release"))
      .filter(line -> !line
        .startsWith("WARN: The property 'sonar.login' is deprecated and will be removed in the future. Please use the 'sonar.token' property instead when passing a token."))
      .filter(line -> !line.startsWith("Picked up JAVA_TOOL_OPTIONS:"))
      .collect(Collectors.toList());

    assertThat(unexpectedLogs).isEmpty();
  }
}
