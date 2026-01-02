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
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.bicep.Declaration;
import org.sonar.iac.arm.tree.api.bicep.HasKeyword;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public abstract class AbstractDeclaration extends AbstractArmTreeImpl implements Declaration, HasKeyword {
  protected final SyntaxToken keyword;
  protected final Identifier identifier;
  protected final SyntaxToken equals;
  protected final Expression expression;

  protected AbstractDeclaration(SyntaxToken keyword, Identifier identifier, SyntaxToken equals, Expression expression) {
    this.keyword = keyword;
    this.identifier = identifier;
    this.equals = equals;
    this.expression = expression;
  }

  @Override
  public List<Tree> children() {
    return List.of(keyword, identifier, equals, expression);
  }

  @Override
  public SyntaxToken keyword() {
    return keyword;
  }

  @Override
  public Identifier declaratedName() {
    return identifier;
  }
}
