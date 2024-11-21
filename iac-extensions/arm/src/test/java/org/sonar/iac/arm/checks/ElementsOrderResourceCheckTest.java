/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.arm.checks;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.common.api.tree.impl.TextRange;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.Verifier.issue;

class ElementsOrderResourceCheckTest {

  private static final String PRIMARY_MESSAGE = "Reorder the elements to match the recommended order.";
  private static final String DIR = "ElementsOrderResource/";
  private static final ElementsOrderResourceCheck CHECK = new ElementsOrderResourceCheck();

  @ParameterizedTest
  @ValueSource(strings = {"resourceExpected.json",
    "resourceExpectedMoreResources.json",
    "resourceExpectedMoreResources.json",
    "resourceExpectedSmall.json"})
  void shouldVerifyExpectedResourceJson(String filename) {
    ArmVerifier.verifyNoIssue(DIR + filename, CHECK);
  }

  static Stream<Arguments> shouldVerifyUnexpectedResourceJson() {
    return Stream.of(
      // filename, primaryTextRange
      arguments("resourceCommentsAtEnd.json", range(23, 6, 23, 16)),
      arguments("resourceKindScale.json", range(13, 6, 13, 12)),
      arguments("resourceNameAndApiVersionAndType.json", range(9, 6, 9, 18)),
      arguments("resourceNameAndApiVersionAndTypeCaseInsensitive.json", range(9, 6, 9, 18)),
      arguments("resourceOnlySecondUnexpected.json", range(16, 6, 16, 16)),
      arguments("resourceOtherPropertiesBetween.json", range(9, 6, 9, 12)),
      arguments("resourcePropertiesAtBeginning.json", range(6, 6, 6, 16)));
  }

  @ParameterizedTest
  @MethodSource
  void shouldVerifyUnexpectedResourceJson(String filename, TextRange primaryTextRange) {
    var issue = issue(primaryTextRange, PRIMARY_MESSAGE);
    ArmVerifier.verify(DIR + filename, CHECK, issue);
  }

  @Test
  void shouldVerifyTwoUnexpectedResourcesInOneFile() {
    var issue1 = issue(range(6, 6, 6, 16), PRIMARY_MESSAGE);
    var issue2 = issue(range(17, 6, 17, 12), PRIMARY_MESSAGE);
    ArmVerifier.verify(DIR + "resourceTwoUnexpected.json", CHECK, issue1, issue2);
  }

  @ParameterizedTest
  @ValueSource(strings = {"resourceExpected.bicep",
    "resourceExpectedMoreResources.bicep",
    "resourceExpectedSmall.bicep"})
  void shouldVerifyExpectedResourceBicep(String filename) {
    BicepVerifier.verifyNoIssue(DIR + filename, CHECK);
  }

  @ParameterizedTest
  @ValueSource(strings = {"resourceMoreIssues.bicep",
    "resourceOnlySecondUnexpected.bicep",
    "resourceParentAtEnd.bicep",
    "resourcePlanAndTags.bicep",
    "resourcePropertiesFirst.bicep",
    "resourceScopeAndParent.bicep"})
  void shouldVerifyUnexpectedResourceBicep(String filename) {
    BicepVerifier.verify(DIR + filename, CHECK);
  }

  @ParameterizedTest
  @ValueSource(strings = {"decoratorExpected.bicep",
    "decoratorExpectedFullyQualified.bicep",
    "decoratorExpectedAndOthers.bicep",
    "decoratorExpectedBatchSizeOnly.bicep",
    "decoratorExpectedDescriptionOnly.bicep"
  })
  void shouldVerifyDecorator(String filename) {
    BicepVerifier.verifyNoIssue(DIR + filename, CHECK);
  }

  @ParameterizedTest
  @ValueSource(strings = {"decoratorCustomFirst.bicep",
    "decoratorCustomFirstOnlyBatchSize.bicep",
    "decoratorCustomInBetween.bicep",
    "decoratorDescriptionAndBatchSize.bicep",
    "decoratorMoreResources.bicep"})
  void shouldVerifyUnexpectedDecorator(String filename) {
    BicepVerifier.verify(DIR + filename, CHECK);
  }

}
