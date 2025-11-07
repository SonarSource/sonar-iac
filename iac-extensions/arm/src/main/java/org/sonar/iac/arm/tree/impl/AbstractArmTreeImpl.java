/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
package org.sonar.iac.arm.tree.impl;

import java.util.List;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;

public abstract class AbstractArmTreeImpl implements ArmTree {

  protected TextRange textRange;
  protected ArmTree parent;

  @Override
  public final boolean is(Kind... kind) {
    for (Kind kindIter : kind) {
      if (getKind() == kindIter) {
        return true;
      }
    }
    return false;
  }

  @Override
  public TextRange textRange() {
    if (textRange == null) {
      List<TextRange> childRanges = children().stream().map(HasTextRange::textRange).toList();
      textRange = TextRanges.merge(childRanges);
    }
    return textRange;
  }

  @Override
  public ArmTree parent() {
    return parent;
  }

  @Override
  public void setParent(ArmTree parent) {
    this.parent = parent;
  }
}
