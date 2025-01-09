/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.bicep.AsClause;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class AsClauseImpl extends AbstractArmTreeImpl implements AsClause {
  private final SyntaxToken keyword;
  private final Identifier alias;

  public AsClauseImpl(SyntaxToken keyword, Identifier alias) {
    this.keyword = keyword;
    this.alias = alias;
  }

  public List<Tree> children() {
    return List.of(keyword, alias);
  }

  @Override
  public SyntaxToken keyword() {
    return keyword;
  }

  @Override
  public Identifier alias() {
    return alias;
  }
}