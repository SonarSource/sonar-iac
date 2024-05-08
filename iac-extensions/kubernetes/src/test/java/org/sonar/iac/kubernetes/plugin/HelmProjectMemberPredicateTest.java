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
package org.sonar.iac.kubernetes.plugin;

import java.nio.file.Path;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.iac.common.testing.IacTestUtils;
import org.sonar.iac.kubernetes.plugin.predicates.HelmProjectMemberPredicate;

import static org.assertj.core.api.Assertions.assertThat;

class HelmProjectMemberPredicateTest {
  protected SensorContextTester context = SensorContextTester.create(Path.of("src/test/resources/").toAbsolutePath());

  @ParameterizedTest
  @CsvSource({
    "helm/templates/pod.yaml,true",
    "helm/templates/nested/pod.yaml,true",
    "helm/templates/nested/double-nested/pod.yaml,true",
    "large_file_with_identifier.yaml,false",
  })
  void shouldDetectFilesInHelmProject(String filePath, boolean shouldMatch) {
    InputFile templateFile = IacTestUtils.inputFile(filePath, "yaml");

    FilePredicate filePredicate = new HelmProjectMemberPredicate(context);
    assertThat(filePredicate.apply(templateFile)).isEqualTo(shouldMatch);
  }
}
