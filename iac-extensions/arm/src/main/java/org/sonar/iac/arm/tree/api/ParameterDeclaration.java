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
package org.sonar.iac.arm.tree.api;

import java.util.List;
import javax.annotation.CheckForNull;
import org.sonar.iac.arm.tree.api.bicep.Declaration;
import org.sonar.iac.common.api.tree.TextTree;

public interface ParameterDeclaration extends Declaration, HasSymbol {

  @CheckForNull
  ParameterType type();

  @CheckForNull
  TextTree resourceType();

  @CheckForNull
  Expression defaultValue();

  List<Expression> allowedValues();

  @CheckForNull
  StringLiteral description();

  @CheckForNull
  NumericLiteral minValue();

  @CheckForNull
  NumericLiteral maxValue();

  @CheckForNull
  NumericLiteral minLength();

  @CheckForNull
  NumericLiteral maxLength();

  default Kind getKind() {
    return Kind.PARAMETER_DECLARATION;
  }
}
