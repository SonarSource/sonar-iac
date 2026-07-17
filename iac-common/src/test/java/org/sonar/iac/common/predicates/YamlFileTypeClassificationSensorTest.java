/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.predicates;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.Phase;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.iac.common.testing.IacTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.predicates.ArmJsonFilePredicate.ARM_JSON_FILE_IDENTIFIER_DEFAULT_VALUE;
import static org.sonar.iac.common.predicates.ArmJsonFilePredicate.ARM_JSON_FILE_IDENTIFIER_KEY;
import static org.sonar.iac.common.predicates.CloudFormationFilePredicate.CLOUDFORMATION_FILE_IDENTIFIER_KEY;

class YamlFileTypeClassificationSensorTest {

  @TempDir
  Path tempDir;

  private static final String VALID_KUBERNETES_CONTENT = """
    apiVersion: v1
    kind: ConfigMap
    metadata:
      name: my-config
    """;

  @Test
  void shouldRunInPrePhaseSoItPrecedesTheAnalysisSensors() {
    // The sensor must warm the cache before the analysis sensors run, which relies on the @Phase(PRE) annotation.
    var phase = YamlFileTypeClassificationSensor.class.getAnnotation(Phase.class);
    assertThat(phase).isNotNull();
    assertThat(phase.name()).isEqualTo(Phase.Name.PRE);
  }

  @Test
  void shouldDescribeSensorWithCandidateLanguages() {
    var sensorContext = SensorContextTester.create(tempDir).setSettings(settings());
    var resolver = new YamlFileTypeResolver(sensorContext.fileSystem(), sensorContext.config(), new YamlFileTypeCache());
    var sensor = new YamlFileTypeClassificationSensor(resolver);

    var descriptor = new DefaultSensorDescriptor();
    sensor.describe(descriptor);

    assertThat(descriptor.name()).isEqualTo(YamlFileTypeClassificationSensor.SENSOR_NAME);
    assertThat(descriptor.languages()).containsExactlyInAnyOrderElementsOf(resolver.candidateLanguages());
  }

  @Test
  void shouldWarmTheSharedCacheOnExecute() {
    var sensorContext = SensorContextTester.create(tempDir).setSettings(settings());
    var kubernetesFile = IacTestUtils.inputFile("deploy.yaml", tempDir, VALID_KUBERNETES_CONTENT, "yaml");
    sensorContext.fileSystem().add(kubernetesFile);
    var cache = new YamlFileTypeCache();
    var resolver = new YamlFileTypeResolver(sensorContext.fileSystem(), sensorContext.config(), cache);
    var sensor = new YamlFileTypeClassificationSensor(resolver);

    // Before execution the file has not been classified yet.
    assertThat(cache.get(kubernetesFile.uri())).isNull();

    sensor.execute(sensorContext);

    // After the PRE-phase classification sensor ran, the shared cache is warmed for the analysis sensors: the file's
    // type is cached and the file system's ordered candidate list is memoized.
    assertThat(cache.get(kubernetesFile.uri())).isEqualTo(FileType.KUBERNETES);
    assertThat(cache.getClassifiedCandidates(sensorContext.fileSystem())).containsExactly(kubernetesFile);
  }

  @Test
  void shouldNotFailOnAProjectWithoutCandidateFiles() {
    var sensorContext = SensorContextTester.create(tempDir).setSettings(settings());
    var cache = new YamlFileTypeCache();
    var resolver = new YamlFileTypeResolver(sensorContext.fileSystem(), sensorContext.config(), cache);
    var sensor = new YamlFileTypeClassificationSensor(resolver);

    sensor.execute(sensorContext);

    // Classification ran (a cache hit for later sensors) but found no candidate file.
    assertThat(cache.getClassifiedCandidates(sensorContext.fileSystem())).isNotNull().isEmpty();
  }

  private static MapSettings settings() {
    var settings = new MapSettings();
    settings.setProperty(CLOUDFORMATION_FILE_IDENTIFIER_KEY, "AWSTemplateFormatVersion");
    settings.setProperty(ARM_JSON_FILE_IDENTIFIER_KEY, ARM_JSON_FILE_IDENTIFIER_DEFAULT_VALUE);
    return settings;
  }
}
