/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.common.testing;

import java.io.File;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
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
import org.sonar.api.resources.Language;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.LogTesterJUnit5;

public abstract class AbstractSensorTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();

  protected static final FileLinesContextFactory fileLinesContextFactory = Mockito.mock(FileLinesContextFactory.class);
  protected static final NoSonarFilter noSonarFilter = Mockito.mock(NoSonarFilter.class);

  @TempDir
  protected File baseDir;
  protected SensorContextTester context;

  @BeforeEach
  void setup() {
    FileLinesContext fileLinesContext = Mockito.mock(FileLinesContext.class);
    Mockito.when(fileLinesContextFactory.createFor(ArgumentMatchers.any(InputFile.class))).thenReturn(fileLinesContext);
    context = SensorContextTester.create(baseDir);
    MapSettings settings = new MapSettings();
    settings.setProperty(getActivationSettingKey(), true);
    context.setSettings(settings);
  }

  protected abstract String getActivationSettingKey();

  protected void analyse(InputFile... inputFiles) {
    analyse(sensor(checkFactory()), inputFiles);
  }

  protected void analyse(Sensor sensor, InputFile... inputFiles) {
    for (InputFile inputFile : inputFiles) {
      context.fileSystem().add(inputFile);
    }
    sensor.execute(context);
  }

  protected InputFile inputFile(String relativePath, String content) {
    return new TestInputFileBuilder("moduleKey", relativePath)
      .setModuleBaseDir(baseDir.toPath())
      .setType(InputFile.Type.MAIN)
      .setLanguage(language().getKey())
      .setCharset(StandardCharsets.UTF_8)
      .setContents(content)
      .build();
  }

  protected CheckFactory checkFactory(String... ruleKeys) {
    ActiveRulesBuilder builder = new ActiveRulesBuilder();
    for (String ruleKey : ruleKeys) {
      NewActiveRule newRule = new NewActiveRule.Builder()
        .setRuleKey(RuleKey.of(repositoryKey(), ruleKey))
        .setName(ruleKey)
        .build();
      builder.addRule(newRule);
    }
    context.setActiveRules(builder.build());
    return new CheckFactory(context.activeRules());
  }

  protected abstract Sensor sensor(CheckFactory checkFactory);

  protected abstract String repositoryKey();

  protected abstract Language language();
}
