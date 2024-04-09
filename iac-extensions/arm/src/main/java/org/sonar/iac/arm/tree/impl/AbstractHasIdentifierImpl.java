/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.arm.tree.impl;

import java.util.List;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.HasIdentifier;
import org.sonar.iac.arm.tree.impl.bicep.IdentifierImpl;
import org.sonar.iac.arm.tree.impl.bicep.StringLiteralImpl;
import org.sonar.iac.arm.tree.impl.bicep.SyntaxTokenImpl;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;

public abstract class AbstractHasIdentifierImpl extends AbstractArmTreeImpl implements HasIdentifier {
  private final Expression expression;

  protected AbstractHasIdentifierImpl(Expression expression, TextRange textRange) {
    if (expression instanceof StringLiteralImpl stringLiteral) {
      // For JSON; "evaluate" the expression to have an Identifier to align representation with Bicep
      this.expression = new IdentifierImpl(new SyntaxTokenImpl(stringLiteral.value(), stringLiteral.textRange(), List.of()));
    } else {
      this.expression = expression;
    }
    this.textRange = textRange;
  }

  @Override
  public Expression identifier() {
    return expression;
  }

  @Override
  public List<Tree> children() {
    return List.of(expression);
  }
}
