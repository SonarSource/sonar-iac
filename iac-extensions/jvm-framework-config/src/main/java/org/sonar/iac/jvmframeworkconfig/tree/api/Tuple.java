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
package org.sonar.iac.jvmframeworkconfig.tree.api;

import org.jspecify.annotations.Nullable;

/**
 * Represents a key-value pair in a Spring configuration file.
 *
 * <p>Deliberately not a {@link org.sonar.iac.common.api.tree.PropertyTree}: that contract guarantees a non-null
 * {@code value()}, while a property in a {@code .properties} or YAML configuration file can be declared without any
 * value, e.g. {@code my.key=}.
 */
public interface Tuple extends JvmFrameworkConfig {
  /**
   * @return the key of the tuple.
   */
  Scalar key();

  /**
   * @return the value of the tuple, maybe null
   */
  @Nullable
  Scalar value();
}
