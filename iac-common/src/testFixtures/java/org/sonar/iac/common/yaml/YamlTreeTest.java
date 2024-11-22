/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
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
