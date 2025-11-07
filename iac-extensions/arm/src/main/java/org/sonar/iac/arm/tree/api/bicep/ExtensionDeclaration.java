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
package org.sonar.iac.arm.tree.api.bicep;

import javax.annotation.CheckForNull;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Statement;

/**
 * Represent an extension declaration as mentioned in the bicep documentation, in
 * <a href="https://github.com/Azure/bicep/blob/main/docs/experimental-features.md">experimental features</a>.
 */
public interface ExtensionDeclaration extends Statement, HasDecorators, HasKeyword {
  @Override
  default Kind getKind() {
    return Kind.EXTENSION_DECLARATION;
  }

  /**
   * @return the specification of the extension
   */
  Expression specification();

  /**
   * @return the with clause of the extension declaration, or null if not present
   */
  @CheckForNull
  WithClause withClause();

  /**
   * @return the as clause of the extension declaration, or null if not present
   */
  @CheckForNull
  AsClause asClause();
}
