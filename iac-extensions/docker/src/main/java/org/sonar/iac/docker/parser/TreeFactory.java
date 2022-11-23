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
import org.sonar.iac.docker.tree.api.AliasTree;
import org.sonar.iac.docker.tree.api.ArgTree;
import org.sonar.iac.docker.tree.api.CmdTree;
import org.sonar.iac.docker.tree.api.EntrypointTree;
import org.sonar.iac.docker.tree.api.CopyTree;
import org.sonar.iac.docker.tree.api.EnvTree;
import org.sonar.iac.docker.tree.api.ExecFormLiteralTree;
import org.sonar.iac.docker.tree.api.ExecFormTree;
import org.sonar.iac.docker.tree.api.AddTree;
import org.sonar.iac.docker.tree.api.ExposeTree;
import org.sonar.iac.docker.tree.api.FileTree;
import org.sonar.iac.docker.tree.api.FromTree;
import org.sonar.iac.docker.tree.api.ImageTree;
import org.sonar.iac.docker.tree.api.InstructionTree;
import org.sonar.iac.docker.tree.api.KeyValuePairTree;
import org.sonar.iac.docker.tree.api.LabelTree;
import org.sonar.iac.docker.tree.api.LiteralListTree;
import org.sonar.iac.docker.tree.api.MaintainerTree;
import org.sonar.iac.docker.tree.api.OnBuildTree;
import org.sonar.iac.docker.tree.api.ParamTree;
import org.sonar.iac.docker.tree.api.ShellTree;
import org.sonar.iac.docker.tree.api.StopSignalTree;
import org.sonar.iac.docker.tree.api.PortTree;
import org.sonar.iac.docker.tree.api.SeparatedList;
import org.sonar.iac.docker.tree.api.ShellFormTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;
import org.sonar.iac.docker.tree.api.UserTree;
import org.sonar.iac.docker.tree.api.WorkdirTree;
import org.sonar.iac.docker.tree.impl.AliasTreeImpl;
import org.sonar.iac.docker.tree.impl.ArgTreeImpl;
import org.sonar.iac.docker.tree.impl.CmdTreeImpl;
import org.sonar.iac.docker.tree.impl.EntrypointTreeImpl;
import org.sonar.iac.docker.tree.impl.CopyTreeImpl;
import org.sonar.iac.docker.tree.impl.EnvTreeImpl;
import org.sonar.iac.docker.tree.impl.ExecFormLiteralTreeImpl;
import org.sonar.iac.docker.tree.impl.ExecFormTreeImpl;
import org.sonar.iac.docker.tree.impl.AddTreeImpl;
import org.sonar.iac.docker.tree.impl.ExposeTreeImpl;
import org.sonar.iac.docker.tree.api.VolumeTree;
import org.sonar.iac.docker.tree.impl.FileTreeImpl;
import org.sonar.iac.docker.tree.impl.FromTreeImpl;
import org.sonar.iac.docker.tree.impl.ImageTreeImpl;
import org.sonar.iac.docker.tree.impl.KeyValuePairTreeImpl;
import org.sonar.iac.docker.tree.impl.LabelTreeImpl;
import org.sonar.iac.docker.tree.impl.MaintainerTreeImpl;
import org.sonar.iac.docker.tree.impl.OnBuildTreeImpl;
import org.sonar.iac.docker.tree.impl.ParamTreeImpl;
import org.sonar.iac.docker.tree.impl.PortTreeImpl;
import org.sonar.iac.docker.tree.impl.SeparatedListImpl;
import org.sonar.iac.docker.tree.impl.ShellFormTreeImpl;
import org.sonar.iac.docker.tree.impl.ShellTreeImpl;
import org.sonar.iac.docker.tree.impl.StopSignalTreeImpl;
import org.sonar.iac.docker.tree.impl.UserTreeImpl;
import org.sonar.iac.docker.tree.impl.VolumeTreeImpl;
import org.sonar.iac.docker.tree.impl.WorkdirTreeImpl;

// S1172 - Unused function parameters should be removed - the spacing argument is ignored, but it's needed from grammar perspective
@SuppressWarnings("java:S1172")
public class TreeFactory {

  public FileTree file(Optional<List<InstructionTree>> instructions, Optional<SyntaxToken> spacing, SyntaxToken eof) {
    return new FileTreeImpl(instructions.or(Collections.emptyList()), eof);
  }

  public InstructionTree instruction(Optional<SyntaxToken> spacing, InstructionTree instruction) {
    return instruction;
  }

  public OnBuildTree onbuild(SyntaxToken keyword, InstructionTree instruction) {
    return new OnBuildTreeImpl(keyword, instruction);
  }

  public FromTree from(SyntaxToken keyword, Optional<ParamTree> platform, ImageTree image, Optional<AliasTree> alias) {
    return new FromTreeImpl(keyword, platform.orNull(), image, alias.orNull());
  }

  public AliasTree alias(SyntaxToken keyword, SyntaxToken alias) {
    return new AliasTreeImpl(keyword, alias);
  }

  public MaintainerTree maintainer(SyntaxToken keyword, List<SyntaxToken> authorsToken) {
    return new MaintainerTreeImpl(keyword, authorsToken);
  }

  public SyntaxToken argument(SyntaxToken token) {
    return token;
  }

  public StopSignalTree stopSignal(SyntaxToken keyword, SyntaxToken tokenValue) {
    return new StopSignalTreeImpl(keyword, tokenValue);
  }

  public WorkdirTree workdir(SyntaxToken keyword, List<SyntaxToken> values) {
    return new WorkdirTreeImpl(keyword, values);
  }

  public ExposeTree expose(SyntaxToken keyword, List<PortTree> ports) {
    return new ExposeTreeImpl(keyword, ports);
  }

  public PortTree port(SyntaxToken portToken, SyntaxToken separatorToken, Optional<SyntaxToken> protocolToken) {
    return new PortTreeImpl(portToken, portToken, separatorToken, protocolToken.orNull());
  }

  public PortTree port(SyntaxToken portMin, SyntaxToken separatorToken, SyntaxToken portMax) {
    return new PortTreeImpl(portMin, portMax, separatorToken, null);
  }

  public PortTree port(SyntaxToken portToken) {
    return new PortTreeImpl(portToken, portToken, null, null);
  }

  public LabelTree label(SyntaxToken token, List<KeyValuePairTree> keyValuePairs) {
    return new LabelTreeImpl(token, keyValuePairs);
  }

  public EnvTree env(SyntaxToken keyword, List<KeyValuePairTree> keyValuePairs) {
    return new EnvTreeImpl(keyword, keyValuePairs);
  }

  public ArgTree arg(SyntaxToken token, List<KeyValuePairTree> argNames) {
    return new ArgTreeImpl(token, argNames);
  }

  public AddTree add(SyntaxToken add, Optional<List<ParamTree>> options, LiteralListTree srcsAndDest) {
    return new AddTreeImpl(add, options.or(Collections.emptyList()), srcsAndDest);
  }

  public CopyTree copy(SyntaxToken copy, Optional<List<ParamTree>> options, LiteralListTree srcsAndDest) {
    return new CopyTreeImpl(copy, options.or(Collections.emptyList()), srcsAndDest);
  }

  public KeyValuePairTree key(SyntaxToken key) {
    return new KeyValuePairTreeImpl(key, null, null);
  }

  public KeyValuePairTree keyValuePair(SyntaxToken key, SyntaxToken value) {
    return new KeyValuePairTreeImpl(key, null, value);
  }

  public KeyValuePairTree keyValuePairEquals(SyntaxToken key, SyntaxToken equals, SyntaxToken value) {
    return new KeyValuePairTreeImpl(key, equals, value);
  }

  public ParamTree param(SyntaxToken prefix, SyntaxToken name, SyntaxToken equals, SyntaxToken value) {
    return new ParamTreeImpl(prefix, name, equals, value);
  }

  public ParamTree param(SyntaxToken prefix, SyntaxToken name) {
    return new ParamTreeImpl(prefix, name, null, null);
  }

  public ImageTree image(SyntaxToken name, Optional<SyntaxToken> tag, Optional<SyntaxToken> digest) {
    return new ImageTreeImpl(name, tag.orNull(), digest.orNull());
  }

  public CmdTree cmd(SyntaxToken token, Optional<LiteralListTree> execFormOrShellForm) {
    return new CmdTreeImpl(token, execFormOrShellForm.orNull());
  }

  public EntrypointTree entrypoint(SyntaxToken token, Optional<LiteralListTree> execFormOrShellForm) {
    return new EntrypointTreeImpl(token, execFormOrShellForm.orNull());
  }

  public UserTree user(SyntaxToken keyword, SyntaxToken user, Optional<Tuple<SyntaxToken, SyntaxToken>> colonAndGroup) {
    if (colonAndGroup.isPresent()) {
      return new UserTreeImpl(keyword, user, colonAndGroup.get().first(), colonAndGroup.get().second());
    } else {
      return new UserTreeImpl(keyword, user, null, null);
    }
  }

  public VolumeTree volume(SyntaxToken token, LiteralListTree execFormOrShellForm) {
    return new VolumeTreeImpl(token, execFormOrShellForm);
  }

  public ShellTree shell(SyntaxToken token, ExecFormTree execForm) {
    return new ShellTreeImpl(token, execForm);
  }

  public ExecFormTree execForm(SyntaxToken leftBracket,
    Optional<Tuple<SyntaxToken, Optional<List<Tuple<SyntaxToken, SyntaxToken>>>>> literals,
    SyntaxToken rightBracket) {

    List<ExecFormLiteralTree> elements = new ArrayList<>();
    List<SyntaxToken> separators = new ArrayList<>();
    SeparatedList<ExecFormLiteralTree> separatedList = new SeparatedListImpl<>(elements, separators);
    if (literals.isPresent()) {
      Tuple<SyntaxToken, Optional<List<Tuple<SyntaxToken, SyntaxToken>>>> tuple = literals.get();
        elements.add(new ExecFormLiteralTreeImpl(tuple.first()));
        Optional<List<Tuple<SyntaxToken, SyntaxToken>>> second = tuple.second();
        if(second.isPresent()) {
          List<Tuple<SyntaxToken, SyntaxToken>> comaAndLiterals = second.get();
          for (Tuple<SyntaxToken, SyntaxToken> comaAndLiteral : comaAndLiterals) {
            separators.add(comaAndLiteral.first());
            elements.add(new ExecFormLiteralTreeImpl(comaAndLiteral.second()));
          }
        }
    }

    return new ExecFormTreeImpl(leftBracket, separatedList, rightBracket);
  }

  public ShellFormTree shellForm(List<SyntaxToken> tokens) {
    return new ShellFormTreeImpl(tokens);
  }

  public <T, U> Tuple<T, U> tuple(T first, U second) {
    return new Tuple<>(first, second);
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
