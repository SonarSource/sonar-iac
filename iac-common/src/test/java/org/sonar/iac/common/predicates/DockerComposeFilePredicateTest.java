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
import com.sonarsource.scanner.engine.sensor.test.fixtures.TestInputFileBuilder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Locale;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.extension.DurationStatistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class DockerComposeFilePredicateTest {

  @TempDir
  Path tempDir;

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  @ParameterizedTest
  @MethodSource("defaultPaths")
  void shouldMatchEveryDefaultTier(String path) {
    var inputFile = inputFile(path, "yaml");
    var predicate = predicate();

    var matches = predicate.apply(inputFile);

    assertThat(matches).isTrue();
  }

  static Stream<String> defaultPaths() {
    return Stream.of(
      "compose", "docker-compose",
      "compose.dev", "compose.override", "compose.", "compose.dev.override",
      "docker-compose.prod", "docker-compose.override", "docker-compose.",
      "docker.compose", "docker.compose.prod",
      "docker_compose", "docker_compose-dev",
      "docker-compose-", "docker-compose-dev",
      "-compose", "service-compose",
      ".docker-compose", "service.docker-compose")
      .flatMap(stem -> Stream.of("yaml", "yml").map(extension -> stem + "." + extension))
      .flatMap(filename -> Stream.of("", "deploy/nested/", ".devcontainer/").map(prefix -> prefix + filename))
      .flatMap(path -> Stream.of(path, path.toUpperCase(Locale.ROOT)));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "config.yaml", "mycompose.yaml", "compose-dev.yaml", "compose_dev.yml", "composefoo.yaml", "dockercompose.yaml",
    "my-compose-dev.yaml", "my.docker-compose.prod.yaml", "docker-composefoo.yaml", "compose.json", "compose.yaml.bak",
    "compose.yaml/template.yml", "compose.dev/other.yaml", "docker.compose/foo.yaml", "compose.yml.txt"
  })
  void shouldRejectPathsOutsideDefaultTiers(String path) {
    var inputFile = inputFile(path, "yaml");
    var predicate = predicate();

    var matches = predicate.apply(inputFile);

    assertThat(matches).isFalse();
  }

  @ParameterizedTest
  @CsvSource(value = {
    "yaml,true",
    "dockercompose,true",
    "docker,false",
    "json,false",
    "kubernetes,false",
    "githubactions,false",
    "ansible,false",
    "YAML,false",
    "NULL,false"
  }, nullValues = "NULL")
  void shouldAcceptOnlyCandidateLanguages(@Nullable String language, boolean expectedMatch) {
    var inputFile = inputFile("compose.yaml", language);
    var predicate = predicate();

    var matches = predicate.apply(inputFile);

    assertThat(matches).isEqualTo(expectedMatch);
  }

  @ParameterizedTest
  @ValueSource(strings = {"arbitrary", "config.yaml", "nested/settings.json", ".devcontainer/action.yml", ".github/actions/build/action.yml"})
  void shouldAcceptExplicitLanguageWithoutMatchingFilenamePatterns(String path) {
    var inputFile = inputFile(path, "dockercompose");
    var predicate = predicate();

    var matches = predicate.apply(inputFile);

    assertThat(matches).isTrue();
  }

  @ParameterizedTest
  @MethodSource("excludedPaths")
  void shouldAlwaysExcludeReservedPaths(String path, String language) {
    var inputFile = inputFile(path, language);
    var predicate = predicate();

    var matches = predicate.apply(inputFile);

    assertThat(matches).isFalse();
  }

  static Stream<Arguments> excludedPaths() {
    var filenames = Stream.of("compose.yaml.j2", "compose.md", "compose.markdown", "kustomization.yaml", "kustomization.yml", "Kustomization")
      .flatMap(path -> Stream.of(path, path.toUpperCase(Locale.ROOT)));
    // GitHub only reads the lowercase workflow directory, so only the filename varies in case.
    var workflows = Stream.of(".github/workflows/compose.yaml", ".github/workflows/nested/compose.yml", ".github/workflows/config.json", ".github/workflows/COMPOSE.YAML");
    return Stream.concat(filenames, workflows)
      .flatMap(path -> Stream.of(path, "nested/" + path, ".hidden/" + path))
      .flatMap(path -> Stream.of("yaml", "dockercompose").map(language -> arguments(path, language)));
  }

  @ParameterizedTest
  @CsvSource({
    "action.yml,false",
    ".github/actions/build/action.yml,false",
    ".github/workflows-backup/compose.yaml,true",
    ".github/workflows.yaml,false",
    "github/workflows/compose.yaml,true",
    ".GITHUB/WORKFLOWS/compose.yaml,true",
    ".github/workflow/compose.yaml,true",
    "kustomization.yaml.bak,false",
    "my-kustomization.yaml,false",
    "compose.markdown.yaml,true",
    "compose.j2.yaml,true",
    "kustomization/compose.yaml,true"
  })
  void shouldNotBroadenHardExclusions(String path, boolean matchesBuiltInPatterns) {
    var yamlFile = inputFile(path, "yaml");
    var explicitFile = inputFile(path, "dockercompose");
    var predicate = predicate();

    var yamlMatches = predicate.apply(yamlFile);
    var explicitMatches = predicate.apply(explicitFile);

    assertThat(yamlMatches).isEqualTo(matchesBuiltInPatterns);
    assertThat(explicitMatches).isTrue();
  }

  @Test
  void shouldReportDockerComposeFileType() {
    assertThat(predicate().fileType()).isEqualTo(FileType.DOCKER_COMPOSE);
  }

  @ParameterizedTest
  @CsvSource({"true, true", "false, false"})
  void shouldLogIdentifiedFilesOnlyWhenExtendedLoggingIsEnabled(boolean extendedLogging, boolean expectedLog) {
    var inputFile = inputFile("compose.yaml", "yaml");
    var predicate = predicate(extendedLogging);

    assertThat(predicate.apply(inputFile)).isTrue();

    assertThat(logTester.logs().contains("Identified as Docker Compose file: " + inputFile)).isEqualTo(expectedLog);
  }

  private DockerComposeFilePredicate predicate() {
    return predicate(false);
  }

  private DockerComposeFilePredicate predicate(boolean extendedLogging) {
    var context = SensorContextTester.create(tempDir);
    var predicate = new DockerComposeFilePredicate(context.fileSystem().predicates(), extendedLogging);
    predicate.applyTimers(new DurationStatistics(context.config()));
    return predicate;
  }

  private InputFile inputFile(String path, @Nullable String language) {
    return new TestInputFileBuilder("moduleKey", path)
      .setModuleBaseDir(tempDir)
      .setLanguage(language)
      .setType(InputFile.Type.MAIN)
      .setCharset(StandardCharsets.UTF_8)
      .build();
  }
}
