/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.testing;

import java.io.File;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.sonar.iac.common.extension.IacSensor.EXTENDED_LOGGING_PROPERTY_NAME;
import static org.sonar.iac.common.predicates.CloudFormationFilePredicate.CLOUDFORMATION_FILE_IDENTIFIER_DEFAULT_VALUE;
import static org.sonar.iac.common.predicates.CloudFormationFilePredicate.CLOUDFORMATION_FILE_IDENTIFIER_KEY;
import static org.sonar.iac.common.testing.IacTestUtils.SONARLINT_RUNTIME_9_9;

public abstract class AbstractSensorTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  protected static final FileLinesContextFactory fileLinesContextFactory = Mockito.mock(FileLinesContextFactory.class);
  protected static final NoSonarFilter noSonarFilter = Mockito.mock(NoSonarFilter.class);
  private final String telemetryLoCKey = "iac.%s.loc".formatted(repositoryKey());

  @TempDir
  protected File baseDir;
  protected SensorContextTester context;
  protected MapSettings settings;
  protected SensorContextTester sonarLintContext;

  @BeforeEach
  void setup() {
    FileLinesContext fileLinesContext = Mockito.mock(FileLinesContext.class);
    Mockito.when(fileLinesContextFactory.createFor(ArgumentMatchers.any(InputFile.class))).thenReturn(fileLinesContext);
    settings = new MapSettings();
    settings.setProperty(getActivationSettingKey(), true);
    settings.setProperty(CLOUDFORMATION_FILE_IDENTIFIER_KEY, CLOUDFORMATION_FILE_IDENTIFIER_DEFAULT_VALUE);
    settings.setProperty(EXTENDED_LOGGING_PROPERTY_NAME, "true");
    context = spy(SensorContextTester.create(baseDir).setSettings(settings));
    context.setSettings(settings);
    sonarLintContext = spy(SensorContextTester.create(baseDir).setRuntime(SONARLINT_RUNTIME_9_9).setSettings(settings));
  }

  protected abstract String getActivationSettingKey();

  protected void analyze(InputFile... inputFiles) {
    analyze(context, inputFiles);
  }

  protected void analyze(SensorContextTester sensorContext, InputFile... inputFiles) {
    analyze(sensorContext, sensor(checkFactory(sensorContext)), inputFiles);
  }

  protected void analyze(Sensor sensor, InputFile... inputFiles) {
    analyze(context, sensor, inputFiles);
  }

  protected void analyze(SensorContextTester sensorContext, Sensor sensor, InputFile... inputFiles) {
    for (InputFile inputFile : inputFiles) {
      sensorContext.fileSystem().add(inputFile);
    }
    sensor.execute(sensorContext);
  }

  protected InputFile inputFile(String relativePath, String content) {
    return new TestInputFileBuilder("moduleKey", relativePath)
      .setModuleBaseDir(baseDir.toPath())
      .setType(InputFile.Type.MAIN)
      .setLanguage(fileLanguageKey())
      .setCharset(StandardCharsets.UTF_8)
      .setContents(content)
      .build();
  }

  protected CheckFactory checkFactory(String... ruleKeys) {
    return checkFactory(context, ruleKeys);
  }

  protected CheckFactory checkFactory(SensorContextTester sensorContext, String... ruleKeys) {
    var builder = new ActiveRulesBuilder();
    for (String ruleKey : ruleKeys) {
      NewActiveRule newRule = new NewActiveRule.Builder()
        .setRuleKey(RuleKey.of(repositoryKey(), ruleKey))
        .setName(ruleKey)
        .build();
      builder.addRule(newRule);
    }
    sensorContext.setActiveRules(builder.build());
    return new CheckFactory(sensorContext.activeRules());
  }

  protected String durationStatisticLog() {
    return logTester.logs(Level.INFO).stream()
      .filter(log -> log.startsWith("Duration Statistics, "))
      .findFirst()
      .orElseThrow(() -> new RuntimeException("Duration statistics should be enabled for sensor"));
  }

  protected void verifyLinesOfCodeTelemetry(int expectedLinesOfCode) {
    if (expectedLinesOfCode == 0) {
      verify(context, never()).addTelemetryProperty(eq(telemetryLoCKey), anyString());
    } else {
      verify(context).addTelemetryProperty(telemetryLoCKey, String.valueOf(expectedLinesOfCode));
    }
  }

  protected abstract Sensor sensor(CheckFactory checkFactory);

  protected abstract String repositoryKey();

  protected abstract String fileLanguageKey();
}
