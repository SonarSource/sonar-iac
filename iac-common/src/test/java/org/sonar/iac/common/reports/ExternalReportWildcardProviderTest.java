/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.reports;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.event.Level;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.api.utils.Version;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class ExternalReportWildcardProviderTest {

  private static final String EXTERNAL_REPORTS_PROPERTY = "sonar.foo.mylinter.reportPaths";

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  @Test
  void shouldReturnEmptyListWhenOldSonarQubeVersion() {
    var runtime = SonarRuntimeImpl.forSonarQube(Version.create(7, 1), SonarQubeSide.SCANNER, SonarEdition.COMMUNITY);
    var context = SensorContextTester.create(new File(".")).setRuntime(runtime);
    context.settings().setProperty(EXTERNAL_REPORTS_PROPERTY, "dummy.txt");

    var reportFiles = ExternalReportWildcardProvider.getReportFiles(context, EXTERNAL_REPORTS_PROPERTY);

    assertThat(reportFiles).isEmpty();
    assertThat(logTester.logs(Level.WARN)).contains("Import of external issues aborted! Import requires SonarQube 7.2 or greater.");
  }

  @Test
  void shouldReturnEmptyListWhenEmptyProperty() {
    var context = SensorContextTester.create(new File("."));

    var reportFiles = ExternalReportWildcardProvider.getReportFiles(context, EXTERNAL_REPORTS_PROPERTY);

    assertThat(reportFiles).isEmpty();
    assertThat(logTester.logs(Level.WARN)).isEmpty();
  }

  static List<Arguments> shouldReturnFilesForPattern() {
    return List.of(
      Arguments.of("big*.txt", List.of("big_file_identifier_after_buffer.txt", "big_file_identifier_in_buffer.txt")),
      Arguments.of("big_file_identifier_after_buffer.txt, big_file_identifier_in_buffer.txt",
        List.of("big_file_identifier_after_buffer.txt", "big_file_identifier_in_buffer.txt")),
      Arguments.of("ext-json-report/**", List.of("noArray.json", "parseError.json", "validIssue.json")),
      Arguments.of("/ext-json-report/**", List.of("noArray.json", "parseError.json", "validIssue.json")),
      Arguments.of("**/ext-json-report/**", List.of("noArray.json", "parseError.json", "validIssue.json")),
      Arguments.of("**/rules/**", List.of("S1.html", "S1.json", "Sonar_way_profile.json")),
      Arguments.of("**/rules/**/S?.json", List.of("S1.json")),
      Arguments.of("org/sonar/l10n/test/rules/test/S?.json", List.of("S1.json")),
      Arguments.of("ext-json-report/noArray.json", List.of("noArray.json")),
      Arguments.of(Path.of("src/test/resources").toAbsolutePath() + "/ext-json-report/noArray.json", List.of("noArray.json")),
      Arguments.of("**/doesnt-exist/**", List.of()),
      Arguments.of("doesnt-exist.txt", List.of()),
      Arguments.of("none*", List.of()),
      Arguments.of("", List.of()));
  }

  @ParameterizedTest(name = "{index} should return files for pattern: {0}")
  @MethodSource
  void shouldReturnFilesForPattern(String pattern, List<String> expectedFiles) {
    var basePath = "src" + File.separatorChar + "test" + File.separatorChar + "resources";
    SensorContextTester context = SensorContextTester.create(new File(basePath));
    context.settings().setProperty(EXTERNAL_REPORTS_PROPERTY, pattern);

    List<File> reportFiles = ExternalReportWildcardProvider.getReportFiles(context, EXTERNAL_REPORTS_PROPERTY);

    var files = reportFiles.stream().map(File::getName);
    assertThat(files).containsAll(expectedFiles);
  }

  @Test
  void shouldReturnEmptyListWhenIOException() throws IOException {
    var context = SensorContextTester.create(new File("."));
    context.settings().setProperty(EXTERNAL_REPORTS_PROPERTY, "foo*");

    try (var ignored = mockStatic(Files.class)) {
      when(Files.walk(any(Path.class))).thenThrow(new IOException("boom"));

      var reportFiles = ExternalReportWildcardProvider.getReportFiles(context, EXTERNAL_REPORTS_PROPERTY);

      assertThat(reportFiles).isEmpty();
      assertThat(logTester.logs(Level.DEBUG)).contains("Exception when searching for report files to import.");
    }
  }
}
