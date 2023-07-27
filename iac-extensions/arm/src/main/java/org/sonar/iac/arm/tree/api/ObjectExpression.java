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
package org.sonar.iac.arm.tree.api;

import java.util.List;
import java.util.stream.Stream;
import org.sonar.iac.common.api.tree.HasProperties;
import org.sonar.iac.common.api.tree.PropertyTree;

public interface ObjectExpression extends Expression, HasProperties {
  List<ResourceDeclaration> nestedResources();

  default Stream<PropertyTree> allLiteralProperties() {
    return properties().stream().flatMap(p -> {
      Expression value = (Expression) p.value();
      if (value instanceof ObjectExpression) {
        return ((ObjectExpression) value).allLiteralProperties();
      } else if (value instanceof StringLiteral) {
        return Stream.of(p);
      } else {
        return Stream.empty();
      }
    });
  }
}
