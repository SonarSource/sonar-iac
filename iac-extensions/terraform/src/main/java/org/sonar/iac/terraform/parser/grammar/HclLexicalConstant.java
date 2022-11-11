/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.terraform.parser.grammar;

import static org.sonar.iac.common.parser.grammar.LexicalConstant.MULTI_LINE_COMMENT;
import static org.sonar.iac.common.parser.grammar.LexicalConstant.SINGLE_LINE_COMMENT_DOUBLE_SLASH;
import static org.sonar.iac.common.parser.grammar.LexicalConstant.SINGLE_LINE_COMMENT_HASH;

public class HclLexicalConstant {

  private HclLexicalConstant() {
  }

  public static final String COMMENT = "(?:"
    + SINGLE_LINE_COMMENT_DOUBLE_SLASH
    + "|" + SINGLE_LINE_COMMENT_HASH
    + "|" + MULTI_LINE_COMMENT + ")";

}
