/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
package org.sonar.iac.arm.symbols;

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.tree.api.ObjectExpression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.CTX;
import static org.sonar.iac.arm.ArmTestUtils.parseObject;

class ObjectSymbolTest {

  ObjectSymbol absent = ObjectSymbol.fromAbsent(CTX, "absentObject", null);
  ObjectExpression objectWithProp = parseObject("{\"key1\": \"value\", \"key2\": {} }");
  ObjectSymbol present = ObjectSymbol.fromPresent(CTX, objectWithProp, null, null);

  @Test
  void property() {
    assertThat(present.property("key1").isPresent()).isTrue();
    assertThat(present.property("key2").isPresent()).isTrue();
    assertThat(present.property("unknown").isPresent()).isFalse();
    assertThat(absent.property("key1").isPresent()).isFalse();
  }

  @Test
  void object() {
    assertThat(present.object("key1").isPresent()).isFalse();
    assertThat(present.object("key2").isPresent()).isTrue();
    assertThat(present.object("unknown").isPresent()).isFalse();
    assertThat(absent.object("key1").isPresent()).isFalse();
  }
}
