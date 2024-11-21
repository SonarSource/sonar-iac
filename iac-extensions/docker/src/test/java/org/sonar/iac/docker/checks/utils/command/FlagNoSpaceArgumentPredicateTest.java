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
package org.sonar.iac.docker.checks.utils.command;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.docker.checks.utils.CommandDetectorTestFactory.buildArgumentList;

class FlagNoSpaceArgumentPredicateTest {
  @ParameterizedTest
  @CsvSource({
    "-p,-pvalue,true",
    "-p,pvalue,false",
    "--password,--passwordvalue,true",
    "--password,passwordvalue,false",
  })
  void shouldMatchFlagsWithoutSpace(String flag, String input, boolean expected) {
    FlagNoSpaceArgumentPredicate predicate = new FlagNoSpaceArgumentPredicate(flag);
    FlagNoSpaceArgumentPredicate predicateCreatedByStaticMethod = StandardCommandDetectors.flagNoSpaceArgument(flag);
    var arguments = buildArgumentList(input).get(0);

    assertThat(predicate.test(arguments)).isEqualTo(expected);
    assertThat(predicateCreatedByStaticMethod.test(arguments)).isEqualTo(expected);
  }
}
