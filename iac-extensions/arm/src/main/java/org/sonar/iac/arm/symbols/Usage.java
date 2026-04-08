/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.arm.symbols;

import org.sonar.iac.arm.tree.api.ArmTree;

public class Usage {

  public enum Kind {
    ASSIGNMENT,
    ACCESS
  }

  private final ArmTree tree;
  private final Kind kind;

  public Usage(ArmTree tree, Kind kind) {
    this.tree = tree;
    this.kind = kind;
  }

  public ArmTree tree() {
    return tree;
  }

  public Kind kind() {
    return kind;
  }
}
