/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.arm.visitors;

import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.iac.arm.tree.ArmTreeUtils;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.FunctionCall;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.bicep.AmbientTypeReference;
import org.sonar.iac.arm.tree.api.bicep.Declaration;
import org.sonar.iac.arm.tree.api.bicep.Decorator;
import org.sonar.iac.arm.tree.api.bicep.ForExpression;
import org.sonar.iac.arm.tree.api.bicep.ForVariableBlock;
import org.sonar.iac.arm.tree.api.bicep.HasKeyword;
import org.sonar.iac.arm.tree.api.bicep.HasToken;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.MultilineString;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.variable.LocalVariable;
import org.sonar.iac.arm.tree.impl.json.FileImpl;
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
    register(Decorator.class, (ctx, tree) -> {
      var expression = tree.expression();
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
      var identifier = tree.symbolicName();
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
  }

  private <T extends ArmTree> void registerTree(Class<T> cls, TypeOfText type) {
    register(cls, (ctx, tree) -> {
      if (ArmTreeUtils.getRootNode(tree) instanceof FileImpl) {
        // don't highlight Bicep parts in JSON file
        return;
      }
      highlight(tree, type);
    });
  }
}
