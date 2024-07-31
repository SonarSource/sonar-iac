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
package org.sonar.iac.docker.checks.utils;

import java.util.ArrayList;
import java.util.List;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.ArgumentList;
import org.sonar.iac.docker.tree.impl.ArgumentImpl;
import org.sonar.iac.docker.tree.impl.LiteralImpl;
import org.sonar.iac.docker.tree.impl.ShellFormImpl;
import org.sonar.iac.docker.tree.impl.SyntaxTokenImpl;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

public class CommandDetectorTestFactory {

  public static List<ArgumentResolution> buildArgumentList(String... strs) {
    List<ArgumentResolution> arguments = new ArrayList<>();
    int offset = 0;
    for (String str : strs) {
      Argument arg = buildArgument(str, range(1, offset, str));
      offset += str.length() + 1;
      arguments.add(ArgumentResolution.ofWithoutStrippingQuotes(arg));
    }
    return arguments;
  }

  public static Argument buildArgument(String str, TextRange range) {
    Argument arg = new ArgumentImpl(List.of(new LiteralImpl(new SyntaxTokenImpl(str, range, List.of()))));
    arg.expressions().forEach(e -> e.setParent(arg));
    ArgumentList shellForm = new ShellFormImpl(List.of(arg));
    arg.setParent(shellForm);
    return arg;
  }
}
