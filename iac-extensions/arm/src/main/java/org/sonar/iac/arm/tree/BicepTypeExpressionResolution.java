/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
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
