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
package org.sonar.iac.common.reports;

import java.io.File;
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

class ExternalReportWildcardProviderTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();

  private final String EXTERNAL_REPORTS_PROPERTY = "sonar.foo.mylinter.reportPaths";

  @Test
  void shouldReturnEmptyListWhenOldSonarQubeVersion() {
    var runtime = SonarRuntimeImpl.forSonarQube(Version.create(7, 1), SonarQubeSide.SCANNER, SonarEdition.COMMUNITY);
    var context = SensorContextTester.create(new File(".")).setRuntime(runtime);

    var reportFiles = ExternalReportWildcardProvider.getReportFiles(context, EXTERNAL_REPORTS_PROPERTY);

    assertThat(reportFiles).isEmpty();
    assertThat(logTester.logs(Level.ERROR)).contains("Import of external issues requires SonarQube 7.2 or greater.");
  }

  @Test
  void shouldReturnEmptyListWhenEmptyProperty() {
    var context = SensorContextTester.create(new File("."));

    var reportFiles = ExternalReportWildcardProvider.getReportFiles(context, EXTERNAL_REPORTS_PROPERTY);

    assertThat(reportFiles).isEmpty();
    assertThat(logTester.logs(Level.ERROR)).doesNotContain("Import of external issues requires SonarQube 7.2 or greater.");
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
      Arguments.of("**/doesnt-exist/**", List.of()));
  }

  @ParameterizedTest(name = "{index} should return files for pattern: {0}")
  @MethodSource
  void shouldReturnFilesForPattern(String pattern, List<String> expectedFiles) {
    var basePath = "src/test/resources/";
    SensorContextTester context = SensorContextTester.create(new File(basePath));
    context.settings().setProperty(EXTERNAL_REPORTS_PROPERTY, pattern);

    List<File> reportFiles = ExternalReportWildcardProvider.getReportFiles(context, EXTERNAL_REPORTS_PROPERTY);

    var files = reportFiles.stream().map(File::getName);
    assertThat(files).containsAll(expectedFiles);
  }

}
