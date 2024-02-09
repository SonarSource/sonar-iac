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
package org.sonar.iac.helm.tree.impl;

import javax.annotation.Nullable;
import org.sonar.iac.helm.protobuf.IdentifierNodeOrBuilder;
import org.sonar.iac.helm.tree.api.IdentifierNode;
import org.sonar.iac.helm.tree.api.Node;

public class IdentifierNodeImpl extends AbstractNode implements IdentifierNode {
  @Nullable
  private final String identifier;

  public IdentifierNodeImpl(long position, long length, String identifier) {
    super(position, length);
    this.identifier = identifier;
  }

  public static Node fromPb(IdentifierNodeOrBuilder identifierNodePb) {
    return new IdentifierNodeImpl(identifierNodePb.getPos(), identifierNodePb.getLength(), identifierNodePb.getIdent());
  }

  public String identifier() {
    return identifier;
  }

}
