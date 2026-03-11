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

import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.bicep.Declaration;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;

public abstract class AbstractDeclarationImpl<T extends Expression> extends AbstractArmTreeImpl implements Declaration {
  protected final SyntaxToken keyword;
  protected final Identifier name;
  protected final SyntaxToken equalsSign;
  protected final T body;

  protected AbstractDeclarationImpl(SyntaxToken keyword, Identifier name, SyntaxToken equalsSign, T body) {
    this.keyword = keyword;
    this.name = name;
    this.equalsSign = equalsSign;
    this.body = body;
  }

  public SyntaxToken keyword() {
    return keyword;
  }

  public Identifier declaratedName() {
    return name;
  }

  public T body() {
    return body;
  }
}
