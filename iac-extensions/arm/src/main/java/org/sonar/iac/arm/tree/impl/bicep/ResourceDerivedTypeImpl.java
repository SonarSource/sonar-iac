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

import java.util.List;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.ResourceDerivedType;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class ResourceDerivedTypeImpl extends AbstractArmTreeImpl implements ResourceDerivedType {

  private final SyntaxToken keyword;
  private final SyntaxToken lessThan;
  private final InterpolatedString typeReference;
  private final SyntaxToken greaterThan;

  public ResourceDerivedTypeImpl(SyntaxToken keyword, SyntaxToken lessThan, InterpolatedString typeReference, SyntaxToken greaterThan) {
    this.keyword = keyword;
    this.lessThan = lessThan;
    this.typeReference = typeReference;
    this.greaterThan = greaterThan;
  }

  @Override
  public SyntaxToken keyword() {
    return keyword;
  }

  @Override
  public InterpolatedString typeReference() {
    return typeReference;
  }

  @Override
  public List<Tree> children() {
    return List.of(keyword, lessThan, typeReference, greaterThan);
  }

  @Override
  public Kind getKind() {
    return Kind.RESOURCE_DERIVED_TYPE;
  }
}
