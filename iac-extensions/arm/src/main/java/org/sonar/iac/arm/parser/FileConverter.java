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
package org.sonar.iac.arm.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.OutputDeclaration;
import org.sonar.iac.arm.tree.api.ParameterDeclaration;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.Statement;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.VariableDeclaration;
import org.sonar.iac.arm.tree.impl.json.FileImpl;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.MappingTree;

public class FileConverter extends ArmJsonBaseConverter {
  public FileConverter(@Nullable InputFileContext inputFileContext) {
    super(inputFileContext);
  }

  public File convertFile(FileTree fileTree) {
    MappingTree document = (MappingTree) fileTree.documents().get(0);
    StringLiteral targetScope = toStringLiteralOrNull(document, "$schema");

    ResourceDeclarationConverter resourceConverter = new ResourceDeclarationConverter(inputFileContext);
    List<ResourceDeclaration> resources = Stream.concat(
      resourceConverter.extractResourcesSequence(document).map(resourceConverter::convertToResourceDeclaration),
      resourceConverter.extractResourcesTuples(document).map(resourceConverter::convertToResourceDeclaration))
      .toList();
    List<Statement> statements = new ArrayList<>(resources);

    OutputDeclarationConverter outputConverter = new OutputDeclarationConverter(inputFileContext);
    List<OutputDeclaration> outputs = outputConverter.extractOutputsMapping(document)
      .map(outputConverter::convertOutputDeclaration)
      .toList();
    statements.addAll(outputs);

    ParameterDeclarationConverter parameterConverter = new ParameterDeclarationConverter(inputFileContext);
    List<ParameterDeclaration> params = parameterConverter.extractParametersSequence(document)
      .map(parameterConverter::convertParameters)
      .toList();
    statements.addAll(params);

    VariableDeclarationConverter variableConverter = new VariableDeclarationConverter(inputFileContext);
    List<VariableDeclaration> variables = variableConverter.extractVariablesMapping(document)
      .map(variableConverter::convertVariableDeclaration)
      .toList();
    statements.addAll(variables);

    return new FileImpl(targetScope, statements, document);
  }
}
