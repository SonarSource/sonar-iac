/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonarsource.iac;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarScanner;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;
import org.junit.ClassRule;
import org.sonarqube.ws.Issues;
import org.sonarqube.ws.Measures.ComponentWsResponse;
import org.sonarqube.ws.Measures.Measure;
import org.sonarqube.ws.client.HttpConnector;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;
import org.sonarqube.ws.client.issues.SearchRequest;
import org.sonarqube.ws.client.measures.ComponentRequest;

import static java.util.Collections.singletonList;

public abstract class TestBase {

  @ClassRule
  public static final Orchestrator ORCHESTRATOR = Tests.ORCHESTRATOR;

  protected SonarScanner getSonarScanner(String projectKey, String directoryToScan, String languageKey) {
    return getSonarScanner(projectKey, directoryToScan, languageKey, null);
  }

  protected SonarScanner getSonarScanner(String projectKey, String directoryToScan, String languageKey, @Nullable String profileName) {
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

  protected Integer getMeasureAsInt(String componentKey, String metricKey) {
    Measure measure = getMeasure(componentKey, metricKey);
    return (measure == null) ? null : Integer.parseInt(measure.getValue());
  }

  protected static WsClient newWsClient() {
    return WsClientFactories.getDefault().newClient(HttpConnector.newBuilder()
      .url(ORCHESTRATOR.getServer().getUrl())
      .build());
  }
}
