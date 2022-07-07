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
package org.sonar.iac.kubernetes.symbols;

import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.TupleTree;

import static org.mockito.Mockito.mock;

public abstract class AbstractSymbolTest {

  private final YamlParser parser = new YamlParser();
  CheckContext ctx = mock(CheckContext.class);

  BlockSymbol parentBlock = TupleBlockSymbol.fromPresent(ctx, parse("parent_block:\n  foo: bar"), "parent", null);

  protected FileTree parse(String source) {
    return parser.parse(source, null);
  }

  protected TupleTree parseTupleBlock(String source) {
    return ((MappingTree) parse(source).root()).elements().get(0);
  }

}
