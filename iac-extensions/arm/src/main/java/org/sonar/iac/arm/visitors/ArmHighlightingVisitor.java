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
package org.sonar.iac.arm.visitors;

import java.util.function.Function;
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
import org.sonar.iac.common.api.tree.HasTextRange;
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

    // HasToken literals also occur in JSON files, as they are sub-parsed from expression strings there
    registerBicepHighlightingOnly(HasToken.class, HasToken::token, CONSTANT);

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
      if (!(tree instanceof Declaration)) {
        var identifier = tree.symbolicName();
        if (identifier != null) {
          highlight(identifier, KEYWORD_LIGHT);
        }
      }
      SyntaxToken existing = tree.existing();
      if (existing != null) {
        highlight(existing, KEYWORD);
      }
    });

    registerBicepHighlightingOnly(AmbientTypeReference.class, KEYWORD);
    registerBicepHighlightingOnly(InterpolatedString.class, STRING);
    registerBicepHighlightingOnly(MultilineString.class, STRING);
  }

  @Override
  public void highlightComments(Tree tree) {
    // comments of Bicep tokens sub-parsed from JSON strings keep their position inside the parsed string, see registerBicepHighlightingOnly
    if (tree instanceof SyntaxToken token && ArmTreeUtils.getRootNode(token) instanceof FileImpl) {
      return;
    }
    super.highlightComments(tree);
  }

  private <T extends ArmTree> void registerBicepHighlightingOnly(Class<T> cls, TypeOfText type) {
    registerBicepHighlightingOnly(cls, tree -> tree, type);
  }

  /**
   * ARM JSON string values are sub-parsed with the Bicep grammar, so Bicep trees also occur in JSON files. Their text ranges are only
   * approximations of the position in the JSON file, hence they must not be highlighted there otherwise they would lead to failures.
   */
  private <T extends ArmTree> void registerBicepHighlightingOnly(Class<T> cls, Function<T, HasTextRange> resolveTreeToHighlight, TypeOfText type) {
    register(cls, (ctx, tree) -> {
      if (ArmTreeUtils.getRootNode(tree) instanceof FileImpl) {
        return;
      }
      highlight(resolveTreeToHighlight.apply(tree), type);
    });
  }
}
