/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import java.util.List;
import org.snakeyaml.engine.v2.nodes.Node;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

public class YamlParser implements IacYamlParser<FileTree> {

  private final IacYamlConverter<FileTree, YamlTree> converter;

  public YamlParser() {
    this(new YamlConverter());
  }

  public YamlParser(IacYamlConverter<FileTree, YamlTree> converter) {
    this.converter = converter;
  }

  @Override
  public FileTree convert(List<Node> nodes) {
    return converter.convertFile(nodes);
  }
}
