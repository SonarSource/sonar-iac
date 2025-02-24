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
package org.sonar.iac.common.yaml;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.yaml.tree.FileTreeImpl;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.ScalarTreeImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.yaml.YamlTreeTestUtils.scalar;

class TreePredicatesTest {

  @Test
  void testIsTrue() {
    assertThat(TreePredicates.isTrue().test(scalar("true"))).isTrue();
    assertThat(TreePredicates.isTrue().test(scalar("false"))).isFalse();
    assertThat(TreePredicates.isTrue().test(notTextTree())).isFalse();
  }

  @Test
  void testIsEqualTo() {
    assertThat(TreePredicates.isEqualTo("VALUE_TEST").test(scalar("VALUE_TEST"))).isTrue();
    assertThat(TreePredicates.isEqualTo("VALUE_TEST").test(scalar("NOT_VALUE_TEST"))).isFalse();
    assertThat(TreePredicates.isEqualTo("VALUE_TEST").test(notTextTree())).isFalse();
  }

  @Test
  void testIsSet() {
    assertThat(TreePredicates.isSet().test(scalar(""))).isFalse();
    assertThat(TreePredicates.isSet().test(scalar("~"))).isFalse();
    assertThat(TreePredicates.isSet().test(scalar("null"))).isFalse();
    assertThat(TreePredicates.isSet().test(scalar("a"))).isTrue();
    assertThat(TreePredicates.isSet().test(scalar("SET_VALUE"))).isTrue();
    assertThat(TreePredicates.isSet().test(notTextTree())).isFalse();
  }

  @Test
  void testStartsWith() {
    assertThat(TreePredicates.startsWith(List.of("/etc")).test(text("/etc/init.d"))).isTrue();
    assertThat(TreePredicates.startsWith(List.of("/etc")).test(text("/var/init.d"))).isFalse();
    assertThat(TreePredicates.startsWith(List.of("/etc")).test(text("/var/etc/init.d"))).isFalse();
    assertThat(TreePredicates.startsWith(List.of("/etc", "/bin")).test(text("/bin/log"))).isTrue();
    assertThat(TreePredicates.startsWith(List.of("/etc", "/bin")).test(text("/var/log"))).isFalse();
    assertThat(TreePredicates.startsWith(List.of("/etc", "/bin")).test(text("etc/log"))).isFalse();
    assertThat(TreePredicates.startsWith(List.of("/etc", "/bin")).test(notTextTree())).isFalse();
  }

  private ScalarTree text(String value) {
    return new ScalarTreeImpl(value, null, null);
  }

  private FileTreeImpl notTextTree() {
    return new FileTreeImpl(null, null);
  }
}
