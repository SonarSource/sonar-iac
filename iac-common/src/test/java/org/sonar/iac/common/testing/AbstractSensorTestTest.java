/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.common.testing;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.resources.Language;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AbstractSensorTestTest {

  private static final Sensor sensor = mock(Sensor.class);

  @TempDir
  protected File baseDir;

  @BeforeEach
  void setup() {
    SENSOR.context = SensorContextTester.create(baseDir);
    SENSOR.baseDir = baseDir;
  }

  private static final SensorTester SENSOR = new SensorTester();
  private static final Language LANGUAGE = new Language() {
    @Override
    public String getKey() {
      return "dummyLang";
    }

    @Override
    public String getName() {
      return "Dummy";
    }

    @Override
    public String[] getFileSuffixes() {
      return new String[] {".dummy"};
    }
  };

  @Test
  void checkFactory_should_contain_rules() {
    SENSOR.checkFactory("S1", "S2", "S3");
    Collection<ActiveRule> rules = SENSOR.context.activeRules().findAll();
    assertThat(rules)
      .hasSize(3)
      .allMatch(rule ->  rule.ruleKey().rule().matches("S[1-3]"))
      .allMatch(rule ->  rule.ruleKey().repository().equals("myRepo"));
  }

  @Test
  void checkFactory_can_be_empty() {
    SENSOR.checkFactory();
    Collection<ActiveRule> rules = SENSOR.context.activeRules().findAll();
    assertThat(rules).isEmpty();
  }

  @Test
  void createImputFile_returns_input_file_with_same_content() throws Exception {
    String content = "class A { }";
    String filename = "yolo.dummy";

    InputFile inputFile = SENSOR.inputFile(filename, content);

    assertThat(inputFile.contents()).isEqualTo(content);
    assertThat(inputFile.charset()).isEqualTo(StandardCharsets.UTF_8);
    assertThat(inputFile.language()).isEqualTo(LANGUAGE.getKey());
    assertThat(inputFile.type()).isEqualTo(InputFile.Type.MAIN);
    assertThat(inputFile.filename()).isEqualTo(filename);
  }

  @Test
  void analyse_execute_sensor() {
    SENSOR.analyse();
    verify(sensor).execute(SENSOR.context);
  }

  @Test
  void analyse_add_file_to_context() {
    String content = "class A { }";
    String filename = "yolo.dummy";

    InputFile inputFile = SENSOR.inputFile(filename, content);
    SENSOR.analyse(inputFile);
    assertThat(SENSOR.context.fileSystem().inputFiles()).contains(inputFile);
  }

  private static class SensorTester extends AbstractSensorTest {

    @Override
    protected Sensor sensor(CheckFactory checkFactory) {
      return sensor;
    }

    @Override
    protected String getActivationSettingKey() {
      return "";
    }

    @Override
    protected String repositoryKey() {
      return "myRepo";
    }

    @Override
    protected Language language() {
      return LANGUAGE;
    }

    @Override
    protected String fileLanguageKey() {
      return LANGUAGE.getKey();
    }
  }
}
