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

import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.OutputDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.impl.json.OutputDeclarationImpl;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.TupleTree;

public class OutputDeclarationConverter extends ArmBaseConverter {

  public OutputDeclarationConverter(@Nullable InputFileContext inputFileContext) {
    super(inputFileContext);
  }

  public Stream<TupleTree> extractOutputsMapping(MappingTree document) {
    return extractMappingToTupleTreeOnField(document, "outputs");
  }

  public OutputDeclaration convertOutputDeclaration(TupleTree tree) {
    Identifier name = toIdentifier(tree.key());
    StringLiteral type = toStringLiteralOrException(tree, "type");
    StringLiteral condition = toStringLiteralOrNull(tree.value(), "condition");
    Expression value = toExpressionOrNull(tree, "value");
    StringLiteral copyCount = toNestedStringLiteralOrNull(tree.value(), "copy", "count");
    StringLiteral copyInput = toNestedStringLiteralOrNull(tree.value(), "copy", "input");

    return new OutputDeclarationImpl(name, type, condition, copyCount, copyInput, value);
  }
}
