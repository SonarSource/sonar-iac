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
package org.sonar.iac.arm.tree.impl.bicep;

import java.util.List;
import org.sonar.iac.arm.tree.api.NumericLiteral;
import org.sonar.iac.arm.tree.api.bicep.HasToken;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class NumericLiteralImpl extends AbstractArmTreeImpl implements NumericLiteral, HasToken {

  private final SyntaxToken token;

  public NumericLiteralImpl(SyntaxToken token) {
    this.token = token;
  }

  @Override
  public double asDouble() {
    return Double.parseDouble(token.value());
  }

  @Override
  public List<Tree> children() {
    return List.of(token);
  }

  @Override
  public String value() {
    return token.value();
  }

  @Override
  public SyntaxToken token() {
    return token;
  }

  @Override
  public String toString() {
    return token.toString();
  }
}
