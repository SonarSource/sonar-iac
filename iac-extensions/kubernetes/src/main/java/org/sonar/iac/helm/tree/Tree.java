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
package org.sonar.iac.helm.tree;

import com.google.protobuf.InvalidProtocolBufferException;
import org.sonar.iac.helm.TreeOrBuilder;

public class Tree {
  private final String name;
  private final String parseName;
  private final int mode;
  private final ListNode root;

  public Tree(String name, String parseName, int mode, ListNode root) {
    this.name = name;
    this.parseName = parseName;
    this.mode = mode;
    this.root = root;
  }

  public static Tree fromPbTree(TreeOrBuilder treePb) throws InvalidProtocolBufferException {
    return new Tree(treePb.getName(), treePb.getParseName(), (int) treePb.getMode(), (ListNode) ListNode.fromPb(treePb.getRoot()));
  }

  public String getName() {
    return name;
  }

  public String getParseName() {
    return parseName;
  }

  public int getMode() {
    return mode;
  }

  public ListNode getRoot() {
    return root;
  }
}
