/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.terraform.api.tree.TerraformTree;

public abstract class TerraformTreeImpl implements TerraformTree {

  protected TextRange textRange;

  @Override
  public final boolean is(Kind... kind) {
    if (getKind() != null) {
      for (Kind kindIter : kind) {
        if (getKind() == kindIter) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public TextRange textRange() {
    if (textRange == null) {
      List<TextRange> childRanges = children().stream().map(HasTextRange::textRange).collect(Collectors.toList());
      textRange = TextRanges.merge(childRanges);
    }
    return textRange;
  }
}
