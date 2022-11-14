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
import org.sonar.iac.docker.tree.api.ExposeTree;
import org.sonar.iac.docker.tree.api.FileTree;
import org.sonar.iac.docker.tree.api.FromTree;
import org.sonar.iac.docker.tree.api.InstructionTree;
import org.sonar.iac.docker.tree.api.MaintainerTree;
import org.sonar.iac.docker.tree.api.StopSignalTree;
import org.sonar.iac.docker.tree.api.PortTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;
import org.sonar.iac.docker.tree.api.WorkdirTree;
import org.sonar.iac.docker.tree.impl.ExposeTreeImpl;
import org.sonar.iac.docker.tree.impl.FileTreeImpl;
import org.sonar.iac.docker.tree.impl.FromTreeImpl;
import org.sonar.iac.docker.tree.impl.MaintainerTreeImpl;
import org.sonar.iac.docker.tree.impl.StopSignalTreeImpl;
import org.sonar.iac.docker.tree.impl.WorkdirTreeImpl;
import org.sonar.iac.docker.tree.impl.PortTreeImpl;

public class TreeFactory {

  public FileTree file(Optional<List<InstructionTree>> instructions, Optional<SyntaxToken> spacing, SyntaxToken eof) {
    return new FileTreeImpl(instructions.or(Collections.emptyList()), eof);
  }

  public FromTree from(SyntaxToken token) {
    return new FromTreeImpl();
  }

  public MaintainerTree maintainer(SyntaxToken maintainerToken, List<SyntaxToken> authorsToken) {
    return new MaintainerTreeImpl(maintainerToken, authorsToken);
  }

  public SyntaxToken argument(SyntaxToken token) {
    return token;
  }

  public StopSignalTree stopSignal(SyntaxToken token, SyntaxToken tokenValue) {
    return new StopSignalTreeImpl(token, tokenValue);
  }

  public WorkdirTree workdir(SyntaxToken token, List<SyntaxToken> values) {
    return new WorkdirTreeImpl(token, values);
  }

  public ExposeTree expose(SyntaxToken exposeToken, List<PortTree> ports) {
    return new ExposeTreeImpl(exposeToken, ports);
  }

  public PortTree port(SyntaxToken portToken) {
    return new PortTreeImpl(portToken);
  }
}
