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
import org.sonar.iac.arm.tree.api.bicep.MetadataDeclaration;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;

public class MetadataDeclarationImpl extends AbstractDeclaration implements MetadataDeclaration {
  public MetadataDeclarationImpl(SyntaxToken keyword, Identifier identifier, SyntaxToken equals, Expression expression) {
    super(keyword, identifier, equals, expression);
  }

  @Override
  public Kind getKind() {
    return Kind.METADATA_DECLARATION;
  }

  @Override
  public Expression value() {
    return expression;
  }
}
