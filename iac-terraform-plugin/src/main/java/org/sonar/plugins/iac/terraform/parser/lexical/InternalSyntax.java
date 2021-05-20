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

import java.util.Collections;
import java.util.List;
import org.sonar.plugins.iac.terraform.api.tree.TextRange;
import org.sonar.plugins.iac.terraform.api.tree.lexical.Syntax;
import org.sonar.plugins.iac.terraform.tree.impl.TerraformTree;

public abstract class InternalSyntax extends TerraformTree implements Syntax {

  private final String value;

  protected InternalSyntax(String value, TextRange textRange) {
    this.value = value;
    this.textRange = textRange;
  }

  @Override
  public String value() {
    return value;
  }

  @Override
  public List<Tree> children() {
    return Collections.emptyList();
  }

  @Override
  public TextRange textRange() {
    return textRange;
  }
}
