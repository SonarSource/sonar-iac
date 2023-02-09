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

import com.sonar.sslr.api.typed.Optional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.sonar.iac.docker.tree.api.AddInstruction;
import org.sonar.iac.docker.tree.api.Alias;
import org.sonar.iac.docker.tree.api.ArgInstruction;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.Expression;
import org.sonar.iac.docker.tree.api.Body;
import org.sonar.iac.docker.tree.api.CmdInstruction;
import org.sonar.iac.docker.tree.api.CopyInstruction;
import org.sonar.iac.docker.tree.api.DockerImage;
import org.sonar.iac.docker.tree.api.EncapsulatedVariable;
import org.sonar.iac.docker.tree.api.EntrypointInstruction;
import org.sonar.iac.docker.tree.api.EnvInstruction;
import org.sonar.iac.docker.tree.api.ExecForm;
import org.sonar.iac.docker.tree.api.ExecFormLiteral;
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
import org.sonar.iac.docker.tree.api.LiteralList;
import org.sonar.iac.docker.tree.api.MaintainerInstruction;
import org.sonar.iac.docker.tree.api.NoneInstruction;
import org.sonar.iac.docker.tree.api.OnBuildInstruction;
import org.sonar.iac.docker.tree.api.Param;
import org.sonar.iac.docker.tree.api.Port;
import org.sonar.iac.docker.tree.api.RegularVariable;
import org.sonar.iac.docker.tree.api.RunInstruction;
import org.sonar.iac.docker.tree.api.SeparatedList;
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
import org.sonar.iac.docker.tree.impl.ExecFormLiteralImpl;
import org.sonar.iac.docker.tree.impl.ExpandableStringCharactersImpl;
import org.sonar.iac.docker.tree.impl.ExpandableStringLiteralImpl;
import org.sonar.iac.docker.tree.impl.ExposeInstructionImpl;
import org.sonar.iac.docker.tree.impl.FileImpl;
import org.sonar.iac.docker.tree.impl.FromInstructionImpl;
import org.sonar.iac.docker.tree.impl.HealthCheckInstructionImpl;
import org.sonar.iac.docker.tree.impl.HereDocumentImpl;
import org.sonar.iac.docker.tree.impl.ImageImpl;
import org.sonar.iac.docker.tree.impl.KeyValuePairImpl;
import org.sonar.iac.docker.tree.impl.LabelInstructionImpl;
import org.sonar.iac.docker.tree.impl.LiteralImpl;
import org.sonar.iac.docker.tree.impl.MaintainerInstructionImpl;
import org.sonar.iac.docker.tree.impl.NoneImpl;
import org.sonar.iac.docker.tree.impl.OnBuildInstructionImpl;
import org.sonar.iac.docker.tree.impl.ParamImpl;
import org.sonar.iac.docker.tree.impl.PortImpl;
import org.sonar.iac.docker.tree.impl.RegularVariableImpl;
import org.sonar.iac.docker.tree.impl.RunInstructionImpl;
import org.sonar.iac.docker.tree.impl.SeparatedListImpl;
import org.sonar.iac.docker.tree.impl.ShellFormImpl;
import org.sonar.iac.docker.tree.impl.ShellInstructionImpl;
import org.sonar.iac.docker.tree.impl.StopSignalInstructionImpl;
import org.sonar.iac.docker.tree.impl.UserInstructionImpl;
import org.sonar.iac.docker.tree.impl.VolumeInstructionImpl;
import org.sonar.iac.docker.tree.impl.WorkdirInstructionImpl;

// S1172 - Unused function parameters should be removed - the spacing argument is ignored, but it's needed from grammar perspective
@SuppressWarnings("java:S1172")
public class TreeFactory {

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

  public FromInstruction from(SyntaxToken keyword, Optional<Param> platform, Image image, Optional<Alias> alias) {
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

  public WorkdirInstruction workdir(SyntaxToken keyword, List<SyntaxToken> values) {
    return new WorkdirInstructionImpl(keyword, values);
  }

  public ExposeInstruction expose(SyntaxToken keyword, List<Port> ports) {
    return new ExposeInstructionImpl(keyword, ports);
  }

  public Port port(SyntaxToken portMin, SyntaxToken separatorPort, SyntaxToken portMax, SyntaxToken separatorProtocol, SyntaxToken protocol) {
    return new PortImpl(portMin, separatorPort, portMax, separatorProtocol, protocol);
  }

  public Port port(SyntaxToken portMin, SyntaxToken separatorPort, SyntaxToken portMax, Optional<SyntaxToken> separatorProtocol) {
    return new PortImpl(portMin, separatorPort, portMax, separatorProtocol.orNull(), null);
  }

  public Port port(SyntaxToken port, SyntaxToken separatorProtocol, SyntaxToken protocol) {
    return new PortImpl(port, null, port, separatorProtocol, protocol);
  }

  public Port port(SyntaxToken port, Optional<SyntaxToken> separatorProtocol) {
    return new PortImpl(port, null, port, separatorProtocol.orNull(), null);
  }

  public Port port(SyntaxToken portToken) {
    return new PortImpl(portToken, null, portToken, null, null);
  }

  public LabelInstruction label(SyntaxToken token, List<KeyValuePair> keyValuePairs) {
    return new LabelInstructionImpl(token, keyValuePairs);
  }

  public EnvInstruction env(SyntaxToken keyword, List<KeyValuePair> keyValuePairs) {
    return new EnvInstructionImpl(keyword, keyValuePairs);
  }

  public ArgInstruction arg(SyntaxToken token, List<KeyValuePair> argNames) {
    return new ArgInstructionImpl(token, argNames);
  }

  public AddInstruction add(SyntaxToken add, Optional<List<Param>> options, LiteralList srcsAndDest) {
    return new AddInstructionImpl(add, options.or(Collections.emptyList()), srcsAndDest);
  }

  public CopyInstruction copy(SyntaxToken copy, Optional<List<Param>> options, LiteralList srcsAndDest) {
    return new CopyInstructionImpl(copy, options.or(Collections.emptyList()), srcsAndDest);
  }

  public KeyValuePair key(SyntaxToken key) {
    return new KeyValuePairImpl(key, null, null);
  }

  public KeyValuePair keyValuePair(SyntaxToken key, SyntaxToken value) {
    return new KeyValuePairImpl(key, null, value);
  }

  public KeyValuePair keyValuePairEquals(SyntaxToken key, SyntaxToken equals, Optional<SyntaxToken> value) {
    return new KeyValuePairImpl(key, equals, value.orNull());
  }

  public Param param(SyntaxToken prefix, SyntaxToken name, SyntaxToken equals, Optional<SyntaxToken> value) {
    return new ParamImpl(prefix, name, equals, value.orNull());
  }

  public Param param(SyntaxToken prefix, SyntaxToken name) {
    return new ParamImpl(prefix, name, null, null);
  }

  public Image image(SyntaxToken name, Optional<SyntaxToken> tag, Optional<SyntaxToken> digest) {
    return new ImageImpl(name, tag.orNull(), digest.orNull());
  }

  public CmdInstruction cmd(SyntaxToken token, Optional<LiteralList> execFormOrShellForm) {
    return new CmdInstructionImpl(token, execFormOrShellForm.orNull());
  }

  public EntrypointInstruction entrypoint(SyntaxToken token, Optional<LiteralList> execFormOrShellForm) {
    return new EntrypointInstructionImpl(token, execFormOrShellForm.orNull());
  }

  public RunInstruction run(SyntaxToken token, Optional<List<Param>> options, Optional<LiteralList> execFormOrShellForm) {
    return new RunInstructionImpl(token, options.or(Collections.emptyList()), execFormOrShellForm.orNull());
  }

  public UserInstruction user(SyntaxToken keyword, SyntaxToken user, Optional<Tuple<SyntaxToken, SyntaxToken>> colonAndGroup) {
    if (colonAndGroup.isPresent()) {
      return new UserInstructionImpl(keyword, user, colonAndGroup.get().first(), colonAndGroup.get().second());
    } else {
      return new UserInstructionImpl(keyword, user, null, null);
    }
  }

  public VolumeInstruction volume(SyntaxToken token, LiteralList execFormOrShellForm) {
    return new VolumeInstructionImpl(token, execFormOrShellForm);
  }

  public ShellInstruction shell(SyntaxToken token, ExecForm execForm) {
    return new ShellInstructionImpl(token, execForm);
  }

  public HealthCheckInstruction healthcheck(SyntaxToken healthcheck, Optional<List<Param>> options, Instruction instruction) {
    return new HealthCheckInstructionImpl(healthcheck, options.or(Collections.emptyList()), instruction);
  }

  public NoneInstruction none(SyntaxToken none) {
    return new NoneImpl(none);
  }

  public HereDocument hereDocument(SyntaxToken content) {
    return new HereDocumentImpl(content);
  }

  public ExecForm execForm(SyntaxToken leftBracket,
    Optional<Tuple<SyntaxToken, Optional<List<Tuple<SyntaxToken, SyntaxToken>>>>> literals,
    SyntaxToken rightBracket) {

    List<ExecFormLiteral> elements = new ArrayList<>();
    List<SyntaxToken> separators = new ArrayList<>();
    SeparatedList<ExecFormLiteral> separatedList = new SeparatedListImpl<>(elements, separators);
    if (literals.isPresent()) {
      Tuple<SyntaxToken, Optional<List<Tuple<SyntaxToken, SyntaxToken>>>> tuple = literals.get();
      elements.add(new ExecFormLiteralImpl(tuple.first()));
      Optional<List<Tuple<SyntaxToken, SyntaxToken>>> second = tuple.second();
      if(second.isPresent()) {
        List<Tuple<SyntaxToken, SyntaxToken>> comaAndLiterals = second.get();
        for (Tuple<SyntaxToken, SyntaxToken> comaAndLiteral : comaAndLiterals) {
          separators.add(comaAndLiteral.first());
          elements.add(new ExecFormLiteralImpl(comaAndLiteral.second()));
        }
      }
    }

    return new ExecFormImpl(leftBracket, separatedList, rightBracket);
  }

  public ShellForm shellForm(List<SyntaxToken> tokens) {
    return new ShellFormImpl(tokens);
  }

  public <T, U> Tuple<T, U> tuple(T first, U second) {
    return new Tuple<>(first, second);
  }

  public Literal regularStringLiteral(SyntaxToken token) {
    return new LiteralImpl(token);
  }

  public ExpandableStringLiteral expandableStringLiteral(
    SyntaxToken openDoubleQuote,
    List<Expression> expressions,
    SyntaxToken closeDoubleQuote
  ) {
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

  public Argument newArgument(List<Expression> expressions) {
    return new ArgumentImpl(expressions);
  }


  public static class Tuple<T, U> {

    private final T first;
    private final U second;

    public Tuple(T first, U second) {
      super();

      this.first = first;
      this.second = second;
    }

    public T first() {
      return first;
    }

    public U second() {
      return second;
    }
  }
}
