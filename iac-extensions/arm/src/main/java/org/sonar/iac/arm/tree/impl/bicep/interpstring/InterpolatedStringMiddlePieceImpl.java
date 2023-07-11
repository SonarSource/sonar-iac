/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.arm.tree.impl.bicep.interpstring;

import java.util.ArrayList;
import java.util.List;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.interpstring.InterpolatedStringMiddlePiece;
import org.sonar.iac.common.api.tree.Tree;

public class InterpolatedStringMiddlePieceImpl extends AbstractInterpolatedString implements InterpolatedStringMiddlePiece {
  private final Expression expression;
  private final SyntaxToken rCurly;
  private final SyntaxToken dollarLcurly;

  public InterpolatedStringMiddlePieceImpl(Expression expression, SyntaxToken rCurly, SyntaxToken text, SyntaxToken dollarLcurly) {
    super(text);
    this.expression = expression;
    this.rCurly = rCurly;
    this.dollarLcurly = dollarLcurly;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(expression);
    children.add(rCurly);
    children.add(text);
    children.add(dollarLcurly);
    return children;
  }

  @Override
  public Kind getKind() {
    throw new UnsupportedOperationException("No kind for InterpolatedStringMiddlePieceImpl");
  }
}
