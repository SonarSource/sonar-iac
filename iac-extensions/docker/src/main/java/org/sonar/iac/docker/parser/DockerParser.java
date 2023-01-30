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
package org.sonar.iac.docker.parser;

import com.sonar.sslr.api.typed.ActionParser;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.docker.tree.api.Docker;
import org.sonar.iac.docker.parser.grammar.DockerGrammar;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.sslr.grammar.GrammarRuleKey;

public class DockerParser extends ActionParser<Docker> implements TreeParser<Tree> {

  public DockerParser() {
    this(DockerLexicalGrammar.FILE);
  }

  public DockerParser(GrammarRuleKey rootRule) {
    super(StandardCharsets.UTF_8,
      DockerLexicalGrammar.createGrammarBuilder(),
      DockerGrammar.class,
      new TreeFactory(),
      new DockerNodeBuilder(),
      rootRule);
  }

  @Override
  public Docker parse(String source) {
    Docker tree = super.parse(source);
    setParents(tree);
    return tree;
  }

  @Override
  public Tree parse(String source, @Nullable InputFileContext inputFileContext) {
    return parse(source);
  }

  private static void setParents(Docker tree) {
    for (Tree children : tree.children()) {
      Docker child = (Docker) children;
      child.setParent(tree);
      setParents(child);
    }
  }
}
