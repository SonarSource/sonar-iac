/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
import org.sonar.iac.docker.tree.api.Expression;
import org.sonar.iac.docker.tree.api.File;
import org.sonar.iac.docker.tree.api.Flag;
import org.sonar.iac.docker.tree.api.FromInstruction;
import org.sonar.iac.docker.tree.api.HealthCheckInstruction;
import org.sonar.iac.docker.tree.api.HereDocument;
import org.sonar.iac.docker.tree.api.Instruction;
import org.sonar.iac.docker.tree.api.KeyValuePair;
import org.sonar.iac.docker.tree.api.LabelInstruction;
import org.sonar.iac.docker.tree.api.Literal;
import org.sonar.iac.docker.tree.api.MaintainerInstruction;
import org.sonar.iac.docker.tree.api.OnBuildInstruction;
import org.sonar.iac.docker.tree.api.RunInstruction;
import org.sonar.iac.docker.tree.api.ShellForm;
import org.sonar.iac.docker.tree.api.ShellInstruction;
import org.sonar.iac.docker.tree.api.StopSignalInstruction;
import org.sonar.iac.docker.tree.api.SyntaxToken;
import org.sonar.iac.docker.tree.api.UserInstruction;
import org.sonar.iac.docker.tree.api.VolumeInstruction;
import org.sonar.iac.docker.tree.api.WorkdirInstruction;

// Ignore uppercase method names warning
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
        b.optional(BODY()),
        b.optional(b.token(DockerLexicalGrammar.SPACING)),
        b.token(DockerLexicalGrammar.EOF)));
  }

  public Body BODY() {
    return b.<Body>nonterminal(DockerLexicalGrammar.BODY).is(
      f.body(
        b.zeroOrMore(ARG()),
        b.oneOrMore(DOCKERIMAGE())));
  }

  public DockerImage DOCKERIMAGE() {
    return b.<DockerImage>nonterminal(DockerLexicalGrammar.DOCKERIMAGE).is(
      f.dockerImage(
        FROM(),
        b.zeroOrMore(INSTRUCTION())));
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
          HEALTHCHECK())));
  }

  public OnBuildInstruction ONBUILD() {
    return b.<OnBuildInstruction>nonterminal(DockerLexicalGrammar.ONBUILD).is(
      f.onbuild(
        b.token(DockerKeyword.ONBUILD),
        INSTRUCTION()));
  }

  public FromInstruction FROM() {
    return b.<FromInstruction>nonterminal(DockerLexicalGrammar.FROM).is(
      f.from(
        b.token(DockerKeyword.FROM),
        b.optional(FLAG()),
        f.ignoreFirst(b.token(DockerLexicalGrammar.WHITESPACE), ARGUMENT()),
        b.optional(ALIAS())));
  }

  public Flag FLAG() {
    return b.<Flag>nonterminal(DockerLexicalGrammar.FLAG).is(
      f.flag(
        b.token(DockerLexicalGrammar.FLAG_PREFIX),
        b.token(DockerLexicalGrammar.FLAG_NAME),
        b.optional(b.token(DockerLexicalGrammar.EQUALS_OPERATOR)),
        b.optional(ARGUMENT())));
  }

  public Alias ALIAS() {
    return b.<Alias>nonterminal(DockerLexicalGrammar.ALIAS).is(
      f.alias(
        b.token(DockerLexicalGrammar.ALIAS_AS),
        b.token(DockerLexicalGrammar.IMAGE_ALIAS)));
  }

  public MaintainerInstruction MAINTAINER() {
    return b.<MaintainerInstruction>nonterminal(DockerLexicalGrammar.MAINTAINER).is(
      f.maintainer(b.token(DockerKeyword.MAINTAINER), ARGUMENTS()));
  }

  // TODO SONARIAC-1479: get rid of this method or even rename the method and token grammar because it will only remain for MAINTAINER
  public List<SyntaxToken> ARGUMENTS() {
    return b.<List<SyntaxToken>>nonterminal(DockerLexicalGrammar.ARGUMENTS).is(
      b.oneOrMore(
        f.argument(b.token(DockerLexicalGrammar.STRING_LITERAL))));
  }

  public StopSignalInstruction STOPSIGNAL() {
    return b.<StopSignalInstruction>nonterminal(DockerLexicalGrammar.STOPSIGNAL).is(
      f.stopSignal(
        b.token(DockerKeyword.STOPSIGNAL),
        b.token(DockerLexicalGrammar.WHITESPACE),
        ARGUMENT()));
  }

  public WorkdirInstruction WORKDIR() {
    return b.<WorkdirInstruction>nonterminal(DockerLexicalGrammar.WORKDIR).is(
      f.workdir(
        b.token(DockerKeyword.WORKDIR),
        b.oneOrMore(f.ignoreFirst(
          b.token(DockerLexicalGrammar.WHITESPACE),
          ARGUMENT()))));
  }

  public ExposeInstruction EXPOSE() {
    return b.<ExposeInstruction>nonterminal(DockerLexicalGrammar.EXPOSE).is(
      f.expose(
        b.token(DockerKeyword.EXPOSE),
        b.oneOrMore(f.ignoreFirst(
          b.token(DockerLexicalGrammar.WHITESPACE),
          ARGUMENT()))));
  }

  public LabelInstruction LABEL() {
    return b.<LabelInstruction>nonterminal(DockerLexicalGrammar.LABEL).is(
      b.firstOf(
        f.label(
          b.token(DockerKeyword.LABEL),
          b.oneOrMore(
            f.ignoreFirst(
              b.token(DockerLexicalGrammar.WHITESPACE),
              KEY_VALUE_PAIR_WITH_EQUAL()))),
        f.label(
          b.token(DockerKeyword.LABEL),
          f.ignoreFirst(
            b.token(DockerLexicalGrammar.WHITESPACE),
            KEY_VALUE_PAIR_WITHOUT_EQUAL()))));
  }

  public EnvInstruction ENV() {
    return b.<EnvInstruction>nonterminal(DockerLexicalGrammar.ENV).is(
      f.env(
        b.token(DockerKeyword.ENV),
        b.oneOrMore(
          f.ignoreFirst(
            b.token(DockerLexicalGrammar.WHITESPACE),
            KEY_VALUE_PAIR()))));
  }

  public UserInstruction USER() {
    return b.<UserInstruction>nonterminal(DockerLexicalGrammar.USER).is(
      f.user(
        b.token(DockerKeyword.USER),
        b.oneOrMore(f.ignoreFirst(
          b.token(DockerLexicalGrammar.WHITESPACE),
          ARGUMENT()))));
  }

  public ArgInstruction ARG() {
    return b.<ArgInstruction>nonterminal(DockerLexicalGrammar.ARG).is(
      f.arg(
        b.token(DockerKeyword.ARG),
        b.oneOrMore(
          f.ignoreFirst(
            b.token(DockerLexicalGrammar.WHITESPACE),
            b.firstOf(
              KEY_VALUE_PAIR_WITH_EQUAL(),
              KEY_VALUE_PAIR_KEY_ONLY())))));
  }

  public AddInstruction ADD() {
    return b.<AddInstruction>nonterminal(DockerLexicalGrammar.ADD).is(
      f.add(
        b.token(DockerKeyword.ADD),
        b.zeroOrMore(
          FLAG()),
        b.firstOf(
          EXEC_FORM(),
          SHELL_FORM())));
  }

  public CopyInstruction COPY() {
    return b.<CopyInstruction>nonterminal(DockerLexicalGrammar.COPY).is(
      f.copy(
        b.token(DockerKeyword.COPY),
        b.zeroOrMore(
          FLAG()),
        b.firstOf(
          HEREDOC(),
          EXEC_FORM(),
          SHELL_FORM())));
  }

  public CmdInstruction CMD() {
    return b.<CmdInstruction>nonterminal(DockerLexicalGrammar.CMD).is(
      f.cmd(
        b.token(DockerKeyword.CMD),
        b.optional(
          b.firstOf(
            EXEC_FORM(),
            SHELL_FORM_GENERIC()))));
  }

  public EntrypointInstruction ENTRYPOINT() {
    return b.<EntrypointInstruction>nonterminal(DockerLexicalGrammar.ENTRYPOINT).is(
      f.entrypoint(
        b.token(DockerKeyword.ENTRYPOINT),
        b.optional(
          b.firstOf(
            EXEC_FORM(),
            SHELL_FORM_GENERIC()))));
  }

  public RunInstruction RUN() {
    return b.<RunInstruction>nonterminal(DockerLexicalGrammar.RUN).is(
      f.run(
        b.token(DockerKeyword.RUN),
        b.zeroOrMore(
          FLAG()),
        b.optional(
          b.firstOf(
            HEREDOC(),
            EXEC_FORM(),
            SHELL_FORM_GENERIC()))));
  }

  public HealthCheckInstruction HEALTHCHECK() {
    return b.<HealthCheckInstruction>nonterminal(DockerLexicalGrammar.HEALTHCHECK).is(
      f.healthcheck(
        b.token(DockerKeyword.HEALTHCHECK),
        b.zeroOrMore(FLAG()),
        b.firstOf(
          b.token(DockerLexicalGrammar.HEALTHCHECK_NONE),
          CMD())));
  }

  public ShellInstruction SHELL() {
    return b.<ShellInstruction>nonterminal(DockerLexicalGrammar.SHELL).is(
      f.shell(
        b.token(DockerKeyword.SHELL),
        EXEC_FORM()));
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
          f.ignoreFirst(b.optional(b.token(DockerLexicalGrammar.WHITESPACE)),
            f.singleExpressionArgument(EXPANDABLE_STRING_LITERAL()))),
        b.zeroOrMore(
          f.tuple(
            b.token(Punctuator.COMMA),
            f.ignoreFirst(b.optional(b.token(DockerLexicalGrammar.WHITESPACE)),
              f.singleExpressionArgument(EXPANDABLE_STRING_LITERAL())))),
        b.token(Punctuator.RBRACKET),
        b.optional(b.token(DockerLexicalGrammar.EXEC_LEFTOVER))));
  }

  /**
   * Shell Form is a way to define some executable command fo different instructions like CMD, ENTRYPOINT, RUN
   */
  public ShellForm SHELL_FORM() {
    return b.<ShellForm>nonterminal(DockerLexicalGrammar.SHELL_FORM).is(
      f.shellForm(
        b.oneOrMore(
          b.firstOf(
            f.hereDocument(b.token(DockerLexicalGrammar.HEREDOC_EXPRESSION)),
            f.singleArgumentList(f.ignoreFirst(b.token(DockerLexicalGrammar.WHITESPACE), ARGUMENT()))))));
  }

  /**
   * Generic version of Shell Form, which should be used to parse non-docker-only syntax for shell content.
   */
  public ShellForm SHELL_FORM_GENERIC() {
    return b.<ShellForm>nonterminal(DockerLexicalGrammar.SHELL_FORM_GENERIC).is(
      f.shellForm(
        b.oneOrMore(
          b.firstOf(
            f.hereDocument(b.token(DockerLexicalGrammar.HEREDOC_EXPRESSION)),
            f.singleArgumentList(f.ignoreFirst(b.token(DockerLexicalGrammar.WHITESPACE), ARGUMENT_GENERIC()))))));
  }

  public HereDocument HEREDOC() {
    return b.<HereDocument>nonterminal(DockerLexicalGrammar.HEREDOC).is(
      f.hereDocument(
        b.token(DockerLexicalGrammar.HEREDOC_EXPRESSION)));
  }

  // This method is used to enable an entrypoint for the Heredoc parser
  @SuppressWarnings("unused")
  public HereDocument HEREDOC_CONTENT() {
    return b.<HereDocument>nonterminal(DockerLexicalGrammar.HEREDOC_CONTENT).is(
      f.hereDocumentContent(
        HEREDOC_ELEMENT(),
        b.zeroOrMore(
          f.ignoreFirst(b.token(DockerLexicalGrammar.WHITESPACE_OR_LINE_BREAK), HEREDOC_ELEMENT()))));
  }

  public Argument HEREDOC_ELEMENT() {
    return b.<Argument>nonterminal().is(
      b.firstOf(
        HEREDOC_NAME(),
        ARGUMENT()));
  }

  public Argument HEREDOC_NAME() {
    return b.<Argument>nonterminal().is(
      f.asArgument(
        f.regularStringLiteral(b.token(DockerLexicalGrammar.HEREDOC_NAME))));
  }

  public VolumeInstruction VOLUME() {
    return b.<VolumeInstruction>nonterminal(DockerLexicalGrammar.VOLUME).is(
      f.volume(
        b.token(DockerKeyword.VOLUME),
        b.firstOf(
          EXEC_FORM(),
          SHELL_FORM())));
  }

  public Argument ARGUMENT() {
    return b.<Argument>nonterminal(DockerLexicalGrammar.ARGUMENT).is(
      f.newArgument(
        b.oneOrMore(
          b.firstOf(
            STRING_LITERAL(),
            VARIABLE()))));
  }

  public Argument ARGUMENT_GENERIC() {
    return b.<Argument>nonterminal(DockerLexicalGrammar.ARGUMENT_GENERIC).is(
      f.newArgument(
        b.oneOrMore(
          b.firstOf(
            STRING_LITERAL_GENERIC(),
            VARIABLE_GENERIC()))));
  }

  public Argument KEY_ARGUMENT() {
    return b.<Argument>nonterminal().is(
      f.newArgument(
        b.oneOrMore(
          b.firstOf(
            f.regularStringLiteral(b.token(DockerLexicalGrammar.QUOTED_STRING_LITERAL)),
            f.regularStringLiteral(b.token(DockerLexicalGrammar.UNQUOTED_KEY_LITERAL)),
            EXPANDABLE_STRING_LITERAL(),
            VARIABLE()))));
  }

  public Expression STRING_LITERAL() {
    return b.<Expression>nonterminal().is(
      b.firstOf(
        REGULAR_STRING_LITERAL(),
        EXPANDABLE_STRING_LITERAL()));
  }

  public Expression STRING_LITERAL_GENERIC() {
    return b.<Expression>nonterminal().is(
      b.firstOf(
        REGULAR_STRING_LITERAL(),
        EXPANDABLE_STRING_LITERAL_GENERIC()));
  }

  public KeyValuePair KEY_VALUE_PAIR() {
    return b.<KeyValuePair>nonterminal(DockerLexicalGrammar.KEY_VALUE_PAIR).is(
      b.firstOf(
        KEY_VALUE_PAIR_WITH_EQUAL(),
        KEY_VALUE_PAIR_WITHOUT_EQUAL(),
        KEY_VALUE_PAIR_KEY_ONLY()));
  }

  public KeyValuePair KEY_VALUE_PAIR_WITH_EQUAL() {
    return b.<KeyValuePair>nonterminal().is(
      f.keyValuePair(
        KEY_ARGUMENT(),
        b.token(DockerLexicalGrammar.EQUALS_OPERATOR),
        ARGUMENT()));
  }

  public KeyValuePair KEY_VALUE_PAIR_WITHOUT_EQUAL() {
    return b.<KeyValuePair>nonterminal().is(
      f.keyValuePair(
        KEY_ARGUMENT(),
        f.ignoreFirst(
          b.token(DockerLexicalGrammar.WHITESPACE),
          ARGUMENT()),
        b.zeroOrMore(
          f.tuple(
            b.token(DockerLexicalGrammar.WHITESPACE),
            ARGUMENT()))));
  }

  public KeyValuePair KEY_VALUE_PAIR_KEY_ONLY() {
    return b.<KeyValuePair>nonterminal().is(
      f.keyValuePair(
        KEY_ARGUMENT(),
        b.optional(b.token(DockerLexicalGrammar.EQUALS_OPERATOR))));
  }

  public Literal REGULAR_STRING_LITERAL() {
    return b.<Literal>nonterminal(DockerLexicalGrammar.REGULAR_STRING_LITERAL).is(
      b.firstOf(
        f.regularStringLiteral(b.token(DockerLexicalGrammar.UNQUOTED_STRING_LITERAL)),
        f.regularStringLiteral(b.token(DockerLexicalGrammar.QUOTED_STRING_LITERAL))));
  }

  public ExpandableStringLiteral EXPANDABLE_STRING_LITERAL() {
    return b.<ExpandableStringLiteral>nonterminal(DockerLexicalGrammar.EXPANDABLE_STRING_LITERAL).is(
      f.expandableStringLiteral(
        b.token(Punctuator.DOUBLE_QUOTE),
        b.oneOrMore(
          b.firstOf(
            EXPANDABLE_STRING_CHARACTERS(),
            VARIABLE())),
        b.token(Punctuator.DOUBLE_QUOTE)));
  }

  public ExpandableStringLiteral EXPANDABLE_STRING_LITERAL_GENERIC() {
    return b.<ExpandableStringLiteral>nonterminal(DockerLexicalGrammar.EXPANDABLE_STRING_LITERAL_GENERIC).is(
      f.expandableStringLiteral(
        b.token(Punctuator.DOUBLE_QUOTE),
        b.oneOrMore(
          b.firstOf(
            EXPANDABLE_STRING_CHARACTERS(),
            VARIABLE_GENERIC())),
        b.token(Punctuator.DOUBLE_QUOTE)));
  }

  public Expression EXPANDABLE_STRING_CHARACTERS() {
    return b.<ExpandableStringCharacters>nonterminal().is(
      f.expandableStringCharacters(b.token(DockerLexicalGrammar.STRING_WITH_ENCAPS_VAR_CHARACTERS)));
  }

  public Expression VARIABLE() {
    return b.<Expression>nonterminal().is(
      b.firstOf(
        REGULAR_VARIABLE(),
        ENCAPS_VARIABLE()));
  }

  public Expression VARIABLE_GENERIC() {
    return b.<Expression>nonterminal().is(
      b.firstOf(
        REGULAR_VARIABLE(),
        ENCAPS_VARIABLE_GENERIC()));
  }

  public Expression REGULAR_VARIABLE() {
    return b.<Expression>nonterminal(DockerLexicalGrammar.REGULAR_VARIABLE).is(
      f.regularVariable(
        b.token(Punctuator.DOLLAR),
        b.token(DockerLexicalGrammar.REGULAR_VAR_IDENTIFIER)));
  }

  public Expression ENCAPS_VARIABLE() {
    return b.<Expression>nonterminal(DockerLexicalGrammar.ENCAPSULATED_VARIABLE).is(
      f.encapsulatedVariable(
        b.token(Punctuator.DOLLAR_LCURLY),
        b.token(DockerLexicalGrammar.REGULAR_VAR_IDENTIFIER),
        b.optional(
          f.tuple(
            b.token(DockerLexicalGrammar.ENCAPS_VAR_MODIFIER_SEPARATOR),
            b.optional(ENCAPS_VARIABLE_MODIFIER()))),
        b.token(Punctuator.RCURLYBRACE)));
  }

  public Expression ENCAPS_VARIABLE_GENERIC() {
    return b.<Expression>nonterminal(DockerLexicalGrammar.ENCAPSULATED_VARIABLE_GENERIC).is(
      f.encapsulatedVariableGeneric(
        b.token(Punctuator.DOLLAR_LCURLY),
        b.token(DockerLexicalGrammar.REGULAR_VAR_IDENTIFIER),
        b.optional(
          b.token(DockerLexicalGrammar.ENCAPS_VAR_MODIFIER_GENERIC)),
        b.token(Punctuator.RCURLYBRACE)));
  }

  public Argument ENCAPS_VARIABLE_MODIFIER() {
    return b.<Argument>nonterminal().is(
      f.newArgument(
        b.oneOrMore(
          b.firstOf(
            EXPANDABLE_STRING_LITERAL(),
            VARIABLE(),
            f.regularStringLiteral(b.token(DockerLexicalGrammar.UNQUOTED_VARIABLE_MODIFIER)),
            f.regularStringLiteral(b.token(DockerLexicalGrammar.QUOTED_STRING_LITERAL))))));
  }
}
