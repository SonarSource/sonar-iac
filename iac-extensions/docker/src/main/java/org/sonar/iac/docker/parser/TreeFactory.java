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
import java.util.Collections;
import java.util.List;
import org.sonar.iac.docker.tree.api.AliasTree;
import org.sonar.iac.docker.tree.api.ArgTree;
import org.sonar.iac.docker.tree.api.EnvTree;
import org.sonar.iac.docker.tree.api.ExposeTree;
import org.sonar.iac.docker.tree.api.FileTree;
import org.sonar.iac.docker.tree.api.FromTree;
import org.sonar.iac.docker.tree.api.InstructionTree;
import org.sonar.iac.docker.tree.api.KeyValuePairTree;
import org.sonar.iac.docker.tree.api.LabelTree;
import org.sonar.iac.docker.tree.api.MaintainerTree;
import org.sonar.iac.docker.tree.api.StopSignalTree;
import org.sonar.iac.docker.tree.api.PortTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;
import org.sonar.iac.docker.tree.api.WorkdirTree;
import org.sonar.iac.docker.tree.impl.AliasTreeImpl;
import org.sonar.iac.docker.tree.impl.ArgTreeImpl;
import org.sonar.iac.docker.tree.impl.EnvTreeImpl;
import org.sonar.iac.docker.tree.impl.ExposeTreeImpl;
import org.sonar.iac.docker.tree.impl.FileTreeImpl;
import org.sonar.iac.docker.tree.impl.FromTreeImpl;
import org.sonar.iac.docker.tree.impl.KeyValuePairTreeImpl;
import org.sonar.iac.docker.tree.impl.LabelTreeImpl;
import org.sonar.iac.docker.tree.impl.MaintainerTreeImpl;
import org.sonar.iac.docker.tree.impl.StopSignalTreeImpl;
import org.sonar.iac.docker.tree.impl.WorkdirTreeImpl;
import org.sonar.iac.docker.tree.impl.PortTreeImpl;

public class TreeFactory {

  public FileTree file(Optional<List<InstructionTree>> instructions, Optional<SyntaxToken> spacing, SyntaxToken eof) {
    return new FileTreeImpl(instructions.or(Collections.emptyList()), eof);
  }

  public FromTree from(SyntaxToken keyword, Optional<KeyValuePairTree> platform, SyntaxToken image, Optional<AliasTree> alias) {
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
    return new PortTreeImpl(portToken, separatorToken, protocolToken.orNull());
  }

  public PortTree port(SyntaxToken portToken) {
    return new PortTreeImpl(portToken, null, null);
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

  public KeyValuePairTree key(SyntaxToken key) {
    return new KeyValuePairTreeImpl(key, null, null);
  }

  public KeyValuePairTree keyValuePair(SyntaxToken key, SyntaxToken value) {
    return new KeyValuePairTreeImpl(key, null, value);
  }

  public KeyValuePairTree keyValuePairEquals(SyntaxToken key, SyntaxToken equals, SyntaxToken value) {
    return new KeyValuePairTreeImpl(key, equals, value);
  }
}
