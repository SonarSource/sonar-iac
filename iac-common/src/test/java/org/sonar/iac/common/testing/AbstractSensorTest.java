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
package org.sonar.iac.common.testing;

import java.io.File;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.slf4j.event.Level;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.api.utils.Version;

public abstract class AbstractSensorTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  protected static final FileLinesContextFactory fileLinesContextFactory = Mockito.mock(FileLinesContextFactory.class);
  protected static final NoSonarFilter noSonarFilter = Mockito.mock(NoSonarFilter.class);

  protected static final Version VERSION_8_9 = Version.create(8, 9);
  protected static final SonarRuntime SONAR_RUNTIME_8_9 = SonarRuntimeImpl.forSonarQube(VERSION_8_9, SonarQubeSide.SCANNER, SonarEdition.DEVELOPER);

  @TempDir
  protected File baseDir;
  protected SensorContextTester context;
  protected SensorContextTester sonarLintContext;

  @BeforeEach
  void setup() {
    FileLinesContext fileLinesContext = Mockito.mock(FileLinesContext.class);
    Mockito.when(fileLinesContextFactory.createFor(ArgumentMatchers.any(InputFile.class))).thenReturn(fileLinesContext);
    MapSettings settings = new MapSettings();
    settings.setProperty(getActivationSettingKey(), true);
    context = SensorContextTester.create(baseDir).setSettings(settings);
    sonarLintContext = SensorContextTester.create(baseDir).setRuntime(SonarRuntimeImpl.forSonarLint(Version.create(3, 14))).setSettings(settings);
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
      .setLanguage(fileLanguageKey())
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

  protected abstract String fileLanguageKey();
}
