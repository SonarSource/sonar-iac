/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.common.api.checks;

import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.common.api.tree.HasTextRange;

public interface CheckContext {

  void reportIssue(TextRange textRange, String message);

  void reportIssue(HasTextRange toHighlight, String message);
}
