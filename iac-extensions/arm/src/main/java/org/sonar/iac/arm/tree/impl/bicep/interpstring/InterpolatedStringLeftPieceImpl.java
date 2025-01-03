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
package org.sonar.iac.arm.tree.impl.bicep.interpstring;

import java.util.ArrayList;
import java.util.List;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.interpstring.InterpolatedStringLeftPiece;
import org.sonar.iac.common.api.tree.Tree;

public class InterpolatedStringLeftPieceImpl extends AbstractInterpolatedString implements InterpolatedStringLeftPiece {
  private final SyntaxToken leftQuote;
  private final SyntaxToken dollarLcurly;

  public InterpolatedStringLeftPieceImpl(SyntaxToken leftQuote, SyntaxToken text, SyntaxToken dollarLcurly) {
    super(text);
    this.leftQuote = leftQuote;
    this.dollarLcurly = dollarLcurly;
  }

  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(leftQuote);
    children.add(text);
    children.add(dollarLcurly);
    return children;
  }

  @Override
  public Kind getKind() {
    throw new UnsupportedOperationException("No kind for InterpolatedStringLeftPieceImpl");
  }
}
