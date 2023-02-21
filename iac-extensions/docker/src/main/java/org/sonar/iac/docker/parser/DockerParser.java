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
package org.sonar.iac.docker.parser;

import com.sonar.sslr.api.typed.ActionParser;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.docker.parser.grammar.DockerGrammar;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.sslr.grammar.GrammarRuleKey;

public class DockerParser extends ActionParser<DockerTree> implements TreeParser<Tree> {

  private final DockerPreprocessor preprocessor = new DockerPreprocessor();
  private final DockerNodeBuilder nodeBuilder;

  private DockerParser(DockerNodeBuilder nodeBuilder, GrammarRuleKey rootRule) {
    super(StandardCharsets.UTF_8,
      DockerLexicalGrammar.createGrammarBuilder(),
      DockerGrammar.class,
      new TreeFactory(),
      nodeBuilder,
      rootRule);
    this.nodeBuilder = nodeBuilder;
  }

  public static DockerParser create() {
    return create(DockerLexicalGrammar.FILE);
  }

  public static DockerParser create(GrammarRuleKey rootRule) {
    return new DockerParser(new DockerNodeBuilder(), rootRule);
  }

  @Override
  public DockerTree parse(String source) {
    DockerPreprocessor.PreprocessorResult preprocessorResult = preprocessor.process(source);
    nodeBuilder.setPreprocessorResult(preprocessorResult);
    DockerTree tree = super.parse(preprocessorResult.processedSourceCode());
    setParents(tree);
    return tree;
  }

  @Override
  public Tree parse(String source, @Nullable InputFileContext inputFileContext) {
    return parse(source);
  }

  private static void setParents(DockerTree tree) {
    for (Tree children : tree.children()) {
      DockerTree child = (DockerTree) children;
      child.setParent(tree);
      setParents(child);
    }
  }
}
