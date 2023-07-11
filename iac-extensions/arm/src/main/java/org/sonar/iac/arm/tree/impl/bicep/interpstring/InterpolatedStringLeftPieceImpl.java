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

import com.sonar.sslr.api.typed.Optional;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.interpstring.InterpolatedStringLeftPiece;
import org.sonar.iac.arm.tree.impl.json.ArmHelper;
import org.sonar.iac.common.api.tree.Tree;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class InterpolatedStringLeftPieceImpl implements InterpolatedStringLeftPiece {
  private final SyntaxToken leftQuote;
  @Nullable
  private final SyntaxToken text;
  private final SyntaxToken dollarLcurly;

  public InterpolatedStringLeftPieceImpl(SyntaxToken leftQuote, Optional<SyntaxToken> text, SyntaxToken dollarLcurly) {
    this.leftQuote = leftQuote;
    this.text = text.orNull();
    this.dollarLcurly = dollarLcurly;
  }

  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(leftQuote);
    ArmHelper.addChildrenIfPresent(children, text);
    children.add(dollarLcurly);
    return children;
  }
}