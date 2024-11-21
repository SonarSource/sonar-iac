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

class TopLevelPropertiesOrderCheckTest {

  private static final String PRIMARY_MESSAGE = "Reorder the elements to match the recommended order.";
  private static final String DIR = "TopLevelPropertiesOrderCheck/";
  private static final TopLevelPropertiesOrderCheck CHECK = new TopLevelPropertiesOrderCheck();

  @Test
  void shouldVerifyExpectedTopLevelJson() {
    ArmVerifier.verifyNoIssue(DIR + "topLevelExpected.json", CHECK);
  }

  static Stream<Arguments> shouldVerifyUnexpectedTopLevelJson() {
    return Stream.of(
      // filename, primaryTextRange
      arguments("topLevelContentVersionAndSchema.json", range(4, 2, 4, 11)),
      arguments("topLevelExpectedFunctionsAndParametersAndOutputsAndResources.json", range(6, 2, 6, 14)),
      arguments("topLevelFunctionsAndParameters.json", range(6, 2, 6, 14)),
      arguments("topLevelFunctionsAndParametersCaseInsensitive.json", range(6, 2, 6, 14)),
      arguments("topLevelOutputsAsFirst.json", range(3, 2, 3, 11)),
      arguments("topLevelResourcesAndParameters.json", range(5, 2, 5, 14)),
      arguments("topLevelSchemaAtEnd.json", range(10, 2, 10, 11)),
      arguments("topLevelVariablesBeforeFunctions.json", range(6, 2, 6, 14)));
  }

  @ParameterizedTest
  @MethodSource
  void shouldVerifyUnexpectedTopLevelJson(String filename, TextRange primaryTextRange) {
    var issue = issue(primaryTextRange, PRIMARY_MESSAGE);
    ArmVerifier.verify(DIR + filename, CHECK, issue);
  }

  @Test
  void shouldNotFailOnEmptyJson() {
    var content = """
      {
      }""";
    ArmVerifier.verifyContent(content, CHECK);
  }

  @Test
  void shouldNotFailOnUnknownTopLevelPropertyJson() {
    var content = """
      {
        "$schema": "https://schema.management.azure.com/schemas/2019-04-01/...",
        "unknown": ""
      }""";
    ArmVerifier.verifyContent(content, CHECK);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "topLevelExpected.bicep",
    "topLevelExpectedMultipleElements.bicep",
    "topLevelExpectedNotAllElements.bicep",
    "topLevelExpectedSubresources.bicep"})
  void shouldVerifyExpectedTopLevelBicep(String filename) {
    BicepVerifier.verifyNoIssue(DIR + filename, CHECK);
  }

  @ParameterizedTest
  @ValueSource(strings = {"topLevelExistingResourceBetweenAnotherResources.bicep",
    "topLevelMetadataAndTargetScope.bicep",
    "topLevelModuleAndResourcesMixed.bicep",
    "topLevelOutputAsFirst.bicep",
    "topLevelParametersAndVariables.bicep",
    "topLevelResourceExistingBetweenOtherResource.bicep",
    "topLevelTargetScopeEnd.bicep"
  })
  void shouldVerifyUnexpectedTopLevelBicep(String filename) {
    BicepVerifier.verify(DIR + filename, CHECK);
  }
}
