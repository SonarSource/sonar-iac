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
package org.sonar.iac.jvmframeworkconfig.parser.yaml;

import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.snakeyaml.engine.v2.nodes.Node;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.IacYamlConverter;
import org.sonar.iac.common.yaml.IacYamlParser;
import org.sonar.iac.jvmframeworkconfig.tree.api.File;

public class JvmFrameworkConfigYamlParser implements IacYamlParser<File> {

  private final IacYamlConverter<File, Stream<JvmFrameworkConfigYamlConverter.TupleBuilder>> converter;
  private final JvmFrameworkConfigYamlPreprocessor preprocessor = new JvmFrameworkConfigYamlPreprocessor();

  public JvmFrameworkConfigYamlParser() {
    this(new JvmFrameworkConfigYamlConverter());
  }

  public JvmFrameworkConfigYamlParser(IacYamlConverter<File, Stream<JvmFrameworkConfigYamlConverter.TupleBuilder>> converter) {
    this.converter = converter;
  }

  @Override
  public File parse(String source, @Nullable InputFileContext inputFileContext) {
    return IacYamlParser.super.parse(preprocessor.preprocess(source), inputFileContext);
  }

  @Override
  public File convert(List<Node> nodes) {
    return converter.convertFile(nodes);
  }
}