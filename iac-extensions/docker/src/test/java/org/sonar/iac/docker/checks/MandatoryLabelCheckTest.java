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
package org.sonar.iac.docker.checks;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class MandatoryLabelCheckTest {

  @Test
  void shouldRaiseIssueOnNoncompliantFile() {
    DockerVerifier.verify("MandatoryLabelCheck/noncompliantWithMissingRequiredLabel.dockerfile", new MandatoryLabelCheck());
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "compliant_simple",
    "compliant_uppercase",
    "compliant_multipleImages",
    "compliant_onbuild",
    "compliant_withoutFromInstruction",
  })
  void shouldExecuteCheck(String name) {
    DockerVerifier.verifyNoIssue("MandatoryLabelCheck/%s.dockerfile".formatted(name), new MandatoryLabelCheck());
  }

  @Test
  void shouldRaiseIssueWhenMultipleRequiredLabelsAreMissing() {
    MandatoryLabelCheck mandatoryLabelCheck = new MandatoryLabelCheck();
    mandatoryLabelCheck.requiredLabels = "maintainer,version,anotherMissingLabel,description,missingLabel,testLabel";

    DockerVerifier.verify("MandatoryLabelCheck/multipleLabelsRequired.dockerfile", mandatoryLabelCheck);
  }

  @Test
  void shouldFindNoIssueWhenAllRequiredLabelsArePresent() {
    MandatoryLabelCheck mandatoryLabelCheck = new MandatoryLabelCheck();
    mandatoryLabelCheck.requiredLabels = "maintainer,version,description";

    // verifyNoIssue doesn't check if the file contains a noncompliant comment, so we can reuse the multipleLabels.dockerfile
    DockerVerifier.verifyNoIssue("MandatoryLabelCheck/multipleLabelsRequired.dockerfile", mandatoryLabelCheck);
  }

  @ParameterizedTest
  @MethodSource
  void shouldSplitRequiredLabelsCorrectly(String labelList, Set<String> expectedLabels) {
    Set<String> splitLabels = MandatoryLabelCheck.splitLabels(labelList);

    assertThat(splitLabels).containsExactlyInAnyOrderElementsOf(expectedLabels);
  }

  static Stream<Arguments> shouldSplitRequiredLabelsCorrectly() {
    return Stream.of(
      Arguments.of("maintainer,version,description", Set.of("maintainer", "version", "description")),
      Arguments.of("", Set.of()),
      Arguments.of(" ", Set.of()),
      Arguments.of(",maintainer, ", Set.of("maintainer")));
  }

  @ParameterizedTest
  @MethodSource
  void shouldFormatLabelsCorrectly(List<String> labels, String expectedFormattedResult) {
    String formattedLabels = MandatoryLabelCheck.formatLabels(new ArrayList<>(labels));

    assertThat(formattedLabels).isEqualTo(expectedFormattedResult);
  }

  static Stream<Arguments> shouldFormatLabelsCorrectly() {
    return Stream.of(
      Arguments.of(List.of("first", "second"), "\"first\" and \"second\""),
      Arguments.of(List.of("first", "second", "inorder"), "\"first\", \"inorder\" and \"second\""),
      Arguments.of(List.of("first"), "\"first\""),
      Arguments.of(List.of(), ""));
  }
}
