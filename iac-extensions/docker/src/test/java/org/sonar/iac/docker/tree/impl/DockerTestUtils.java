/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
package org.sonar.iac.docker.tree.impl;

import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.docker.parser.DockerParser;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.sslr.grammar.GrammarRuleKey;

public class DockerTestUtils {

  public static <T extends DockerTree> T parse(String input, GrammarRuleKey rootRule) {
    DockerParser parser = DockerParser.create(rootRule);
    DockerTree tree = parser.parse(input);

    return (T) tree;
  }

  public static <T extends DockerTree> T parse(String input, GrammarRuleKey rootRule, InputFileContext inputFileContext) {
    DockerParser parser = DockerParser.create(rootRule);
    Tree tree = parser.parse(input, inputFileContext);

    return (T) tree;
  }

}
