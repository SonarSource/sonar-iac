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
package org.sonar.iac.docker.parser.grammar;

import com.sonar.sslr.api.typed.GrammarBuilder;
import java.util.List;
import org.sonar.iac.docker.tree.api.FileTree;
import org.sonar.iac.docker.tree.api.FromTree;
import org.sonar.iac.docker.tree.api.InstructionTree;
import org.sonar.iac.docker.tree.api.MaintainerTree;
import org.sonar.iac.docker.tree.api.StopSignalTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;
import org.sonar.iac.docker.parser.TreeFactory;
import org.sonar.iac.docker.tree.api.WorkdirTree;

import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.SPACING;
import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.STRING_LITERAL;

@SuppressWarnings("java:S100")
public class DockerGrammar {

  private final GrammarBuilder<SyntaxToken> b;
  private final TreeFactory f;

  public DockerGrammar(GrammarBuilder<SyntaxToken> b, TreeFactory f) {
    this.b = b;
    this.f = f;
  }

  public FileTree FILE() {
    return b.<FileTree>nonterminal(DockerLexicalGrammar.FILE).is(
      f.file(
        b.zeroOrMore(INSTRUCTION()),
        b.optional(b.token(SPACING)),
        b.token(DockerLexicalGrammar.EOF))
    );
  }

  public InstructionTree INSTRUCTION() {
    return b.<InstructionTree>nonterminal(DockerLexicalGrammar.INSTRUCTION).is(
      b.firstOf(
        FROM(),
        MAINTAINER(),
        STOPSIGNAL(),
        WORKDIR()
      )
    );
  }

  public FromTree FROM() {
    return b.<FromTree>nonterminal(DockerLexicalGrammar.FROM).is(
      f.from(b.token(DockerKeyword.FROM))
    );
  }

  public MaintainerTree MAINTAINER() {
    return b.<MaintainerTree>nonterminal(DockerLexicalGrammar.MAINTAINER).is(
      f.maintainer(b.token(DockerKeyword.MAINTAINER), ARGUMENTS())
    );
  }

  public List<SyntaxToken> ARGUMENTS() {
    return b.<List<SyntaxToken>>nonterminal(DockerLexicalGrammar.ARGUMENTS).is(
      b.oneOrMore(
        f.argument(b.token(DockerLexicalGrammar.STRING_LITERAL))
      )
    );
  }

  public StopSignalTree STOPSIGNAL() {
    return b.<StopSignalTree>nonterminal(DockerLexicalGrammar.STOPSIGNAL).is(
      f.stopSignal(
        b.token(DockerKeyword.STOPSIGNAL),
        b.token(STRING_LITERAL)
      )
    );
  }

  public WorkdirTree WORKDIR() {
    return b.<WorkdirTree>nonterminal(DockerLexicalGrammar.WORKDIR).is(
      f.workdir(
        b.token(DockerKeyword.WORKDIR),
        ARGUMENTS()
      )
    );
  }
}
