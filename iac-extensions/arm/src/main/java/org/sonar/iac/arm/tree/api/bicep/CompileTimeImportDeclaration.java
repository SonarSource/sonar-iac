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
package org.sonar.iac.arm.tree.api.bicep;

import org.sonar.iac.arm.tree.api.Statement;
import org.sonar.iac.arm.tree.api.bicep.importdecl.CompileTimeImportFromClause;
import org.sonar.iac.arm.tree.api.bicep.importdecl.CompileTimeImportTarget;

/**
 * Compile-time import declaration.
 */
public interface CompileTimeImportDeclaration extends Statement, HasDecorators, HasKeyword {
  @Override
  default Kind getKind() {
    return Kind.COMPILE_TIME_IMPORT_DECLARATION;
  }

  /**
   * Import target.
   * @return import target
   */
  CompileTimeImportTarget target();

  /**
   * Import from clause.
   * @return import from clause
   */
  CompileTimeImportFromClause fromClause();

}
