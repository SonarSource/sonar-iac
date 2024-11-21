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
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.bicep.ComposedTypeReference;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.SeparatedList;
import org.sonar.iac.common.api.tree.Tree;

public class ComposedTypeReferenceImpl extends AbstractArmTreeImpl implements ComposedTypeReference {
  private final SeparatedList<Identifier, SyntaxToken> elements;

  public ComposedTypeReferenceImpl(SeparatedList<Identifier, SyntaxToken> elements) {
    this.elements = elements;
  }

  @Override
  public List<Identifier> identifiers() {
    return elements.elements();
  }

  @Override
  public List<SyntaxToken> separators() {
    return elements.separators();
  }

  @Override
  public Kind getKind() {
    return Kind.COMPOSED_TYPE_REFERENCE;
  }

  @Override
  public List<Tree> children() {
    return elements.elementsAndSeparators();
  }
}
