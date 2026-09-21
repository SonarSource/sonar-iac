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

import com.sonarsource.scanner.engine.sensor.test.fixtures.SensorContextTester;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.iac.common.extension.SharedFileHeadReader;
import org.sonar.scanner.plugin.api.impl.config.MapSettings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.predicates.ArmJsonFilePredicate.ARM_JSON_FILE_IDENTIFIER_DEFAULT_VALUE;
import static org.sonar.iac.common.predicates.ArmJsonFilePredicate.ARM_JSON_FILE_IDENTIFIER_KEY;
import static org.sonar.iac.common.predicates.FilePredicateTestUtils.newInputFileMock;

class BicepGeneratedArmJsonFilePredicateTest {

  private static final String ARM_SCHEMA = "https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#";

  @TempDir
  private Path tempDir;
  private SensorContextTester sensorContext;
  private MapSettings settings;

  @BeforeEach
  void setUp() {
    settings = new MapSettings();
    settings.setProperty(ARM_JSON_FILE_IDENTIFIER_KEY, ARM_JSON_FILE_IDENTIFIER_DEFAULT_VALUE);
    sensorContext = SensorContextTester.create(tempDir).setSettings(settings);
  }

  @Test
  void shouldMatchFormattedBicepGeneratedArmJson() throws IOException {
    var file = inputFile("templates/generated.json", formattedBicepTemplate(), InputFile.Type.MAIN);

    assertThat(predicate().apply(file)).isTrue();
  }

  @Test
  void shouldMatchBicepGeneratedArmJsonWithByteOrderMark() throws IOException {
    var file = inputFile("templates/generated.json", "\uFEFF" + formattedBicepTemplate(), InputFile.Type.MAIN);

    assertThat(predicate().apply(file)).isTrue();
  }

  @Test
  void shouldMatchMinifiedBicepGeneratedArmJson() throws IOException {
    var file = inputFile("templates/generated.json",
      "{\"$schema\":\"%s\",\"metadata\":{\"_generator\":{\"name\":\"bicep\",\"version\":\"0.35.1\",\"templateHash\":\"42\"}}}".formatted(ARM_SCHEMA),
      InputFile.Type.MAIN);

    assertThat(predicate().apply(file)).isTrue();
  }

  @Test
  void shouldNotMatchBicepMarkerNestedInAnArray() throws IOException {
    var file = inputFile("templates/template.json", """
      {
        "$schema": "%s",
        "resources": [
          {
            "metadata": {
              "_generator": {
                "name": "bicep"
              }
            }
          }
        ]
      }
      """.formatted(ARM_SCHEMA), InputFile.Type.MAIN);

    assertThat(predicate().apply(file)).isFalse();
  }

  @ParameterizedTest
  @MethodSource("nonBicepMarkers")
  void shouldNotMatchOrdinaryOrOnlyMarkerLikeArmJson(String content) throws IOException {
    var file = inputFile("templates/template.json", content, InputFile.Type.MAIN);

    assertThat(predicate().apply(file)).isFalse();
  }

  @Test
  void shouldKeepGeneratedArmJsonExplicitlyIncludedInSourceScope() throws IOException {
    settings.setProperty("sonar.inclusions", "included/**");
    var explicitlyIncluded = inputFile("included/template.json", formattedBicepTemplate(), InputFile.Type.MAIN);
    var notIncluded = inputFile("generated/template.json", formattedBicepTemplate(), InputFile.Type.MAIN);

    assertThat(predicate().apply(explicitlyIncluded)).isFalse();
    assertThat(predicate().apply(notIncluded)).isTrue();
  }

  @Test
  void shouldGiveExclusionsPrecedenceOverSourceInclusions() throws IOException {
    settings.setProperty("sonar.inclusions", "templates/**");
    settings.setProperty("sonar.exclusions", "templates/generated/**");
    var file = inputFile("templates/generated/template.json", formattedBicepTemplate(), InputFile.Type.MAIN);

    assertThat(predicate().apply(file)).isTrue();
  }

  @Test
  void shouldNotTreatBlankInclusionsAsAnExplicitOptIn() throws IOException {
    settings.setProperty("sonar.inclusions", " , ");
    var file = inputFile("templates/generated.json", formattedBicepTemplate(), InputFile.Type.MAIN);

    assertThat(predicate().apply(file)).isTrue();
  }

  @Test
  void shouldUseTestScopePatternsForTestFiles() throws IOException {
    settings.setProperty("sonar.inclusions", "tests/**");
    settings.setProperty("sonar.test.inclusions", "test-fixtures/**");
    var sourceMatchedTestFile = inputFile("tests/generated.json", formattedBicepTemplate(), InputFile.Type.TEST);
    var testMatchedFile = inputFile("test-fixtures/generated.json", formattedBicepTemplate(), InputFile.Type.TEST);

    assertThat(predicate().apply(sourceMatchedTestFile)).isTrue();
    assertThat(predicate().apply(testMatchedFile)).isFalse();
  }

  private FilePredicate predicate() {
    var sharedFileHeadReader = new SharedFileHeadReader();
    return BicepGeneratedArmJsonFilePredicate.create(sensorContext.fileSystem().predicates(), sensorContext.config(),
      false, sharedFileHeadReader);
  }

  private InputFile inputFile(String relativePath, String content, InputFile.Type type) throws IOException {
    return newInputFileMock(relativePath, content, "json", type);
  }

  private static Stream<String> nonBicepMarkers() {
    return Stream.of(
      "{\"$schema\":\"%s\"}".formatted(ARM_SCHEMA),
      "{\"$schema\":\"%s\",\"resource\":{\"metadata\":{\"_generator\":{\"name\":\"bicep\"}}}}".formatted(ARM_SCHEMA),
      "{\"$schema\":\"%s\",\"metadata\":[{\"_generator\":{\"name\":\"bicep\"}}]}".formatted(ARM_SCHEMA),
      "{\"$schema\":\"%s\",\"metadata\":{\"_generator\":{\"name\":\"arm\"}}}".formatted(ARM_SCHEMA),
      "{\"$schema\":\"%s\",\"metadata\":{\"_generator\":{}}}".formatted(ARM_SCHEMA),
      "{\"$schema\":\"%s\",\"description\":\"metadata._generator.name = bicep\"}".formatted(ARM_SCHEMA));
  }

  private static String formattedBicepTemplate() {
    return """
      {
        "$schema": "%s",
        "contentVersion": "1.0.0.0",
        "metadata": {
          "_generator": {
            "name": "bicep",
            "version": "0.35.1",
            "templateHash": "42"
          }
        }
      }
      """.formatted(ARM_SCHEMA);
  }
}
