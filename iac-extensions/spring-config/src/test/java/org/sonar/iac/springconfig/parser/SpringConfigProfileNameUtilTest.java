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
package org.sonar.iac.springconfig.parser;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.springconfig.tree.api.Tuple;
import org.sonar.iac.springconfig.tree.impl.ScalarImpl;
import org.sonar.iac.springconfig.tree.impl.SyntaxTokenImpl;
import org.sonar.iac.springconfig.tree.impl.TupleImpl;

import static org.assertj.core.api.Assertions.assertThat;

class SpringConfigProfileNameUtilTest {

  @Test
  void shouldFilterOutDefaultProfileWithoutValue() {
    List<Tuple> tuples = List.of(buildTupleWithNullValue("spring.profiles.default"));
    String profileName = SpringConfigProfileNameUtil.profileName(tuples);

    assertThat(profileName).isEqualTo("default");
  }

  @Test
  void shouldFilterOutActiveProfileWithoutValue() {
    List<Tuple> tuples = List.of(buildTupleWithNullValue("spring.profiles.active"));
    String profileName = SpringConfigProfileNameUtil.profileName(tuples);

    assertThat(profileName).isEqualTo("default");
  }

  private static Tuple buildTupleWithNullValue(String key) {
    return new TupleImpl(new ScalarImpl(new SyntaxTokenImpl(key, null)), null);
  }
}
