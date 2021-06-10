/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
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
  }

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
