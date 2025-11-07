/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
import java.util.Collections;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.bicep.AsClause;
import org.sonar.iac.arm.tree.api.bicep.Decorator;
import org.sonar.iac.arm.tree.api.bicep.ExtensionDeclaration;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.WithClause;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class ExtensionDeclarationImpl extends AbstractArmTreeImpl implements ExtensionDeclaration {
  private final List<Decorator> decorators;
  private final SyntaxToken keyword;
  private final Expression specification;
  @Nullable
  private final WithClause withClause;
  @Nullable
  private final AsClause asClause;

  public ExtensionDeclarationImpl(
    List<Decorator> decorators,
    SyntaxToken keyword,
    Expression specification,
    @Nullable WithClause withClause,
    @Nullable AsClause asClause) {
    this.decorators = decorators;
    this.keyword = keyword;
    this.specification = specification;
    this.withClause = withClause;
    this.asClause = asClause;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.addAll(decorators);
    children.add(keyword);
    children.add(specification);
    if (withClause != null) {
      children.add(withClause);
    }
    if (asClause != null) {
      children.add(asClause);
    }
    return children;
  }

  @Override
  public List<Decorator> decorators() {
    return Collections.unmodifiableList(decorators);
  }

  @Override
  public SyntaxToken keyword() {
    return keyword;
  }

  @Override
  public Expression specification() {
    return specification;
  }

  @CheckForNull
  @Override
  public WithClause withClause() {
    return withClause;
  }

  @CheckForNull
  @Override
  public AsClause asClause() {
    return asClause;
  }
}
