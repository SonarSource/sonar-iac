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
package org.sonar.iac.arm.checkdsl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.tree.api.ObjectExpression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.CTX;
import static org.sonar.iac.arm.ArmTestUtils.parseObject;

class ContextualObjectTest {

  ContextualObject absent = ContextualObject.fromAbsent(CTX, "absentObject", null);
  ObjectExpression objectWithProp = parseObject("{\"key1\": \"value\", \"key2\": {} , \"key3\": [{\"childObject\": {}}, {\"childObject\": {}, \"childObject\": \"string\"}]}");
  ContextualObject present = ContextualObject.fromPresent(CTX, objectWithProp, null, null);

  @Test
  void property() {
    assertThat(present.property("key1").isPresent()).isTrue();
    assertThat(present.property("key2").isPresent()).isTrue();
    assertThat(present.property("key3").isPresent()).isTrue();
    assertThat(present.property("unknown").isPresent()).isFalse();
    assertThat(absent.property("key1").isPresent()).isFalse();
  }

  @Test
  void object() {
    assertThat(present.object("key1").isPresent()).isFalse();
    assertThat(present.object("key2").isPresent()).isTrue();
    assertThat(present.object("key3").isPresent()).isFalse();
    assertThat(present.object("unknown").isPresent()).isFalse();
    assertThat(absent.object("key2").isPresent()).isFalse();
  }

  @Test
  void list() {
    assertThat(present.list("key1").isPresent()).isFalse();
    assertThat(present.list("key2").isPresent()).isFalse();
    assertThat(present.list("key3").isPresent()).isTrue();
    assertThat(present.list("unknown").isPresent()).isFalse();
    assertThat(absent.list("key3").isPresent()).isFalse();
  }

  @Test
  void objectsByPath() {
    assertThat(present.objectsByPath("key3/*/childObject")).hasSize(2);
    assertThat(present.objectsByPath("key1/*/childObject")).isEmpty();
  }
}
