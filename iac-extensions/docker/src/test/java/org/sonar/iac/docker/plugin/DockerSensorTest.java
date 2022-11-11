package org.sonar.iac.docker.plugin;

import org.junit.jupiter.api.Test;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.iac.common.testing.AbstractSensorTest;
import org.sonar.iac.docker.parser.DockerParser;

import static org.assertj.core.api.Assertions.assertThat;

class DockerSensorTest extends AbstractSensorTest {

  @Test
  void shouldReturnDockerDescriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    sensor(checkFactory()).describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("IaC Docker Sensor");
    assertThat(descriptor.languages()).containsOnly("docker");
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
  void shouldReturnVisitors() {
    assertThat(sensor().visitors(null, null)).isEmpty();
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
      noSonarFilter,
      new DockerLanguage());
  }

  @Override
  protected String repositoryKey() {
    return "docker";
  }

  @Override
  protected String fileLanguageKey() {
    return "Dockerfile";
  }

  private DockerSensor sensor() {
    return (DockerSensor) sensor(checkFactory());
  }
}
