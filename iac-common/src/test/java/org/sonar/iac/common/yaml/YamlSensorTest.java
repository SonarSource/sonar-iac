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
package org.sonar.iac.common.yaml;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.exceptions.MarkedYamlEngineException;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultTextPointer;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.resources.Language;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.ParseException;
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

  @Test
  void describe() {
    DefaultSensorDescriptor sensorDescriptor = new DefaultSensorDescriptor();
    sensor().describe(sensorDescriptor);
    assertThat(sensorDescriptor.languages()).hasSize(2);
    assertThat(sensorDescriptor.languages()).containsExactly("json", "yaml");
    assertThat(sensorDescriptor.name()).isEqualTo("IaC Yaml Sensor");
  }


  @Test
  void treeParser() {
    assertThat(sensor().treeParser()).isInstanceOf(YamlParser.class);
  }

  @Test
  void visitors() {
    assertThat(sensor().visitors(context, mock(DurationStatistics.class))).hasSize(3);
  }

  @Test
  void shouldNotReturnHighlightingAndMetricsVisitorsInSonarLintContext() {
    List<TreeVisitor<InputFileContext>> visitors = sensor().visitors(sonarLintContext, mock(DurationStatistics.class));
    assertThat(visitors).doesNotHaveAnyElementsOfTypes(SyntaxHighlightingVisitor.class, MetricsVisitor.class);
  }

  @Test
  void toParseException_with_MarkedYamlEngineException() {
    MarkedYamlEngineException yamlEngineException = mock(MarkedYamlEngineException.class);
    when(yamlEngineException.getProblemMark()).thenReturn(Optional.of(new Mark("mark", 1, 1, 1, new int[0], 1)));
    when(yamlEngineException.getMessage()).thenReturn("message");

    InputFile inputFile = mock(InputFile.class);
    when(inputFile.toString()).thenReturn("TestFile");
    when(inputFile.newPointer(2, 0)).thenReturn(new DefaultTextPointer(1, 0));

    ParseException e = sensor().toParseException("action", inputFile, yamlEngineException);
    assertThat(e)
      .hasMessage("Cannot action 'TestFile:1:1'")
      .extracting(ParseException::getPosition)
      .isEqualTo(new DefaultTextPointer(1, 0));
  }

  @Test
  void toParseException_with_MarkedYamlEngineException_and_without_marker() {
    MarkedYamlEngineException yamlEngineException = mock(MarkedYamlEngineException.class);
    when(yamlEngineException.getProblemMark()).thenReturn(Optional.empty());
    when(yamlEngineException.getMessage()).thenReturn("message");

    InputFile inputFile = mock(InputFile.class);
    when(inputFile.toString()).thenReturn("TestFile");

    ParseException e = sensor().toParseException("action", inputFile, yamlEngineException);
    assertThat(e)
      .hasMessage("Cannot action 'TestFile'")
      .extracting(ParseException::getPosition)
      .isNull();
  }

  @Test
  void toParseException_with_other_exception() {
    Exception exception = mock(Exception.class);
    when(exception.getMessage()).thenReturn("message");

    InputFile inputFile = mock(InputFile.class);
    when(inputFile.toString()).thenReturn("TestFile");
    when(inputFile.newPointer(2, 0)).thenReturn(new DefaultTextPointer(1, 0));

    ParseException e = sensor().toParseException("action", inputFile, exception);
    assertThat(e)
      .hasMessage("Cannot action 'TestFile'")
      .extracting(ParseException::getPosition)
      .isNull();
  }

  @Test
  void mainFilePredicate() {
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
      return new String[]{};
    }
  }
}
