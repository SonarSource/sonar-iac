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
package org.sonar.iac.arm.visitors;

import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.bicep.AmbientTypeReference;
import org.sonar.iac.arm.tree.api.bicep.FunctionCall;
import org.sonar.iac.arm.tree.impl.bicep.AbstractDeclaration;
import org.sonar.iac.arm.tree.impl.bicep.BooleanLiteralImpl;
import org.sonar.iac.arm.tree.impl.bicep.DecoratorImpl;
import org.sonar.iac.arm.tree.impl.bicep.ForExpressionImpl;
import org.sonar.iac.arm.tree.impl.bicep.ForVariableBlockImpl;
import org.sonar.iac.arm.tree.impl.bicep.FunctionDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.IfConditionImpl;
import org.sonar.iac.arm.tree.impl.bicep.ImportDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.InterpolatedStringImpl;
import org.sonar.iac.arm.tree.impl.bicep.ModuleDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.MultilineStringImpl;
import org.sonar.iac.arm.tree.impl.bicep.NullLiteralImpl;
import org.sonar.iac.arm.tree.impl.bicep.NumericLiteralImpl;
import org.sonar.iac.arm.tree.impl.bicep.OutputDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.ParameterDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.PropertyImpl;
import org.sonar.iac.arm.tree.impl.bicep.ResourceDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.StringCompleteImpl;
import org.sonar.iac.arm.tree.impl.bicep.StringLiteralImpl;
import org.sonar.iac.arm.tree.impl.bicep.TargetScopeDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.TypeDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.importdecl.ImportAsClause;
import org.sonar.iac.arm.tree.impl.bicep.importdecl.ImportWithClause;
import org.sonar.iac.arm.tree.impl.bicep.variable.LocalVariableImpl;
import org.sonar.iac.common.extension.visitors.SyntaxHighlightingVisitor;

import static org.sonar.api.batch.sensor.highlighting.TypeOfText.ANNOTATION;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.CONSTANT;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD_LIGHT;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.STRING;

public class BicepHighlightingVisitor extends SyntaxHighlightingVisitor {
  @Override
  protected void languageSpecificHighlighting() {
    register(ResourceDeclarationImpl.class, (ctx, tree) -> {
      highlight(tree.keyword(), KEYWORD);
      highlight(tree.symbolicName(), KEYWORD_LIGHT);
      highlight(tree.getExisting(), KEYWORD);
    });
    register(DecoratorImpl.class, (ctx, tree) -> {
      highlight(tree.keyword(), ANNOTATION);
      Expression expression = tree.expression();
      if (expression instanceof FunctionCall) {
        highlight(((FunctionCall) expression).name(), ANNOTATION);
      }
    });
    register(TargetScopeDeclarationImpl.class, (ctx, tree) -> highlight(tree.keyword(), KEYWORD));
    register(ImportDeclarationImpl.class, (ctx, tree) -> highlight(tree.keyword(), KEYWORD));
    register(ImportWithClause.class, (ctx, tree) -> highlight(tree.keyword(), KEYWORD));
    register(ImportAsClause.class, (ctx, tree) -> highlight(tree.keyword(), KEYWORD));
    register(ParameterDeclarationImpl.class, (ctx, tree) -> {
      highlight(tree.keyword(), KEYWORD);
      highlight(tree.identifier(), KEYWORD_LIGHT);
    });
    register(TypeDeclarationImpl.class, (ctx, tree) -> {
      highlight(tree.keyword(), KEYWORD);
      highlight(tree.name(), KEYWORD_LIGHT);
    });
    register(AbstractDeclaration.class, (ctx, tree) -> {
      highlight(tree.keyword(), KEYWORD);
      highlight(tree.identifier(), KEYWORD_LIGHT);
    });
    register(ModuleDeclarationImpl.class, (ctx, tree) -> {
      highlight(tree.keyword(), KEYWORD);
      highlight(tree.name(), KEYWORD_LIGHT);
    });
    register(OutputDeclarationImpl.class, (ctx, tree) -> {
      highlight(tree.keyword(), KEYWORD);
      highlight(tree.name(), KEYWORD_LIGHT);
      highlight(tree.type(), KEYWORD);
    });
    register(FunctionDeclarationImpl.class, (ctx, tree) -> {
      highlight(tree.keyword(), KEYWORD);
      highlight(tree.name(), KEYWORD_LIGHT);
    });
    register(IfConditionImpl.class, (ctx, tree) -> highlight(tree.keyword(), KEYWORD));
    register(ForExpressionImpl.class, (ctx, tree) -> {
      highlight(tree.forKeyword(), KEYWORD);
      highlight(tree.inKeyword(), KEYWORD);
    });
    register(ForVariableBlockImpl.class, (ctx, tree) -> highlight(tree.itemIdentifier(), ANNOTATION));
    register(LocalVariableImpl.class, (ctx, tree) -> highlight(tree.identifier(), ANNOTATION));
    register(PropertyImpl.class, (ctx, tree) -> highlight(tree.key(), ANNOTATION));

    register(AmbientTypeReference.class, (ctx, tree) -> highlight(tree, KEYWORD));

    register(BooleanLiteralImpl.class, (ctx, tree) -> highlight(tree.children().get(0), CONSTANT));
    register(NullLiteralImpl.class, (ctx, tree) -> highlight(tree.children().get(0), CONSTANT));
    register(NumericLiteralImpl.class, (ctx, tree) -> highlight(tree.children().get(0), CONSTANT));

    register(InterpolatedStringImpl.class, (ctx, tree) -> highlight(tree, STRING));
    register(MultilineStringImpl.class, (ctx, tree) -> highlight(tree, STRING));
    register(StringCompleteImpl.class, (ctx, tree) -> highlight(tree, STRING));
    register(StringLiteralImpl.class, (ctx, tree) -> highlight(tree, STRING));
  }
}
