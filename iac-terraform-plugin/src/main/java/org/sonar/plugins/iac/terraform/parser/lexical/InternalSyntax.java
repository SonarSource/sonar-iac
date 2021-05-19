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

import org.sonar.plugins.iac.terraform.api.tree.lexical.Syntax;
import org.sonar.plugins.iac.terraform.tree.impl.TerraformTree;

public abstract class InternalSyntax extends TerraformTree implements Syntax {

  private final String value;
  private final int startColumn;
  private final int startLine;
  private int endLine;
  private int endColumn;

  protected InternalSyntax(String value, int startLine, int startColumn) {
    this.value = value;
    this.startLine = startLine;
    this.startColumn = startColumn;
    calculateEndOffsets();
  }

  private void calculateEndOffsets() {
    String[] lines = value.split("\r\n|\n|\r", -1);
    endColumn = startColumn + value.length();
    endLine = startLine + lines.length - 1;

    if (endLine != startLine) {
      endColumn = lines[lines.length - 1].length();
    }
  }

  @Override
  public String value() {
    return value;
  }

  @Override
  public int line() {
    return startLine;
  }

  @Override
  public int column() {
    return startColumn;
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
