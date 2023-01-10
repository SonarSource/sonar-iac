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
import org.sonar.iac.docker.tree.api.AddTree;
import org.sonar.iac.docker.tree.api.AliasTree;
import org.sonar.iac.docker.tree.api.ArgTree;
import org.sonar.iac.docker.tree.api.CmdTree;
import org.sonar.iac.docker.tree.api.CopyTree;
import org.sonar.iac.docker.tree.api.DockerImageTree;
import org.sonar.iac.docker.tree.api.EntrypointTree;
import org.sonar.iac.docker.tree.api.EnvTree;
import org.sonar.iac.docker.tree.api.ExecFormTree;
import org.sonar.iac.docker.tree.api.ExposeTree;
import org.sonar.iac.docker.tree.api.FileTree;
import org.sonar.iac.docker.tree.api.FromTree;
import org.sonar.iac.docker.tree.api.HealthCheckTree;
import org.sonar.iac.docker.tree.api.HereDocumentTree;
import org.sonar.iac.docker.tree.api.ImageTree;
import org.sonar.iac.docker.tree.api.InstructionTree;
import org.sonar.iac.docker.tree.api.KeyValuePairTree;
import org.sonar.iac.docker.tree.api.LabelTree;
import org.sonar.iac.docker.tree.api.MaintainerTree;
import org.sonar.iac.docker.tree.api.NoneTree;
import org.sonar.iac.docker.tree.api.OnBuildTree;
import org.sonar.iac.docker.tree.api.ParamTree;
import org.sonar.iac.docker.tree.api.PortTree;
import org.sonar.iac.docker.tree.api.RunTree;
import org.sonar.iac.docker.tree.api.ShellFormTree;
import org.sonar.iac.docker.tree.api.ShellTree;
import org.sonar.iac.docker.tree.api.StopSignalTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;
import org.sonar.iac.docker.tree.api.UserTree;
import org.sonar.iac.docker.tree.api.VolumeTree;
import org.sonar.iac.docker.tree.api.WorkdirTree;

import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.IMAGE_DIGEST;
import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.IMAGE_NAME;
import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.IMAGE_TAG;
import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.STRING_LITERAL;
import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.KEY_IN_KEY_VALUE_PAIR_IN_EQUALS_SYNTAX;
import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.STRING_LITERAL_WITH_QUOTES;
import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.STRING_UNTIL_EOL;
import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.VALUE_IN_KEY_VALUE_PAIR_IN_EQUALS_SYNTAX;

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
        b.zeroOrMore(ARG()),
        b.zeroOrMore(DOCKERIMAGE()),
        b.optional(b.token(DockerLexicalGrammar.INSTRUCTION_PREFIX)),
        b.token(DockerLexicalGrammar.EOF))
    );
  }

  public DockerImageTree DOCKERIMAGE() {
    return b.<DockerImageTree>nonterminal(DockerLexicalGrammar.DOCKERIMAGE).is(
      f.dockerImage(
        FROM(),
        b.zeroOrMore(INSTRUCTION())
      )
    );
  }

  public InstructionTree INSTRUCTION() {
    return b.<InstructionTree>nonterminal(DockerLexicalGrammar.INSTRUCTION).is(
      f.instruction(
        b.optional(b.token(DockerLexicalGrammar.INSTRUCTION_PREFIX)),
        b.firstOf(
          ONBUILD(),
          MAINTAINER(),
          STOPSIGNAL(),
          WORKDIR(),
          EXPOSE(),
          LABEL(),
          ENV(),
          ARG(),
          CMD(),
          ENTRYPOINT(),
          RUN(),
          ADD(),
          COPY(),
          USER(),
          VOLUME(),
          SHELL(),
          HEALTHCHECK()
        )
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
        b.optional(b.token(DockerLexicalGrammar.INSTRUCTION_PREFIX)),
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
        b.optional(b.token(DockerLexicalGrammar.PARAM_VALUE))
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
        f.port(
          b.token(DockerLexicalGrammar.EXPOSE_PORT), b.token(DockerLexicalGrammar.EXPOSE_SEPARATOR_PORT), b.token(DockerLexicalGrammar.EXPOSE_PORT),
          b.token(DockerLexicalGrammar.EXPOSE_SEPARATOR_PROTOCOL), b.token(DockerLexicalGrammar.EXPOSE_PROTOCOL)
        ),
        f.port(
          b.token(DockerLexicalGrammar.EXPOSE_PORT), b.token(DockerLexicalGrammar.EXPOSE_SEPARATOR_PORT), b.token(DockerLexicalGrammar.EXPOSE_PORT),
          b.optional(b.token(DockerLexicalGrammar.EXPOSE_SEPARATOR_PROTOCOL))
          ),
        f.port(
          b.token(DockerLexicalGrammar.EXPOSE_PORT),
          b.token(DockerLexicalGrammar.EXPOSE_SEPARATOR_PROTOCOL), b.token(DockerLexicalGrammar.EXPOSE_PROTOCOL)
        ),
        f.port(
          b.token(DockerLexicalGrammar.EXPOSE_PORT),
          b.optional(b.token(DockerLexicalGrammar.EXPOSE_SEPARATOR_PROTOCOL))
        ),
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
      f.user(
        b.token(DockerKeyword.USER),
        b.token(DockerLexicalGrammar.USER_NAME),
        b.optional(
          f.tuple(
            b.token(DockerLexicalGrammar.USER_SEPARATOR),
            b.token(DockerLexicalGrammar.USER_GROUP)
          )
        )
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
      f.keyValuePairEquals(b.token(KEY_IN_KEY_VALUE_PAIR_IN_EQUALS_SYNTAX), b.token(Punctuator.EQU), b.token(VALUE_IN_KEY_VALUE_PAIR_IN_EQUALS_SYNTAX))
    );
  }

  public ArgTree ARG() {
    return b.<ArgTree>nonterminal(DockerLexicalGrammar.ARG).is(
      f.arg(
        b.optional(b.token(DockerLexicalGrammar.INSTRUCTION_PREFIX)),
        b.token(DockerKeyword.ARG),
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
        b.firstOf(
          EXEC_FORM(),
          SHELL_FORM()
        )
      )
    );
  }

  public CopyTree COPY() {
    return b.<CopyTree>nonterminal(DockerLexicalGrammar.COPY).is(
      f.copy(
        b.token(DockerKeyword.COPY),
        b.zeroOrMore(
          b.firstOf(
            PARAM(),
            PARAM_NO_VALUE()
          )
        ),
        b.firstOf(
          HEREDOC_FORM(),
          EXEC_FORM(),
          SHELL_FORM()
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

  public EntrypointTree ENTRYPOINT() {
    return b.<EntrypointTree>nonterminal(DockerLexicalGrammar.ENTRYPOINT).is(
      f.entrypoint(
        b.token(DockerKeyword.ENTRYPOINT),
        b.optional(
          b.firstOf(
            EXEC_FORM(),
            SHELL_FORM()
          )
        )
      )
    );
  }

  public RunTree RUN() {
    return b.<RunTree>nonterminal(DockerLexicalGrammar.RUN).is(
      f.run(
        b.token(DockerKeyword.RUN),
        b.zeroOrMore(
          b.firstOf(
            PARAM(),
            PARAM_NO_VALUE()
          )
        ),
        b.optional(
          b.firstOf(
            HEREDOC_FORM(),
            EXEC_FORM(),
            SHELL_FORM()
          )
        )
      )
    );
  }

  public HealthCheckTree HEALTHCHECK() {
    return b.<HealthCheckTree>nonterminal(DockerLexicalGrammar.HEALTHCHECK).is(
      f.healthcheck(
        b.token(DockerKeyword.HEALTHCHECK),
        b.zeroOrMore(PARAM()),
        b.firstOf(NONE(), CMD())
      )
    );
  }

  public ShellTree SHELL() {
    return b.<ShellTree>nonterminal(DockerLexicalGrammar.SHELL).is(
      f.shell(
        b.token(DockerKeyword.SHELL),
        EXEC_FORM()
      )
    );
  }

  public NoneTree NONE() {
    return b.<NoneTree>nonterminal(DockerLexicalGrammar.NONE).is(
      f.none(b.token(DockerKeyword.NONE))
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

  public HereDocumentTree HEREDOC_FORM() {
    return b.<HereDocumentTree>nonterminal(DockerLexicalGrammar.HEREDOC_FORM).is(
      f.hereDocument(
        b.token(DockerLexicalGrammar.HEREDOC_EXPRESSION)
      )
    );
  }

  public VolumeTree VOLUME() {
    return b.<VolumeTree>nonterminal(DockerLexicalGrammar.VOLUME).is(
      f.volume(
        b.token(DockerKeyword.VOLUME),
        b.firstOf(
          EXEC_FORM(),
          SHELL_FORM()
        )
      )
    );
  }
}
