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
package org.sonar.iac.common.api.checks;

import java.util.Objects;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.impl.TextRange;

public class SecondaryLocation {

  public final TextRange textRange;

  public final String message;

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
}
