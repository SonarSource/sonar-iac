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
package org.sonar.iac.common.yaml;

import java.io.File;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.Configuration;
import org.sonar.api.resources.Language;
import org.sonar.iac.common.extension.analyzer.SingleFileAnalyzer;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.MetricsVisitor;
import org.sonar.iac.common.extension.visitors.SyntaxHighlightingVisitor;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.testing.AbstractSensorTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class YamlSensorTest extends AbstractSensorTest {

  @TempDir
  private File sensorDir;

  @Test
  void shouldVerifyDescribe() {
    DefaultSensorDescriptor sensorDescriptor = new DefaultSensorDescriptor();
    sensor().describe(sensorDescriptor);
    assertThat(sensorDescriptor.languages()).hasSize(2);
    assertThat(sensorDescriptor.languages()).containsExactly("json", "yaml");
    assertThat(sensorDescriptor.name()).isEqualTo("IaC Yaml Sensor");
  }

  @Test
  void shouldCreateAnalyzer() {
    SensorContextTester sensorContextTester = SensorContextTester.create(sensorDir);
    DurationStatistics durationStatistics = new DurationStatistics(mock(Configuration.class));
    assertThat(sensor().createAnalyzer(sensorContextTester, durationStatistics)).isInstanceOf(SingleFileAnalyzer.class);
  }

  @Test
  void shouldCreateVisitors() {
    assertThat(sensor().visitors(context, mock(DurationStatistics.class))).hasSize(3);
  }

  @Test
  void shouldNotReturnHighlightingAndMetricsVisitorsInSonarLintContext() {
    List<TreeVisitor<InputFileContext>> visitors = sensor().visitors(sonarLintContext, mock(DurationStatistics.class));
    assertThat(visitors).doesNotHaveAnyElementsOfTypes(SyntaxHighlightingVisitor.class, MetricsVisitor.class);
  }

  @Test
  void shouldVerifyMainFilePredicate() {
    FilePredicate predicate = sensor().mainFilePredicate(context);

    InputFile mainYamlFile = file().setType(InputFile.Type.MAIN).setLanguage("yaml").build();
    assertThat(predicate.apply(mainYamlFile)).isTrue();

    InputFile mainJsonFile = file().setType(InputFile.Type.MAIN).setLanguage("json").build();
    assertThat(predicate.apply(mainJsonFile)).isTrue();

    InputFile testYamlFile = file().setType(InputFile.Type.TEST).setLanguage("yaml").build();
    assertThat(predicate.apply(testYamlFile)).isFalse();

    InputFile mainPhpFile = file().setType(InputFile.Type.TEST).setLanguage("php").build();
    assertThat(predicate.apply(mainPhpFile)).isFalse();
  }

  private TestInputFileBuilder file() {
    return new TestInputFileBuilder("moduleKey", baseDir.getName());
  }

  @Override
  protected String getActivationSettingKey() {
    return "yaml.activation";
  }

  private YamlSensor sensor() {
    return sensor(checkFactory());
  }

  @Override
  protected YamlSensor sensor(CheckFactory checkFactory) {
    return new YamlSensor(SONAR_RUNTIME_8_9, fileLinesContextFactory, checkFactory, noSonarFilter, YamlLanguage.YAML, Collections.emptyList()) {
      @Override
      protected FilePredicate customFilePredicate(SensorContext sensorContext) {
        FilePredicate customPredicate = mock(FilePredicate.class);
        when(customPredicate.apply(any())).thenReturn(true);
        return customPredicate;
      }

      @Override
      protected String repositoryKey() {
        return null;
      }

      @Override
      protected String getActivationSettingKey() {
        return null;
      }
    };
  }

  @Override
  protected String repositoryKey() {
    return "yaml";
  }

  @Override
  protected String fileLanguageKey() {
    return "yaml";
  }

  enum YamlLanguage implements Language {
    YAML;

    @Override
    public String getKey() {
      return "yaml";
    }

    @Override
    public String getName() {
      return "Yaml";
    }

    @Override
    public String[] getFileSuffixes() {
      return new String[] {};
    }
  }
}
