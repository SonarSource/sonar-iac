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
package org.sonar.plugins.iac.terraform.parser;

import com.sonar.sslr.api.typed.Optional;
import org.sonar.plugins.iac.terraform.api.tree.BodyTree;
import org.sonar.plugins.iac.terraform.api.tree.OneLineBlockTree;
import org.sonar.plugins.iac.terraform.parser.lexical.InternalSyntaxToken;
import org.sonar.plugins.iac.terraform.tree.impl.BodyTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.OneLineBlockTreeImpl;

import java.util.List;

public class TreeFactory {
  public BodyTree body(Optional<List<OneLineBlockTree>> zeroOrMoree, Optional<InternalSyntaxToken> optional, InternalSyntaxToken token) {
    return new BodyTreeImpl();
  }

  public OneLineBlockTree oneLineBlock(InternalSyntaxToken token, InternalSyntaxToken token1, InternalSyntaxToken token2) {
    return new OneLineBlockTreeImpl();
  }

}
