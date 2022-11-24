/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.docker.plugin;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.iac.common.testing.ExtensionSensorTest;
import org.sonar.iac.docker.parser.DockerParser;

import static org.assertj.core.api.Assertions.assertThat;

class DockerSensorTest extends ExtensionSensorTest {


  @Test
  void shouldReturnDockerDescriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    sensor().describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("IaC Docker Sensor");
    assertThat(descriptor.languages()).isEmpty();
    assertThat(descriptor.isProcessesFilesIndependently()).isTrue();
  }

  @Test
  void shouldReturnDockerParser() {
    assertThat(sensor().treeParser()).isInstanceOf(DockerParser.class);
  }

  @Test
  void shouldReturnRepositoryKey() {
    assertThat(sensor().repositoryKey()).isEqualTo(repositoryKey());
  }

  @Test
  void shouldReturnActivationSettingKey() {
    assertThat(sensor().getActivationSettingKey()).isEqualTo(getActivationSettingKey());
  }

  @Test
  void shouldAnalyzeDockerfilesOnly() {
    DockerSensor sensor = sensor();
    analyse(sensor,
      inputFile("Dockerfile", ""),
      inputFile("Dockerfile.foo", ""),
      inputFile("FooDockerfile", ""),
      inputFile("DockerfileFoo", ""),
      inputFile("Dockerfile.foo.bar", "")
    );

    FileSystem fileSystem = context.fileSystem();
    Iterable<InputFile> inputFiles = fileSystem.inputFiles(sensor.mainFilePredicate(context));

    assertThat(inputFiles)
      .extracting(inputFile -> Path.of(inputFile.uri()).getFileName().toString())
      .containsExactly("Dockerfile", "Dockerfile.foo.bar", "Dockerfile.foo");
  }

  @Test
  void shouldReturnVisitors() {
    assertThat(sensor().visitors(null, null)).hasSize(1);
  }

  @Override
  protected String getActivationSettingKey() {
    return DockerSettings.ACTIVATION_KEY;
  }

  @Override
  protected Sensor sensor(CheckFactory checkFactory) {
    return new DockerSensor(
      SONAR_RUNTIME_8_9,
      fileLinesContextFactory,
      checkFactory,
      noSonarFilter,
      new DockerLanguage());
  }

  @Override
  protected String repositoryKey() {
    return "docker";
  }

  @Override
  protected String fileLanguageKey() {
    return "docker";
  }

  @Override
  protected InputFile emptyFile() {
    return inputFile("Dockerfile", "");
  }

  @Override
  protected InputFile fileWithParsingError() {
    return inputFile("Dockerfile", "FOOBAR");
  }

  @Override
  protected InputFile validFile() {
    return inputFile("Dockerfile", "FROM");
  }

  private DockerSensor sensor(String... rules) {
    return (DockerSensor) sensor(checkFactory(rules));
  }

}
