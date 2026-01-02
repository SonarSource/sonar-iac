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
package org.sonar.iac.common.extension;

import org.sonar.api.batch.fs.TextPointer;
import org.sonar.iac.common.api.tree.impl.TextRange;

public class BasicTextPointer implements TextPointer {
  private final int line;
  private final int lineOffset;

  public BasicTextPointer(int line, int lineOffset) {
    this.line = line;
    this.lineOffset = lineOffset;
  }

  public BasicTextPointer(TextRange range) {
    // The Yaml Parser returns lineOffset where first position is 1,
    // but TextPointer expect that first position is 0 (zero).
    this(range.start().line(), range.start().lineOffset() - 1);
  }

  public int line() {
    return this.line;
  }

  public int lineOffset() {
    return this.lineOffset;
  }

  public int compareTo(TextPointer o) {
    return this.line == o.line() ? Integer.compare(this.lineOffset, o.lineOffset()) : Integer.compare(this.line, o.line());
  }
}
