/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.common;

import java.util.Collections;
import java.util.List;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.common.api.tree.Tree;

public abstract class AbstractTestTree implements Tree {

  @Override
  public TextRange textRange() {
    return null;
  }

  @Override
  public List<Tree> children() {
    return Collections.emptyList();
  }
}
