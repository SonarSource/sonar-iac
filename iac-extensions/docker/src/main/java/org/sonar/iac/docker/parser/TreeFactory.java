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
package org.sonar.iac.docker.parser;

import com.sonar.sslr.api.typed.Optional;
import org.sonar.iac.common.api.tree.SeparatedList;
import org.sonar.iac.common.api.tree.impl.Tuple;
import org.sonar.iac.docker.tree.api.AddInstruction;
import org.sonar.iac.docker.tree.api.Alias;
import org.sonar.iac.docker.tree.api.ArgInstruction;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.ArgumentList;
import org.sonar.iac.docker.tree.api.Body;
import org.sonar.iac.docker.tree.api.CmdInstruction;
import org.sonar.iac.docker.tree.api.CopyInstruction;
import org.sonar.iac.docker.tree.api.DockerImage;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.EncapsulatedVariable;
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
import org.sonar.iac.docker.tree.api.RegularVariable;
import org.sonar.iac.docker.tree.api.RunInstruction;
import org.sonar.iac.docker.tree.api.ShellForm;
import org.sonar.iac.docker.tree.api.ShellInstruction;
import org.sonar.iac.docker.tree.api.StopSignalInstruction;
import org.sonar.iac.docker.tree.api.SyntaxToken;
import org.sonar.iac.docker.tree.api.UserInstruction;
import org.sonar.iac.docker.tree.api.VolumeInstruction;
import org.sonar.iac.docker.tree.api.WorkdirInstruction;
import org.sonar.iac.docker.tree.impl.AddInstructionImpl;
import org.sonar.iac.docker.tree.impl.AliasImpl;
import org.sonar.iac.docker.tree.impl.ArgInstructionImpl;
import org.sonar.iac.docker.tree.impl.ArgumentImpl;
import org.sonar.iac.docker.tree.impl.BodyImpl;
import org.sonar.iac.docker.tree.impl.CmdInstructionImpl;
import org.sonar.iac.docker.tree.impl.CopyInstructionImpl;
import org.sonar.iac.docker.tree.impl.DockerImageImpl;
import org.sonar.iac.docker.tree.impl.EncapsulatedVariableImpl;
import org.sonar.iac.docker.tree.impl.EntrypointInstructionImpl;
import org.sonar.iac.docker.tree.impl.EnvInstructionImpl;
import org.sonar.iac.docker.tree.impl.ExecFormImpl;
import org.sonar.iac.docker.tree.impl.ExpandableStringCharactersImpl;
import org.sonar.iac.docker.tree.impl.ExpandableStringLiteralImpl;
import org.sonar.iac.docker.tree.impl.ExposeInstructionImpl;
import org.sonar.iac.docker.tree.impl.FileImpl;
import org.sonar.iac.docker.tree.impl.FlagImpl;
import org.sonar.iac.docker.tree.impl.FromInstructionImpl;
import org.sonar.iac.docker.tree.impl.HealthCheckInstructionImpl;
import org.sonar.iac.docker.tree.impl.HereDocumentImpl;
import org.sonar.iac.docker.tree.impl.KeyValuePairImpl;
import org.sonar.iac.docker.tree.impl.LabelInstructionImpl;
import org.sonar.iac.docker.tree.impl.LiteralImpl;
import org.sonar.iac.docker.tree.impl.MaintainerInstructionImpl;
import org.sonar.iac.docker.tree.impl.OnBuildInstructionImpl;
import org.sonar.iac.docker.tree.impl.RegularVariableImpl;
import org.sonar.iac.docker.tree.impl.RunInstructionImpl;
import org.sonar.iac.docker.tree.impl.ShellFormImpl;
import org.sonar.iac.docker.tree.impl.ShellInstructionImpl;
import org.sonar.iac.docker.tree.impl.StopSignalInstructionImpl;
import org.sonar.iac.docker.tree.impl.UserInstructionImpl;
import org.sonar.iac.docker.tree.impl.VolumeInstructionImpl;
import org.sonar.iac.docker.tree.impl.WorkdirInstructionImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.sonar.iac.common.api.tree.impl.SeparatedListImpl.emptySeparatedList;
import static org.sonar.iac.common.api.tree.impl.SeparatedListImpl.separatedList;

// S1172 - Unused function parameters should be removed - the spacing argument is ignored, but it's needed from grammar perspective
@SuppressWarnings("java:S1172")
public class TreeFactory {

  private static final DockerHeredocParser HEREDOC_PARSER = DockerHeredocParser.create();

  public File file(Body body, Optional<SyntaxToken> spacing, SyntaxToken eof) {
    return new FileImpl(body, eof);
  }

  public Body body(Optional<List<ArgInstruction>> globalArgs, List<DockerImage> dockerImages) {
    return new BodyImpl(globalArgs.or(Collections.emptyList()), dockerImages);
  }

  public DockerImage dockerImage(FromInstruction from, Optional<List<Instruction>> instructions) {
    return new DockerImageImpl(from, instructions.or(Collections.emptyList()));
  }

  public Instruction instruction(Instruction instruction) {
    return instruction;
  }

  public OnBuildInstruction onbuild(SyntaxToken keyword, Instruction instruction) {
    return new OnBuildInstructionImpl(keyword, instruction);
  }

  public FromInstruction from(SyntaxToken keyword, Optional<Flag> platform, Argument image, Optional<Alias> alias) {
    return new FromInstructionImpl(keyword, platform.orNull(), image, alias.orNull());
  }

  public Alias alias(SyntaxToken keyword, SyntaxToken alias) {
    return new AliasImpl(keyword, alias);
  }

  public MaintainerInstruction maintainer(SyntaxToken keyword, List<SyntaxToken> authorsToken) {
    return new MaintainerInstructionImpl(keyword, authorsToken);
  }

  public SyntaxToken argument(SyntaxToken token) {
    return token;
  }

  public StopSignalInstruction stopSignal(SyntaxToken keyword, SyntaxToken whitespace, Argument argument) {
    return new StopSignalInstructionImpl(keyword, argument);
  }

  public WorkdirInstruction workdir(SyntaxToken keyword, List<Argument> arguments) {
    return new WorkdirInstructionImpl(keyword, arguments);
  }

  public ExposeInstruction expose(SyntaxToken keyword, List<Argument> arguments) {
    return new ExposeInstructionImpl(keyword, arguments);
  }

  public LabelInstruction label(SyntaxToken token, List<KeyValuePair> keyValuePairs) {
    return new LabelInstructionImpl(token, keyValuePairs);
  }

  public LabelInstruction label(SyntaxToken token, KeyValuePair keyValuePair) {
    return new LabelInstructionImpl(token, Collections.singletonList(keyValuePair));
  }

  public EnvInstruction env(SyntaxToken keyword, List<KeyValuePair> keyValuePairs) {
    return new EnvInstructionImpl(keyword, keyValuePairs);
  }

  public ArgInstruction arg(SyntaxToken token, List<KeyValuePair> argNames) {
    return new ArgInstructionImpl(token, argNames);
  }

  public AddInstruction add(SyntaxToken add, Optional<List<Flag>> options, ArgumentList srcsAndDest) {
    return new AddInstructionImpl(add, options.or(Collections.emptyList()), srcsAndDest);
  }

  public CopyInstruction copy(SyntaxToken copy, Optional<List<Flag>> options, ArgumentList srcsAndDest) {
    return new CopyInstructionImpl(copy, options.or(Collections.emptyList()), srcsAndDest);
  }

  public Flag flag(SyntaxToken prefix, SyntaxToken name, Optional<SyntaxToken> equals, Optional<Argument> value) {
    return new FlagImpl(prefix, name, equals.orNull(), value.orNull());
  }

  public CmdInstruction cmd(SyntaxToken token, Optional<ArgumentList> execFormOrShellForm) {
    return new CmdInstructionImpl(token, execFormOrShellForm.orNull());
  }

  public EntrypointInstruction entrypoint(SyntaxToken token, Optional<ArgumentList> execFormOrShellForm) {
    return new EntrypointInstructionImpl(token, execFormOrShellForm.orNull());
  }

  public RunInstruction run(SyntaxToken token, Optional<List<Flag>> options, Optional<ArgumentList> execFormOrShellForm) {
    return new RunInstructionImpl(token, options.or(Collections.emptyList()), execFormOrShellForm.orNull());
  }

  public UserInstruction user(SyntaxToken keyword, List<Argument> arguments) {
    return new UserInstructionImpl(keyword, arguments);
  }

  public VolumeInstruction volume(SyntaxToken token, ArgumentList execFormOrShellForm) {
    return new VolumeInstructionImpl(token, execFormOrShellForm);
  }

  public ShellInstruction shell(SyntaxToken token, ExecForm execForm) {
    return new ShellInstructionImpl(token, execForm);
  }

  public HealthCheckInstruction healthcheck(SyntaxToken healthcheck, Optional<List<Flag>> flags, DockerTree noneOrCmd) {
    if (noneOrCmd instanceof CmdInstruction cmdInstruction) {
      return new HealthCheckInstructionImpl(healthcheck, flags.or(Collections.emptyList()), cmdInstruction, null);
    } else {
      return new HealthCheckInstructionImpl(healthcheck, flags.or(Collections.emptyList()), null, (SyntaxToken) noneOrCmd);
    }
  }

  public HereDocument hereDocument(SyntaxToken token) {
    return (HereDocument) HEREDOC_PARSER.parse(token);
  }

  public HereDocument hereDocumentContent(Argument firstArgument, Optional<List<Argument>> otherArguments) {
    List<Argument> arguments = new ArrayList<>();
    arguments.add(firstArgument);
    arguments.addAll(otherArguments.or(Collections.emptyList()));
    return new HereDocumentImpl(arguments);
  }

  public Argument singleExpressionArgument(Expression expression) {
    return new ArgumentImpl(List.of(expression));
  }

  public ExecForm execForm(SyntaxToken leftBracket, Optional<Argument> firstArgument,
    Optional<List<Tuple<SyntaxToken, Argument>>> otherArguments,
    SyntaxToken rightBracket) {

    SeparatedList<Argument, SyntaxToken> separatedList;
    if (firstArgument.isPresent()) {
      separatedList = separatedList(firstArgument.get(), otherArguments);
    } else {
      separatedList = emptySeparatedList();
    }

    return new ExecFormImpl(leftBracket, separatedList, rightBracket);
  }

  public ShellForm shellForm(List<Argument> arguments) {
    return new ShellFormImpl(arguments);
  }

  public <T, U> Tuple<T, U> tuple(T first, U second) {
    return new Tuple<>(first, second);
  }

  public <T, U> U ignoreFirst(T first, U second) {
    return second;
  }

  public Literal regularStringLiteral(SyntaxToken token) {
    return new LiteralImpl(token);
  }

  public ExpandableStringLiteral expandableStringLiteral(
    SyntaxToken openDoubleQuote,
    List<Expression> expressions,
    SyntaxToken closeDoubleQuote) {
    return new ExpandableStringLiteralImpl(openDoubleQuote, expressions, closeDoubleQuote);
  }

  public ExpandableStringCharacters expandableStringCharacters(SyntaxToken token) {
    return new ExpandableStringCharactersImpl(token);
  }

  public RegularVariable regularVariable(SyntaxToken dollar, SyntaxToken identifier) {
    return new RegularVariableImpl(dollar, identifier);
  }

  public EncapsulatedVariable encapsulatedVariable(SyntaxToken openDollarCurly, SyntaxToken identifier, Optional<Tuple<SyntaxToken, Argument>> modifier, SyntaxToken closeCurly) {
    if (modifier.isPresent()) {
      return new EncapsulatedVariableImpl(openDollarCurly, identifier, modifier.get().first(), modifier.get().second(), closeCurly);
    }
    return new EncapsulatedVariableImpl(openDollarCurly, identifier, null, null, closeCurly);
  }

  public EncapsulatedVariable encapsulatedVariableGeneric(SyntaxToken openDollarCurly, SyntaxToken identifier, Optional<SyntaxToken> modifier, SyntaxToken closeCurly) {
    if (modifier.isPresent()) {
      List<Expression> modifierExpr = new ArrayList<>();
      modifierExpr.add(new LiteralImpl(modifier.get()));
      return new EncapsulatedVariableImpl(openDollarCurly, identifier, null, new ArgumentImpl(modifierExpr), closeCurly);
    } else {
      return new EncapsulatedVariableImpl(openDollarCurly, identifier, null, null, closeCurly);
    }
  }

  public Argument newArgument(List<Expression> expressions) {
    return new ArgumentImpl(expressions);
  }

  public Argument asArgument(Expression expressions) {
    return new ArgumentImpl(List.of(expressions));
  }

  public KeyValuePair keyValuePair(Argument key, SyntaxToken equalSign, Argument value) {
    return new KeyValuePairImpl(key, equalSign, value);
  }

  public KeyValuePair keyValuePair(Argument key, Argument firstValue, Optional<List<Tuple<SyntaxToken, Argument>>> moreValue) {
    List<Expression> expressions = new LinkedList<>(firstValue.expressions());

    for (Tuple<SyntaxToken, Argument> valuePart : moreValue.or(Collections.emptyList())) {
      expressions.add(new LiteralImpl(valuePart.first()));
      expressions.addAll(valuePart.second().expressions());
    }

    Argument value = new ArgumentImpl(expressions);
    return new KeyValuePairImpl(key, null, value);
  }

  public KeyValuePair keyValuePair(Argument key, Optional<SyntaxToken> equalSign) {
    return new KeyValuePairImpl(key, equalSign.orNull(), null);
  }
}
