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
package org.sonar.iac.terraform.tree.impl;

import com.sonar.sslr.api.typed.ActionParser;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.parser.HclParser;
import org.sonar.sslr.grammar.GrammarRuleKey;

public abstract class TerraformTreeModelTest {
  protected ActionParser<TerraformTree> p;

  /**
   * Parse the given string and return the first descendant of the given kind.
   *
   * @param s the string to parse
   * @param rootRule the rule to start parsing from
   * @return the node found for the given kind, null if not found.
   */
  protected <T extends TerraformTree> T parse(String s, GrammarRuleKey rootRule) {
    p = new HclParser(rootRule);
    TerraformTree node = p.parse(s);

    //TODO: SONARIAC-91 similar to PHP parser, check how far we have parsed. Missing possibility to represent trees as string for now
    //checkFullFidelity(node, s.trim());

    return (T) node;
  }
}
