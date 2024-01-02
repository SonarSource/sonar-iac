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
package org.sonar.iac.arm.tree.impl.json;

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.parser.ArmParser;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.Property;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.arm.tree.impl.json.PropertyTestUtils.LINE_OFFSET;
import static org.sonar.iac.arm.tree.impl.json.PropertyTestUtils.parseProperty;

class ObjectExpressionImplTest {
  private final ArmParser parser = new ArmParser();

  @Test
  void shouldParseObjectExpression() {
    ObjectExpression objectExpression = (ObjectExpression) parseProperty(parser, "\"object_prop\": {\"key\":\"val\"}").value();

    assertThat(objectExpression)
      .containsKeyValue("key", "val")
      .hasRange(LINE_OFFSET + 1, 15, LINE_OFFSET + 1, 28);
    assertThat(objectExpression.getKind()).isEqualTo(ArmTree.Kind.OBJECT_EXPRESSION);
    assertThat(objectExpression.nestedResources()).isEmpty();
    assertThat(objectExpression.children()).hasSize(2);
    assertThat(((ArmTree) objectExpression.children().get(0)).getKind()).isEqualTo(ArmTree.Kind.IDENTIFIER);
    assertThat(((ArmTree) objectExpression.children().get(1)).getKind()).isEqualTo(ArmTree.Kind.STRING_LITERAL);
  }

  @Test
  void shouldParseEmptyObjectExpression() {
    Property objectProperty = parseProperty(parser, "\"object_prop\": {}");
    assertThat(objectProperty.value()).asObjectExpression()
      .hasSize(0)
      .hasRange(LINE_OFFSET + 1, 15, LINE_OFFSET + 1, 17);
  }
}
