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
import org.sonar.iac.common.parser.grammar.Punctuator;
import org.sonar.iac.docker.parser.TreeFactory;
import org.sonar.iac.docker.tree.api.AliasTree;
import org.sonar.iac.docker.tree.api.ArgTree;
import org.sonar.iac.docker.tree.api.CmdTree;
import org.sonar.iac.docker.tree.api.EnvTree;
import org.sonar.iac.docker.tree.api.ExecFormTree;
import org.sonar.iac.docker.tree.api.ExposeTree;
import org.sonar.iac.docker.tree.api.AddTree;
import org.sonar.iac.docker.tree.api.FileTree;
import org.sonar.iac.docker.tree.api.FromTree;
import org.sonar.iac.docker.tree.api.ImageTree;
import org.sonar.iac.docker.tree.api.InstructionTree;
import org.sonar.iac.docker.tree.api.KeyValuePairTree;
import org.sonar.iac.docker.tree.api.LabelTree;
import org.sonar.iac.docker.tree.api.MaintainerTree;
import org.sonar.iac.docker.tree.api.OnBuildTree;
import org.sonar.iac.docker.tree.api.ParamTree;
import org.sonar.iac.docker.tree.api.StopSignalTree;
import org.sonar.iac.docker.tree.api.PortTree;
import org.sonar.iac.docker.tree.api.ShellFormTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;
import org.sonar.iac.docker.tree.api.UserTree;
import org.sonar.iac.docker.tree.api.WorkdirTree;

import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.IMAGE_DIGEST;
import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.IMAGE_NAME;
import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.IMAGE_TAG;
import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.SPACING;
import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.STRING_LITERAL;
import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.STRING_LITERAL_WITH_QUOTES;
import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.STRING_LITERAL_WITHOUT_SPACE;
import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.STRING_LITERAL_NO_COLON;
import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.STRING_UNTIL_EOL;

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
        ONBUILD(),
        FROM(),
        MAINTAINER(),
        STOPSIGNAL(),
        WORKDIR(),
        EXPOSE(),
        LABEL(),
        ENV(),
        ARG(),
        CMD(),
        ADD(),
        USER()
      )
    );
  }

  public OnBuildTree ONBUILD() {
    return b.<OnBuildTree>nonterminal(DockerLexicalGrammar.ONBUILD).is(
      f.onbuild(
        b.token(DockerKeyword.ONBUILD),
        INSTRUCTION()
      )
    );
  }

  public FromTree FROM() {
    return b.<FromTree>nonterminal(DockerLexicalGrammar.FROM).is(
      f.from(
        b.token(DockerKeyword.FROM),
        b.optional(PARAM()),
        IMAGE(),
        b.optional(ALIAS())
      )
    );
  }

  public ParamTree PARAM() {
    return b.<ParamTree>nonterminal(DockerLexicalGrammar.PARAM).is(
      f.param(
        b.token(DockerLexicalGrammar.PARAM_PREFIX),
        b.token(DockerLexicalGrammar.PARAM_NAME),
        b.token(DockerLexicalGrammar.EQUALS_OPERATOR),
        b.token(DockerLexicalGrammar.PARAM_VALUE)
      )
    );
  }

  public ParamTree PARAM_NO_VALUE() {
    return b.<ParamTree>nonterminal(DockerLexicalGrammar.PARAM_NO_VALUE).is(
      f.param(
        b.token(DockerLexicalGrammar.PARAM_PREFIX),
        b.token(DockerLexicalGrammar.PARAM_NAME)
      )
    );
  }

  public ImageTree IMAGE() {
    return b.<ImageTree>nonterminal(DockerLexicalGrammar.IMAGE).is(
      f.image(
        b.token(IMAGE_NAME),
        b.optional(b.token(IMAGE_TAG)),
        b.optional(b.token(IMAGE_DIGEST))
      )
    );
  }

  public AliasTree ALIAS() {
    return b.<AliasTree>nonterminal(DockerLexicalGrammar.ALIAS).is(
      f.alias(
        b.token(DockerKeyword.AS),
        b.token(DockerLexicalGrammar.IMAGE_ALIAS)
      )
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

  public ExposeTree EXPOSE() {
    return b.<ExposeTree>nonterminal(DockerLexicalGrammar.EXPOSE).is(
      f.expose(b.token(DockerKeyword.EXPOSE), b.oneOrMore(PORT()))
    );
  }

  public PortTree PORT() {
    return b.<PortTree>nonterminal(DockerLexicalGrammar.PORT).is(
      b.firstOf(
        f.port(b.token(DockerLexicalGrammar.NUMERIC_LITERAL), b.token(DockerLexicalGrammar.SEPARATOR_PORT), b.optional(b.token(STRING_LITERAL_WITHOUT_SPACE))),
        f.port(b.token(DockerLexicalGrammar.STRING_LITERAL))
      )
    );
  }

  public LabelTree LABEL() {
    return b.<LabelTree>nonterminal(DockerLexicalGrammar.LABEL).is(
      f.label(b.token(DockerKeyword.LABEL),
        b.oneOrMore(
          b.firstOf(KEY_VALUE_PAIR_WITH_EQUALS(), KEY_VALUE_PAIR())
        )
      )
    );
  }

  public EnvTree ENV() {
    return b.<EnvTree>nonterminal(DockerLexicalGrammar.ENV).is(
      f.env(b.token(DockerKeyword.ENV),
        b.oneOrMore(
          b.firstOf(KEY_VALUE_PAIR_WITH_EQUALS(), KEY_VALUE_PAIR())
        )
      )
    );
  }

  public UserTree USER() {
    return b.<UserTree>nonterminal(DockerLexicalGrammar.USER).is(
      b.firstOf(
        f.user(b.token(DockerKeyword.USER), b.token(STRING_LITERAL_NO_COLON), b.token(Punctuator.COLON), b.token(STRING_UNTIL_EOL)),
        f.user(b.token(DockerKeyword.USER), b.token(STRING_UNTIL_EOL))
      )
    );
  }

  /**
   * To match such element as KeyValuePairTree : key
   */
  public KeyValuePairTree KEY_ONLY() {
    return b.<KeyValuePairTree>nonterminal(DockerLexicalGrammar.KEY_ONLY).is(
      f.key(b.token(STRING_LITERAL))
    );
  }

  /**
   * To match such element : key1 value1 value1bis value1tris
   */
  public KeyValuePairTree KEY_VALUE_PAIR() {
    return b.<KeyValuePairTree>nonterminal(DockerLexicalGrammar.KEY_VALUE_PAIR_SINGLE).is(
      f.keyValuePair(b.token(STRING_LITERAL), b.token(STRING_UNTIL_EOL))
    );
  }

  /**
   * To match such element : key1=value1
   */
  public KeyValuePairTree KEY_VALUE_PAIR_WITH_EQUALS() {
    return b.<KeyValuePairTree>nonterminal(DockerLexicalGrammar.KEY_VALUE_PAIR_EQUALS).is(
      f.keyValuePairEquals(b.token(STRING_LITERAL), b.token(Punctuator.EQU), b.token(STRING_LITERAL))
    );
  }

  public ArgTree ARG() {
    return b.<ArgTree>nonterminal(DockerLexicalGrammar.ARG).is(
      f.arg(b.token(DockerKeyword.ARG),
        b.oneOrMore(
          b.firstOf(
            KEY_VALUE_PAIR_WITH_EQUALS(),
            KEY_ONLY()
          )
        )
      )
    );
  }

  public AddTree ADD() {
    return b.<AddTree>nonterminal(DockerLexicalGrammar.ADD).is(
      f.add(
        b.token(DockerKeyword.ADD),
        b.zeroOrMore(
          b.firstOf(
            PARAM(),
            PARAM_NO_VALUE()
          )
        ),
        b.oneOrMore(
          b.token(STRING_LITERAL)
        )
      )
    );
  }

  public CmdTree CMD() {
    return b.<CmdTree>nonterminal(DockerLexicalGrammar.CMD).is(
      f.cmd(
        b.token(DockerKeyword.CMD),
        b.optional(
          b.firstOf(
            EXEC_FORM(),
            SHELL_FORM()
          )
        )
      )
    );
  }

  /**
   * Exec Form is something like this:
   * {@code ["executable","param1","param2"]}
   * what is used by different instructions like CMD, ENTRYPOINT, RUN, SHELL
   */
  public ExecFormTree EXEC_FORM() {
    return b.<ExecFormTree>nonterminal(DockerLexicalGrammar.EXEC_FORM).is(
      f.execForm(
        b.token(Punctuator.LBRACKET),
        b.optional(
          f.tuple(
            f.argument(b.token(STRING_LITERAL_WITH_QUOTES)),
            b.zeroOrMore(
              f.tuple(
                b.token(Punctuator.COMMA),
                f.argument(b.token(STRING_LITERAL_WITH_QUOTES)))
            )
          )
        ),
        b.token(Punctuator.RBRACKET)
      )
    );
  }

  /**
   * Shell Form is a way to define some executable command fo different instructions like CMD, ENTRYPOINT, RUN
   */
  public ShellFormTree SHELL_FORM() {
    return b.<ShellFormTree>nonterminal(DockerLexicalGrammar.SHELL_FORM).is(
      f.shellForm(
        b.oneOrMore(
          b.token(STRING_LITERAL)
        )
      )
    );
  }
}
