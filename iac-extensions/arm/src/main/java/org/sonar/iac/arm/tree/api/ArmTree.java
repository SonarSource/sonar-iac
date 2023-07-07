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
package org.sonar.iac.arm.tree.api;

import javax.annotation.CheckForNull;

import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.MetadataDeclaration;
import org.sonar.iac.arm.tree.api.bicep.TargetScopeDeclaration;
import org.sonar.iac.arm.tree.impl.json.PropertyImpl;
import org.sonar.iac.arm.tree.impl.json.ResourceGroupDeclarationImpl;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.sslr.grammar.GrammarRuleKey;

public interface ArmTree extends Tree {

  @CheckForNull
  ArmTree parent();

  boolean is(Kind... kind);

  Kind getKind();

  void setParent(ArmTree parent);

  enum Kind implements GrammarRuleKey {
    FILE(File.class),
    OUTPUT_DECLARATION(OutputDeclaration.class),
    PARAMETER_DECLARATION(ParameterDeclaration.class),
    RESOURCE_GROUP_DECLARATION(ResourceGroupDeclarationImpl.class),
    RESOURCE_DECLARATION(ResourceDeclaration.class),
    VARIABLE_DECLARATION(VariableDeclaration.class),
    IDENTIFIER(Identifier.class),
    PROPERTY(PropertyImpl.class),
    BOOLEAN_LITERAL(BooleanLiteral.class),
    NUMERIC_LITERAL(NumericLiteral.class),
    NULL_LITERAL(NullLiteral.class),
    STRING_LITERAL(StringLiteral.class),
    ARRAY_EXPRESSION(ArrayExpression.class),
    OBJECT_EXPRESSION(ObjectExpression.class),

    // Bicep specific,
    TARGET_SCOPE_DECLARATION(TargetScopeDeclaration.class),
    METADATA_DECLARATION(MetadataDeclaration.class),
    INTERPOLATED_STRING(InterpolatedString.class);

    private final Class<? extends ArmTree> associatedInterface;

    Kind(Class<? extends ArmTree> associatedInterface) {
      this.associatedInterface = associatedInterface;
    }

    public Class<? extends ArmTree> getAssociatedInterface() {
      return this.associatedInterface;
    }
  }
}
