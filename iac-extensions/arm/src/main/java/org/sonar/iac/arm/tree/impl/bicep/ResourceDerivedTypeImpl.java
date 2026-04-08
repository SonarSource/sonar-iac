/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.ArmHelper;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.ResourceDerivedType;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class ResourceDerivedTypeImpl extends AbstractArmTreeImpl implements ResourceDerivedType {

  @Nullable
  private final Identifier namespace;
  @Nullable
  private final SyntaxToken dot;
  private final SyntaxToken keyword;
  private final SyntaxToken lessThan;
  private final InterpolatedString typeReference;
  private final SyntaxToken greaterThan;

  public ResourceDerivedTypeImpl(@Nullable Identifier namespace, @Nullable SyntaxToken dot,
    SyntaxToken keyword, SyntaxToken lessThan, InterpolatedString typeReference, SyntaxToken greaterThan) {
    this.namespace = namespace;
    this.dot = dot;
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
    var children = new ArrayList<Tree>();
    ArmHelper.addChildrenIfPresent(children, namespace);
    ArmHelper.addChildrenIfPresent(children, dot);
    children.add(keyword);
    children.add(lessThan);
    children.add(typeReference);
    children.add(greaterThan);
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.RESOURCE_DERIVED_TYPE;
  }
}
