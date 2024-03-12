/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.arm.visitors;

import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.OutputDeclaration;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.AmbientTypeReference;
import org.sonar.iac.arm.tree.api.bicep.Declaration;
import org.sonar.iac.arm.tree.api.bicep.Decorator;
import org.sonar.iac.arm.tree.api.bicep.ForExpression;
import org.sonar.iac.arm.tree.api.bicep.ForVariableBlock;
import org.sonar.iac.arm.tree.api.bicep.FunctionCall;
import org.sonar.iac.arm.tree.api.bicep.HasKeyword;
import org.sonar.iac.arm.tree.api.bicep.HasToken;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.MultilineString;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.variable.LocalVariable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.yaml.visitors.YamlHighlightingVisitor;

import static org.sonar.api.batch.sensor.highlighting.TypeOfText.ANNOTATION;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.CONSTANT;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD_LIGHT;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.STRING;

public class ArmHighlightingVisitor extends YamlHighlightingVisitor {
  @Override
  protected void languageSpecificHighlighting() {
    register(ForVariableBlock.class, (ctx, tree) -> highlight(tree.itemIdentifier(), ANNOTATION));
    register(LocalVariable.class, (ctx, tree) -> highlight(tree.identifier(), ANNOTATION));
    register(Property.class, (ctx, tree) -> highlight(tree.key(), ANNOTATION));

    register(HasToken.class, (ctx, tree) -> highlight(tree.token(), CONSTANT));

    register(HasKeyword.class, (ctx, tree) -> highlight(tree.keyword(), KEYWORD));
    register(OutputDeclaration.class, (ctx, tree) -> highlight(tree.type(), KEYWORD));
    register(Decorator.class, (ctx, tree) -> {
      Expression expression = tree.expression();
      if (expression instanceof FunctionCall functionCall) {
        highlight(functionCall.name(), KEYWORD);
      }
    });
    register(ForExpression.class, (ctx, tree) -> {
      highlight(tree.forKeyword(), KEYWORD);
      highlight(tree.inKeyword(), KEYWORD);
    });

    register(Declaration.class, (ctx, tree) -> highlight(tree.declaratedName(), KEYWORD_LIGHT));

    register(ResourceDeclaration.class, (ctx, tree) -> {
      Identifier identifier = tree.symbolicName();
      if (identifier != null) {
        highlight(identifier, KEYWORD_LIGHT);
      }
      SyntaxToken existing = tree.existing();
      if (existing != null) {
        highlight(existing, KEYWORD);
      }
    });

    registerTree(AmbientTypeReference.class, KEYWORD);
    registerTree(InterpolatedString.class, STRING);
    registerTree(MultilineString.class, STRING);
    registerTree(StringLiteral.class, STRING);
  }

  private <T extends Tree> void registerTree(Class<T> cls, TypeOfText type) {
    register(cls, (ctx, tree) -> highlight(tree, type));
  }
}
