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
import org.sonar.iac.docker.tree.api.AddInstruction;
import org.sonar.iac.docker.tree.api.Alias;
import org.sonar.iac.docker.tree.api.ArgInstruction;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.Body;
import org.sonar.iac.docker.tree.api.CmdInstruction;
import org.sonar.iac.docker.tree.api.CopyInstruction;
import org.sonar.iac.docker.tree.api.DockerImage;
import org.sonar.iac.docker.tree.api.EntrypointInstruction;
import org.sonar.iac.docker.tree.api.EnvInstruction;
import org.sonar.iac.docker.tree.api.ExecForm;
import org.sonar.iac.docker.tree.api.ExpandableStringCharacters;
import org.sonar.iac.docker.tree.api.ExpandableStringLiteral;
import org.sonar.iac.docker.tree.api.ExposeInstruction;
import org.sonar.iac.docker.tree.api.File;
import org.sonar.iac.docker.tree.api.FromInstruction;
import org.sonar.iac.docker.tree.api.HealthCheckInstruction;
import org.sonar.iac.docker.tree.api.HereDocument;
import org.sonar.iac.docker.tree.api.Image;
import org.sonar.iac.docker.tree.api.Instruction;
import org.sonar.iac.docker.tree.api.KeyValuePair;
import org.sonar.iac.docker.tree.api.LabelInstruction;
import org.sonar.iac.docker.tree.api.Literal;
import org.sonar.iac.docker.tree.api.MaintainerInstruction;
import org.sonar.iac.docker.tree.api.NoneInstruction;
import org.sonar.iac.docker.tree.api.OnBuildInstruction;
import org.sonar.iac.docker.tree.api.Param;
import org.sonar.iac.docker.tree.api.Port;
import org.sonar.iac.docker.tree.api.RunInstruction;
import org.sonar.iac.docker.tree.api.ShellForm;
import org.sonar.iac.docker.tree.api.ShellInstruction;
import org.sonar.iac.docker.tree.api.StopSignalInstruction;
import org.sonar.iac.docker.tree.api.SyntaxToken;
import org.sonar.iac.docker.tree.api.UserInstruction;
import org.sonar.iac.docker.tree.api.VolumeInstruction;
import org.sonar.iac.docker.tree.api.WorkdirInstruction;

import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.IMAGE_DIGEST;
import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.IMAGE_NAME;
import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.IMAGE_TAG;
import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.KEY_IN_KEY_VALUE_PAIR_IN_EQUALS_SYNTAX;
import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.STRING_LITERAL;
import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.STRING_LITERAL_WITH_QUOTES;
import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.STRING_UNTIL_EOL;
import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.VALUE_IN_KEY_VALUE_PAIR_IN_EQUALS_SYNTAX;
import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.WHITESPACE;

@SuppressWarnings("java:S100")
public class DockerGrammar {

  private final GrammarBuilder<SyntaxToken> b;
  private final TreeFactory f;

  public DockerGrammar(GrammarBuilder<SyntaxToken> b, TreeFactory f) {
    this.b = b;
    this.f = f;
  }

  public File FILE() {
    return b.<File>nonterminal(DockerLexicalGrammar.FILE).is(
      f.file(
        BODY(),
        b.optional(b.token(DockerLexicalGrammar.SPACING)),
        b.token(DockerLexicalGrammar.EOF))
    );
  }

  public Body BODY() {
    return b.<Body>nonterminal(DockerLexicalGrammar.BODY).is(
      f.body(
        b.zeroOrMore(ARG()),
        b.oneOrMore(DOCKERIMAGE())
      )
    );
  }

  public DockerImage DOCKERIMAGE() {
    return b.<DockerImage>nonterminal(DockerLexicalGrammar.DOCKERIMAGE).is(
      f.dockerImage(
        FROM(),
        b.zeroOrMore(INSTRUCTION())
      )
    );
  }

  public Instruction INSTRUCTION() {
    return b.<Instruction>nonterminal(DockerLexicalGrammar.INSTRUCTION).is(
      f.instruction(
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

  public OnBuildInstruction ONBUILD() {
    return b.<OnBuildInstruction>nonterminal(DockerLexicalGrammar.ONBUILD).is(
      f.onbuild(
        b.token(DockerKeyword.ONBUILD),
        INSTRUCTION()
      )
    );
  }

  public FromInstruction FROM() {
    return b.<FromInstruction>nonterminal(DockerLexicalGrammar.FROM).is(
      f.from(
        b.token(DockerKeyword.FROM),
        b.optional(PARAM()),
        IMAGE(),
        b.optional(ALIAS())
      )
    );
  }

  public Param PARAM() {
    return b.<Param>nonterminal(DockerLexicalGrammar.PARAM).is(
      f.param(
        b.token(DockerLexicalGrammar.PARAM_PREFIX),
        b.token(DockerLexicalGrammar.PARAM_NAME),
        b.token(DockerLexicalGrammar.EQUALS_OPERATOR),
        b.optional(b.token(DockerLexicalGrammar.PARAM_VALUE))
      )
    );
  }

  public Param PARAM_NO_VALUE() {
    return b.<Param>nonterminal(DockerLexicalGrammar.PARAM_NO_VALUE).is(
      f.param(
        b.token(DockerLexicalGrammar.PARAM_PREFIX),
        b.token(DockerLexicalGrammar.PARAM_NAME)
      )
    );
  }

  public Image IMAGE() {
    return b.<Image>nonterminal(DockerLexicalGrammar.IMAGE).is(
      f.image(
        b.token(IMAGE_NAME),
        b.optional(b.token(IMAGE_TAG)),
        b.optional(b.token(IMAGE_DIGEST))
      )
    );
  }

  public Alias ALIAS() {
    return b.<Alias>nonterminal(DockerLexicalGrammar.ALIAS).is(
      f.alias(
        b.token(DockerLexicalGrammar.ALIAS_AS),
        b.token(DockerLexicalGrammar.IMAGE_ALIAS)
      )
    );
  }

  public MaintainerInstruction MAINTAINER() {
    return b.<MaintainerInstruction>nonterminal(DockerLexicalGrammar.MAINTAINER).is(
      f.maintainer(b.token(DockerKeyword.MAINTAINER), ARGUMENTS())
    );
  }

  // TODO get rid of this method or even rename the method and token grammar because it will only remain for MAINTAINER
  public List<SyntaxToken> ARGUMENTS() {
    return b.<List<SyntaxToken>>nonterminal(DockerLexicalGrammar.ARGUMENTS).is(
      b.oneOrMore(
        f.argument(b.token(DockerLexicalGrammar.STRING_LITERAL))
      )
    );
  }

  public StopSignalInstruction STOPSIGNAL() {
    return b.<StopSignalInstruction>nonterminal(DockerLexicalGrammar.STOPSIGNAL).is(
      f.stopSignal(
        b.token(DockerKeyword.STOPSIGNAL),
        b.token(STRING_LITERAL)
      )
    );
  }

  public WorkdirInstruction WORKDIR() {
    return b.<WorkdirInstruction>nonterminal(DockerLexicalGrammar.WORKDIR).is(
      f.workdir(
        b.token(DockerKeyword.WORKDIR),
        ARGUMENTS()
      )
    );
  }

  public ExposeInstruction EXPOSE() {
    return b.<ExposeInstruction>nonterminal(DockerLexicalGrammar.EXPOSE).is(
      f.expose(b.token(DockerKeyword.EXPOSE), b.oneOrMore(PORT()))
    );
  }

  public Port PORT() {
    return b.<Port>nonterminal(DockerLexicalGrammar.PORT).is(
      b.firstOf(
        f.port(
          b.token(DockerLexicalGrammar.EXPOSE_PORT), b.token(DockerLexicalGrammar.EXPOSE_SEPARATOR_PORT), b.token(DockerLexicalGrammar.EXPOSE_PORT_MAX),
          b.token(DockerLexicalGrammar.EXPOSE_SEPARATOR_PROTOCOL), b.token(DockerLexicalGrammar.EXPOSE_PROTOCOL)
        ),
        f.port(
          b.token(DockerLexicalGrammar.EXPOSE_PORT), b.token(DockerLexicalGrammar.EXPOSE_SEPARATOR_PORT), b.token(DockerLexicalGrammar.EXPOSE_PORT_MAX),
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

  public LabelInstruction LABEL() {
    return b.<LabelInstruction>nonterminal(DockerLexicalGrammar.LABEL).is(
      f.label(b.token(DockerKeyword.LABEL),
        b.oneOrMore(
          b.firstOf(KEY_VALUE_PAIR_WITH_EQUALS(), KEY_VALUE_PAIR())
        )
      )
    );
  }

  public EnvInstruction ENV() {
    return b.<EnvInstruction>nonterminal(DockerLexicalGrammar.ENV).is(
      f.env(b.token(DockerKeyword.ENV),
        b.oneOrMore(
          b.firstOf(KEY_VALUE_PAIR_WITH_EQUALS(), KEY_VALUE_PAIR())
        )
      )
    );
  }

  public UserInstruction USER() {
    return b.<UserInstruction>nonterminal(DockerLexicalGrammar.USER).is(
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
  public KeyValuePair KEY_ONLY() {
    return b.<KeyValuePair>nonterminal(DockerLexicalGrammar.KEY_ONLY).is(
      f.key(b.token(STRING_LITERAL))
    );
  }

  /**
   * To match such element : key1 value1 value1bis value1tris
   */
  public KeyValuePair KEY_VALUE_PAIR() {
    return b.<KeyValuePair>nonterminal(DockerLexicalGrammar.KEY_VALUE_PAIR_SINGLE).is(
      f.keyValuePair(b.token(STRING_LITERAL), b.token(STRING_UNTIL_EOL))
    );
  }

  /**
   * To match such element : key1=value1
   */
  public KeyValuePair KEY_VALUE_PAIR_WITH_EQUALS() {
    return b.<KeyValuePair>nonterminal(DockerLexicalGrammar.KEY_VALUE_PAIR_EQUALS).is(
      f.keyValuePairEquals(b.token(KEY_IN_KEY_VALUE_PAIR_IN_EQUALS_SYNTAX), b.token(Punctuator.EQU), b.optional(b.token(VALUE_IN_KEY_VALUE_PAIR_IN_EQUALS_SYNTAX)))
    );
  }

  public ArgInstruction ARG() {
    return b.<ArgInstruction>nonterminal(DockerLexicalGrammar.ARG).is(
      f.arg(
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

  public AddInstruction ADD() {
    return b.<AddInstruction>nonterminal(DockerLexicalGrammar.ADD).is(
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

  public CopyInstruction COPY() {
    return b.<CopyInstruction>nonterminal(DockerLexicalGrammar.COPY).is(
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

  public CmdInstruction CMD() {
    return b.<CmdInstruction>nonterminal(DockerLexicalGrammar.CMD).is(
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

  public EntrypointInstruction ENTRYPOINT() {
    return b.<EntrypointInstruction>nonterminal(DockerLexicalGrammar.ENTRYPOINT).is(
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

  public RunInstruction RUN() {
    return b.<RunInstruction>nonterminal(DockerLexicalGrammar.RUN).is(
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

  public HealthCheckInstruction HEALTHCHECK() {
    return b.<HealthCheckInstruction>nonterminal(DockerLexicalGrammar.HEALTHCHECK).is(
      f.healthcheck(
        b.token(DockerKeyword.HEALTHCHECK),
        b.zeroOrMore(PARAM()),
        b.firstOf(NONE(), CMD())
      )
    );
  }

  public ShellInstruction SHELL() {
    return b.<ShellInstruction>nonterminal(DockerLexicalGrammar.SHELL).is(
      f.shell(
        b.token(DockerKeyword.SHELL),
        EXEC_FORM()
      )
    );
  }

  public NoneInstruction NONE() {
    return b.<NoneInstruction>nonterminal(DockerLexicalGrammar.NONE).is(
      f.none(b.token(DockerLexicalGrammar.HEALTHCHECK_NONE))
    );
  }

  /**
   * Exec Form is something like this:
   * {@code ["executable","param1","param2"]}
   * what is used by different instructions like CMD, ENTRYPOINT, RUN, SHELL
   */
  public ExecForm EXEC_FORM() {
    return b.<ExecForm>nonterminal(DockerLexicalGrammar.EXEC_FORM).is(
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
  public ShellForm SHELL_FORM() {
    return b.<ShellForm>nonterminal(DockerLexicalGrammar.SHELL_FORM).is(
      f.shellForm(
        b.oneOrMore(
          b.token(STRING_LITERAL)
        )
      )
    );
  }

  public HereDocument HEREDOC_FORM() {
    return b.<HereDocument>nonterminal(DockerLexicalGrammar.HEREDOC_FORM).is(
      f.hereDocument(
        b.token(DockerLexicalGrammar.HEREDOC_EXPRESSION)
      )
    );
  }

  public VolumeInstruction VOLUME() {
    return b.<VolumeInstruction>nonterminal(DockerLexicalGrammar.VOLUME).is(
      f.volume(
        b.token(DockerKeyword.VOLUME),
        b.firstOf(
          EXEC_FORM(),
          SHELL_FORM()
        )
      )
    );
  }

  public Argument ARGUMENT() {
    return b.<Argument>nonterminal(DockerLexicalGrammar.ARGUMENT).is(
      b.firstOf(
        STRING_LITERAL(),
        VARIABLE()
      )
    );
  }

  public Argument STRING_LITERAL() {
    return b.<Argument>nonterminal().is(
      b.firstOf(
        REGULAR_STRING_LITERAL(),
        EXPANDABLE_STRING_LITERAL()
      )
    );
  }

  public Literal REGULAR_STRING_LITERAL() {
    return b.<Literal>nonterminal(DockerLexicalGrammar.REGULAR_STRING_LITERAL).is(
      b.firstOf(
        f.regularStringLiteral(b.token(DockerLexicalGrammar.QUOTED_STRING_LITERAL)),
        f.regularStringLiteral(b.token(DockerLexicalGrammar.UNQUOTED_STRING_LITERAL))
      )
    );
  }

  public ExpandableStringLiteral EXPANDABLE_STRING_LITERAL() {
    return b.<ExpandableStringLiteral>nonterminal(DockerLexicalGrammar.EXPANDABLE_STRING_LITERAL).is(
      f.expandableStringLiteral(
        b.optional(b.token(WHITESPACE)),
        b.token(Punctuator.DOUBLE_QUOTE),
        b.oneOrMore(
          b.firstOf(
            EXPANDABLE_STRING_CHARACTERS(),
            VARIABLE())),
        b.token(Punctuator.DOUBLE_QUOTE)));
  }

  public Argument EXPANDABLE_STRING_CHARACTERS() {
    return b.<ExpandableStringCharacters>nonterminal().is(
      f.expandableStringCharacters(b.token(DockerLexicalGrammar.STRING_WITH_ENCAPS_VAR_CHARACTERS)));
  }

  public Argument VARIABLE() {
    return b.<Argument>nonterminal().is(
      b.firstOf(
        REGULAR_VARIABLE(),
        ENCAPS_VARIABLE()
      )
    );
  }

  public Argument REGULAR_VARIABLE() {
    return b.<Argument>nonterminal(DockerLexicalGrammar.REGULAR_VARIABLE).is(
      f.regularVariable(
        b.token(Punctuator.DOLLAR),
        b.token(DockerLexicalGrammar.REGULAR_VAR_IDENTIFIER)
      )
    );
  }

  public Argument ENCAPS_VARIABLE() {
    return b.<Argument>nonterminal(DockerLexicalGrammar.ENCAPSULATED_VARIABLE).is(
      f.encapsulatedVariable(
        b.token(Punctuator.DOLLAR_LCURLY),
        b.token(DockerLexicalGrammar.REGULAR_VAR_IDENTIFIER),
        b.optional(
          f.tuple(
            b.token(DockerLexicalGrammar.ENCAPS_VAR_MODIFIER_SEPARATOR),
            ARGUMENT()
          )
        ),
        b.token(Punctuator.RCURLYBRACE)
      )
    );
  }
}
