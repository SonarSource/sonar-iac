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
package org.sonar.iac.arm.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.OutputDeclaration;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.Statement;
import org.sonar.iac.arm.tree.impl.json.FileImpl;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.MappingTree;

public class ArmParser implements TreeParser<ArmTree> {

  @Nullable
  private InputFileContext inputFileContext;

  @Override
  public ArmTree parse(String source, @Nullable InputFileContext inputFileContext) {
    this.inputFileContext = inputFileContext;
    return convert(parseJson(source));
  }

  private FileTree parseJson(String source) {
    YamlParser yamlParser = new YamlParser();
    try {
      return yamlParser.parse(source, inputFileContext);
    } catch (Exception e) {
      String message = "Failed to parse";
      if (inputFileContext != null) {
        String filename = inputFileContext.inputFile.filename();
        message = message + " " + filename;
      }
      throw new ParseException(message, null, e.getMessage());
    }
  }

  private ArmTree convert(FileTree fileTree) {
    List<Statement> statements = new ArrayList<>();
    MappingTree document = (MappingTree) fileTree.documents().get(0);

    ResourceDeclarationConverter resourceConverter = new ResourceDeclarationConverter(inputFileContext);
    List<ResourceDeclaration> resources = resourceConverter.extractResourcesSequence(document)
      .map(resourceConverter::convertToResourceDeclaration)
      .collect(Collectors.toList());
    statements.addAll(resources);

    OutputDeclarationConverter outputConverter = new OutputDeclarationConverter(inputFileContext);
    List<OutputDeclaration> outputs = outputConverter.extractOutputsMapping(document)
      .map(outputConverter::convertOutputDeclaration)
      .collect(Collectors.toList());
    statements.addAll(outputs);

    ParameterDeclarationConverter parameterConverter = new ParameterDeclarationConverter(inputFileContext);
    List<Statement> params = parameterConverter.extractParametersSequence(document)
      .map(parameterConverter::convertParameters)
      .collect(Collectors.toList());
    statements.addAll(params);

    VariableDeclarationConverter variableConverter = new VariableDeclarationConverter(inputFileContext);
    List<Statement> variables = variableConverter.extractVariablesMapping(document)
      .map(variableConverter::convertVariableDeclaration)
      .collect(Collectors.toList());
    statements.addAll(variables);

    return new FileImpl(statements);
  }
}
