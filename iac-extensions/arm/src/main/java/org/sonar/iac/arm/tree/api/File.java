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
package org.sonar.iac.arm.tree.api;

import java.util.List;
import javax.annotation.CheckForNull;

public interface File extends ArmTree, HasSymbolTable {
  Scope targetScope();

  @CheckForNull
  Expression targetScopeLiteral();

  List<Statement> statements();

  enum Scope {
    RESOURCE_GROUP,
    MANAGEMENT_GROUP,
    SUBSCRIPTION,
    TENANT,
    UNKNOWN,
    NOT_SET
  }
}
