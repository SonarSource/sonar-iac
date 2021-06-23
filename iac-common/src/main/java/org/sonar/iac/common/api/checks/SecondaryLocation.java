/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.common.api.checks;

import java.util.Objects;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.common.api.tree.HasTextRange;

public class SecondaryLocation {

  public final TextRange textRange;

  public final String message;

  public SecondaryLocation(HasTextRange tree, String message) {
    this(tree.textRange(), message);
  }

  public SecondaryLocation(TextRange textRange, String message) {
    this.textRange = textRange;
    this.message = message;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SecondaryLocation other = (SecondaryLocation) o;
    return this.textRange.equals(other.textRange) && Objects.equals(this.message, other.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(textRange, message);
  }
}
