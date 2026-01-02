/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.inputFile;

class HelmProjectMemberPredicateTest {
  protected SensorContextTester context = SensorContextTester.create(Path.of("src/test/resources/").toAbsolutePath());

  @ParameterizedTest
  @CsvSource({
    "helm/templates/pod.yaml,true",
    "helm/templates/nested/pod.yaml,true",
    "helm/templates/nested/double-nested/pod.yaml,true",
    "small_file.txt,false",
  })
  void shouldDetectFilesInHelmProject(String filePath, boolean shouldMatch) {
    InputFile templateFile = inputFile(filePath, "yaml");

    FilePredicate filePredicate = new HelmProjectMemberPredicate(context);
    assertThat(filePredicate.apply(templateFile)).isEqualTo(shouldMatch);
  }
}
