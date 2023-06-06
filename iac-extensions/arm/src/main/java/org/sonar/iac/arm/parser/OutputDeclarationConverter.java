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

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.OutputDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.impl.json.OutputDeclarationImpl;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.TupleTree;

public class OutputDeclarationConverter extends ArmBaseConverter {

  public OutputDeclarationConverter(@Nullable InputFileContext inputFileContext) {
    super(inputFileContext);
  }

  public Stream<TupleTree> extractOutputsMapping(MappingTree document) {
    return document.elements().stream()
      .filter(filterOnField("outputs"))
      .map(TupleTree::value)
      .filter(MappingTree.class::isInstance)
      .map(MappingTree.class::cast)
      .map(MappingTree::elements)
      .flatMap(List::stream);
  }

  public OutputDeclaration convertOutputDeclaration(TupleTree tree) {
    Identifier name = toIdentifier(tree.key());
    StringLiteral type = PropertyUtils.get(tree.value(), "type").map(this::toStringLiteral).orElseThrow(() -> missingMandatoryAttributeError(tree, "type"));
    StringLiteral condition = PropertyUtils.get(tree.value(), "condition").map(this::toStringLiteral).orElse(null);
    StringLiteral value = PropertyUtils.get(tree.value(), "value").map(this::toStringLiteral).orElse(null);
    Optional<PropertyTree> copy = PropertyUtils.get(tree.value(), "copy");
    StringLiteral copyCount = copy.map(c -> extractProperty(c, "count")).orElse(null);
    StringLiteral copyInput = copy.map(c -> extractProperty(c, "input")).orElse(null);

    return new OutputDeclarationImpl(name, type, condition, copyCount, copyInput, value);
  }

  @CheckForNull
  private StringLiteral extractProperty(PropertyTree property, String name) {
    return PropertyUtils.get(property.value(), name)
      .map(this::toStringLiteral)
      .orElse(null);
  }
}
