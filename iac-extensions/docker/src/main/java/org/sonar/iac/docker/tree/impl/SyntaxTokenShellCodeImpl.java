/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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
package org.sonar.iac.docker.tree.impl;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.docker.tree.api.SyntaxToken;
import org.sonar.iac.docker.tree.api.SyntaxTokenShellCode;

public class SyntaxTokenShellCodeImpl extends AbstractShellCodeImpl<SyntaxToken> implements SyntaxTokenShellCode {
  private final String originalSourceCode;

  public SyntaxTokenShellCodeImpl(SyntaxToken code, @Nullable String originalSourceCode) {
    super(code);
    this.originalSourceCode = originalSourceCode;
  }

  @Override
  @CheckForNull
  public String originalSourceCode() {
    return originalSourceCode;
  }
}
