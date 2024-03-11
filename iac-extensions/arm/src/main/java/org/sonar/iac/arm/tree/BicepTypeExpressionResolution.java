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
package org.sonar.iac.arm.tree;

import org.sonar.iac.arm.tree.api.bicep.TypeExpressionAble;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.api.tree.Tree;

public class BicepTypeExpressionResolution {

  private BicepTypeExpressionResolution() {
    // empty
  }

  public static String resolve(TypeExpressionAble tree) {
    return resolve(tree, 20);
  }

  static String resolve(Tree tree, int maxDepth) {
    if (maxDepth == 0) {
      return "";
    }
    StringBuilder result = new StringBuilder();
    tree.children().forEach(t -> {
      if (t instanceof TextTree textTree) {
        result.append(textTree.value());
      } else {
        result.append(resolve(t, maxDepth - 1));
      }
    });
    return result.toString();
  }
}
