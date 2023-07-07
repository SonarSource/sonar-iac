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
package org.sonar.iac.arm.tree.impl.bicep;

import com.sonar.sslr.api.typed.Optional;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InterpolatedStringImpl extends AbstractArmTreeImpl implements InterpolatedString {
  private final SyntaxToken leftQuote;
  @Nullable private final SyntaxToken stringLeftPiece;
  private final SyntaxToken leftDollarLcurly;
  private final List<InterpolatedStringMiddlePiece> stringMiddlePiece;
  private final Expression expression;
  private final SyntaxToken rightRcurly;
  @Nullable private final SyntaxToken stringRightPiece;
  private final SyntaxToken rightQuote;

  public InterpolatedStringImpl(SyntaxToken leftQuote,
                                Optional<SyntaxToken> stringLeftPiece,
                                SyntaxToken leftDollarLcurly,
                                Optional<List<InterpolatedStringMiddlePiece>> stringMiddlePiece,
                                Expression expression,
                                SyntaxToken rightRcurly,
                                Optional<SyntaxToken> stringRightPiece,
                                SyntaxToken rightQuote) {
    this.leftQuote = leftQuote;
    this.stringLeftPiece = stringLeftPiece.orNull();
    this.leftDollarLcurly = leftDollarLcurly;
    this.stringMiddlePiece = stringMiddlePiece.or(List.of());
    this.expression = expression;
    this.rightRcurly = rightRcurly;
    this.stringRightPiece = stringRightPiece.orNull();
    this.rightQuote = rightQuote;
  }

  @Override
  public List<Tree> children() {
    List<Tree> result = new ArrayList<>();
    result.add(leftQuote);
    if (stringLeftPiece != null) {
      result.add(stringLeftPiece);
    }
    result.add(leftDollarLcurly);
    result.addAll(stringMiddlePiece.stream().flatMap(it -> it.children().stream()).collect(Collectors.toList()));
    result.add(expression);
    result.add(rightRcurly);
    if (stringRightPiece != null) {
      result.add(stringRightPiece);
    }
    result.add(rightQuote);
    return result;
  }

  @Override
  public Kind getKind() {
    return Kind.INTERPOLATED_STRING;
  }
}
