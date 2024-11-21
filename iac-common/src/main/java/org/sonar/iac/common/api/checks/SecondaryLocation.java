/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.api.checks;

import java.util.Objects;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.impl.TextRange;

public class SecondaryLocation {

  public final TextRange textRange;

  public final String message;

  /**
   * This <code>filePath</code> doesn't have to be strictly absolute or relative.
   * In order for secondary location to be raised on the file the path is describing, it has to satisfy 
   * <code>org.sonar.api.batch.fs.FilePredicate#is(new File(filePath))</code>.
   * If the <code>filePath</code> is <code>null</code>,
   * the secondary location will be raised on the same file as the primary location of the issue.
   */
  @Nullable
  public final String filePath;

  public SecondaryLocation(HasTextRange tree, String message) {
    this(tree, message, null);
  }

  public SecondaryLocation(HasTextRange tree, String message, @Nullable String filePath) {
    this(tree.textRange(), message, filePath);
  }

  public SecondaryLocation(TextRange textRange, String message) {
    this(textRange, message, null);
  }

  public SecondaryLocation(TextRange textRange, String message, @Nullable String filePath) {
    this.textRange = textRange;
    this.message = message;
    this.filePath = filePath;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SecondaryLocation other = (SecondaryLocation) o;
    return this.textRange.equals(other.textRange) && Objects.equals(message, other.message) && Objects.equals(filePath, other.filePath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(textRange, message, filePath);
  }

  @Override
  public String toString() {
    return "SecondaryLocation{" +
      "textRange=" + textRange +
      ", message='" + message + '\'' +
      ", filePath='" + filePath + '\'' +
      '}';
  }
}
