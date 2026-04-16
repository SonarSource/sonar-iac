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
package org.sonar.iac.jvmframeworkconfig.plugin.visitors;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.languages.IacLanguage;
import org.sonar.iac.jvmframeworkconfig.parser.JvmFrameworkConfigParser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class JvmFrameworkConfigTelemetryVisitorTest {

  @TempDir
  Path tempDir;

  SensorContextTester sensorContext;
  @Spy
  JvmFrameworkConfigTelemetryVisitor telemetryVisitor;
  JvmFrameworkConfigParser parser = new JvmFrameworkConfigParser();
  AutoCloseable mocks;

  @BeforeEach
  void setUp() {
    mocks = MockitoAnnotations.openMocks(this);
    sensorContext = SensorContextTester.create(tempDir);
  }

  @AfterEach
  void tearDown() throws Exception {
    mocks.close();
  }

  @Test
  void shouldStoreZeroWhenNoFrameworkDetected() {
    scan(// language=properties
      """
        some.unrelated.key=value
        springframework.some.key=value
        quarkusframework.some.key=value
        micronautframework.some.key=value
        """);
    assertThat(telemetryVisitor.springRecorded).isFalse();
    assertThat(telemetryVisitor.micronautRecorded).isFalse();
    assertThat(telemetryVisitor.quarkusRecorded).isFalse();
    assertThat(telemetryVisitor.allFrameworksRecorded()).isFalse();
  }

  @Test
  void shouldDetectSpring() {
    scan(// language=properties
      """
        spring.datasource.password=abjlkhubjlkkbjlkavgui
        spring.datasource.username=abjlkhubjlkkbjlkavgui
        """);
    assertThat(telemetryVisitor.springRecorded).isTrue();
    assertThat(telemetryVisitor.micronautRecorded).isFalse();
    assertThat(telemetryVisitor.quarkusRecorded).isFalse();
    assertThat(telemetryVisitor.allFrameworksRecorded()).isFalse();
  }

  @Test
  void shouldDetectOnlyQuarkus() {
    scan(// language=properties
      """
        quarkus.datasource.db-kind=postgresql
        quarkus.datasource.username=abjlkhubjlkkbjlkavgui
        """);
    assertThat(telemetryVisitor.quarkusRecorded).isTrue();
    assertThat(telemetryVisitor.springRecorded).isFalse();
    assertThat(telemetryVisitor.micronautRecorded).isFalse();
  }

  @Test
  void shouldDetectQuarkusWithProfilePrefix() {
    scan(// language=properties
      """
        %prod.quarkus.datasource.username=sa
        """);
    assertThat(telemetryVisitor.quarkusRecorded).isTrue();
  }

  @Test
  void shouldDetectOnlyMicronaut() {
    scan(// language=properties
      """
        micronaut.application.name=my-app
        micronaut.server.port=8080
        """);
    assertThat(telemetryVisitor.micronautRecorded).isTrue();
    assertThat(telemetryVisitor.springRecorded).isFalse();
    assertThat(telemetryVisitor.quarkusRecorded).isFalse();
  }

  @Test
  void shouldRecordAll() {
    scan(// language=properties
      """
        micronaut.application.name=my-app
        quarkus.datasource.username=abjlkhubjlkkbjlkavgui
        spring.datasource.password=abjlkhubjlkkbjlkavgui
        """);
    assertThat(telemetryVisitor.allFrameworksRecorded()).isTrue();
  }

  @Test
  void shouldAccumulateFrameworksAcrossFiles() {
    scan(// language=properties
      """
        spring.datasource.url=jdbc:h2:mem:db
        """);
    scan(// language=properties
      """
        quarkus.datasource.username=abjlkhubjlkkbjlkavgui
        """);

    assertThat(telemetryVisitor.springRecorded).isTrue();
    assertThat(telemetryVisitor.quarkusRecorded).isTrue();
    assertThat(telemetryVisitor.micronautRecorded).isFalse();
  }

  @Test
  void shouldStopOnAllFrameworksRecorded() {
    scan(// language=properties
      """
        spring.datasource.url=jdbc:h2:mem:db
        """);
    scan(// language=properties
      """
        quarkus.datasource.username=abjlkhubjlkkbjlkavgui
        """);
    scan(// language=properties
      """
        micronaut.application.name=my-app
        extra.key=value
        """);

    assertThat(telemetryVisitor.allFrameworksRecorded()).isTrue();
    // 3 = one per framework key; extra.key is not processed because the early exit fires first
    verify(telemetryVisitor, times(3)).recordFramework(anyString());
  }

  private void scan(String content) {
    var inputFile = new TestInputFileBuilder("moduleKey", "test.properties")
      .setCharset(StandardCharsets.UTF_8)
      .setContents(content)
      .build();
    var ctx = new InputFileContext(sensorContext, inputFile, IacLanguage.UNKNOWN);
    telemetryVisitor.scan(ctx, parser.parse(content, ctx));
  }
}
