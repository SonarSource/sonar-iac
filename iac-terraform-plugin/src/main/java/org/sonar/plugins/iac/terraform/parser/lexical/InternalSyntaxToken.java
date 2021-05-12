/*
 * SonarQube IaC Terraform Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.plugins.iac.terraform.parser.lexical;

import org.sonar.plugins.iac.terraform.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.iac.terraform.tree.impl.TerraformTree;

public class InternalSyntaxToken extends TerraformTree implements SyntaxToken {
  private String value;
  private int line;
  private int column;
  private int endLine;
  private int endColumn;

  public InternalSyntaxToken(int line, int column, String value, int startIndex, boolean isEOF) {
    this.value = value;
    this.line = line;
    this.column = column;
    calculateEndOffsets();
  }

  private void calculateEndOffsets() {
    String[] lines = value.split("\r\n|\n|\r", -1);
    endColumn = column + value.length();
    endLine = line + lines.length - 1;

    if (endLine != line) {
      endColumn = lines[lines.length - 1].length();
    }
  }

  @Override
  public String text() {
    return value;
  }

  @Override
  public int line() {
    return line;
  }

  @Override
  public int column() {
    return column;
  }

  @Override
  public int endLine() {
    return endLine;
  }

  @Override
  public int endColumn() {
    return endColumn;
  }
}
