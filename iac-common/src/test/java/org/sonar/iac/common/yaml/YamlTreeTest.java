/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
package org.sonar.iac.common.yaml;

import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class YamlTreeTest {

  protected static FileTree parse(String source) {
    YamlParser parser = new YamlParser();
    return parser.parse(source, null);
  }

  protected static <T extends YamlTree> T parse(String source, Class<T> clazz) {
    FileTree fileTree = parse(source);
    assertThat(fileTree.documents()).as("Parsed source code contains not a single document").hasSize(1);
    YamlTree rootTree = fileTree.documents().get(0);
    assertThat(rootTree).isInstanceOf(clazz);
    return (T) rootTree;
  }

  protected static TupleTree parseTuple(String source) {
    return parse(source, MappingTree.class).elements().get(0);
  }

}
