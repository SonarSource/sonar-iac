/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
package org.sonar.iac.kubernetes.checks;

import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.object.BlockObject;
import org.sonar.iac.common.yaml.tree.MappingTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.sonar.iac.kubernetes.checks.AbstractResourceManagementCheck.getFirstChildElement;

class AbstractResourceManagementCheckTest {
  static MappingTree CONTAINER_TREE;

  static {
    var parser = new YamlParser();
    CONTAINER_TREE = (MappingTree) parser.parse("""
        containers:
          - name: foo
      """, null).documents().get(0);
  }

  @Test
  void testGetFirstChildElement() {
    CheckContext checkContext = mock(CheckContext.class);
    BlockObject block = BlockObject.fromAbsent(checkContext, "a");
    HasTextRange firstChildElement = getFirstChildElement(block);
    assertThat(firstChildElement).isNull();
  }

  @ParameterizedTest
  @CsvSource(nullValues = "null", textBlock = """
    1, true
    1Gi, true
    200M, true
    +200M, true
    -200M, true
    .5, true
    1.5Gi, true
    ~, false
    '', false
    1.5, true
    null, false""")
  void shouldDetectIfValueIsSet(@Nullable String value, boolean shouldBeValid) {
    assertThat(AbstractResourceManagementCheck.isSet(value)).isEqualTo(shouldBeValid);
  }
}
