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
package org.sonar.iac.arm.tree.impl.bicep;

import java.util.ArrayList;
import java.util.List;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.ObjectType;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.SeparatedList;
import org.sonar.iac.common.api.tree.Tree;

public class ObjectTypeImpl extends AbstractArmTreeImpl implements ObjectType {
  private final SyntaxToken openingCurlyBracket;
  private final SeparatedList<ArmTree, SyntaxToken> objectTypePropertiesWithSeparators;
  private final SyntaxToken closingCurlyBracket;

  public ObjectTypeImpl(SyntaxToken openingCurlyBracket, SeparatedList<ArmTree, SyntaxToken> objectTypePropertiesWithSeparators, SyntaxToken closingCurlyBracket) {
    this.openingCurlyBracket = openingCurlyBracket;
    this.objectTypePropertiesWithSeparators = objectTypePropertiesWithSeparators;
    this.closingCurlyBracket = closingCurlyBracket;
  }

  @Override
  public List<ArmTree> properties() {
    return objectTypePropertiesWithSeparators.elements();
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(openingCurlyBracket);
    children.addAll(objectTypePropertiesWithSeparators.elementsAndSeparators());
    children.add(closingCurlyBracket);
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.OBJECT_TYPE;
  }
}
