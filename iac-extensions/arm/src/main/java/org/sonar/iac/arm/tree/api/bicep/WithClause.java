/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.arm.tree.api.bicep;

import org.sonar.iac.arm.tree.api.ObjectExpression;

/**
 * Represent a body declaration with the keyword 'with' for extension or import statement.
 */
public interface WithClause extends HasKeyword {

  @Override
  default Kind getKind() {
    return Kind.WITH_CLAUSE;
  }

  /**
   * @return the object expression of the with clause
   */
  ObjectExpression object();
}
