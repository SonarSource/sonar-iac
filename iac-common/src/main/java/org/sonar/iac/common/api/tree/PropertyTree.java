/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.api.tree;

import org.jspecify.annotations.Nullable;

public interface PropertyTree extends HasTextRange {
  Tree key();

  /**
   * @return the value of the property, or {@code null} for languages where a property can be declared without a value,
   *   e.g. {@code my.key=} in a {@code .properties} file. For most languages this is never {@code null}, and their
   *   implementations narrow the contract accordingly.
   */
  @Nullable
  Tree value();
}
