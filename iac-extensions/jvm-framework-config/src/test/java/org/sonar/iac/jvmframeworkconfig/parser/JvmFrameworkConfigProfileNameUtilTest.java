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
package org.sonar.iac.jvmframeworkconfig.parser;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.jvmframeworkconfig.tree.api.Tuple;
import org.sonar.iac.jvmframeworkconfig.tree.impl.ScalarImpl;
import org.sonar.iac.jvmframeworkconfig.tree.impl.SyntaxTokenImpl;
import org.sonar.iac.jvmframeworkconfig.tree.impl.TupleImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.jvmframeworkconfig.parser.JvmFrameworkConfigProfileNameUtil.profileName;

class JvmFrameworkConfigProfileNameUtilTest {

  @Test
  void shouldFilterOutDefaultProfileWithoutValue() {
    List<Tuple> tuples = List.of(buildTupleWithNullValue("spring.profiles.default"));
    String profileName = profileName(tuples);

    assertThat(profileName).isEqualTo("default");
  }

  @Test
  void shouldFilterOutActiveProfileWithoutValue() {
    List<Tuple> tuples = List.of(buildTupleWithNullValue("spring.profiles.active"));
    String profileName = profileName(tuples);

    assertThat(profileName).isEqualTo("default");
  }

  private static Tuple buildTupleWithNullValue(String key) {
    return new TupleImpl(new ScalarImpl(new SyntaxTokenImpl(key, null)), null);
  }
}
