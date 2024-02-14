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

import java.util.Collections;
import java.util.List;
import org.sonar.iac.helm.protobuf.FieldNodeOrBuilder;
import org.sonar.iac.helm.tree.api.FieldNode;
import org.sonar.iac.helm.tree.api.Node;

public class FieldNodeImpl extends AbstractNode implements FieldNode {
  private final List<String> identifiers;

  public FieldNodeImpl(long position, long length, List<String> identifiers) {
    super(position, length);
    this.identifiers = Collections.unmodifiableList(identifiers);
  }

  public static Node fromPb(FieldNodeOrBuilder nodePb) {
    return new FieldNodeImpl(nodePb.getPos(), nodePb.getLength(), nodePb.getIdentList());
  }

  public List<String> identifiers() {
    return identifiers;
  }
}
