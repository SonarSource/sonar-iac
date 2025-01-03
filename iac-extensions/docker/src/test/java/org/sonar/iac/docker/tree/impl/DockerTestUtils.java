/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
