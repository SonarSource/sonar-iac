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
package org.sonar.iac.arm.tree.api.bicep.importdecl;

import org.sonar.iac.arm.tree.api.bicep.HasKeyword;

/**
 * Compile-time import from clause.
 */
public interface CompileTimeImportFromClause extends HasKeyword {
  @Override
  default Kind getKind() {
    return Kind.COMPILE_TIME_IMPORT_FROM_CLAUSE;
  }
}
