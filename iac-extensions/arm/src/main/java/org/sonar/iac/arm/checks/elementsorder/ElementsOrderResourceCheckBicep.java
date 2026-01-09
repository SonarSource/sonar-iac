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
package org.sonar.iac.arm.checks.elementsorder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.CheckForNull;
import org.sonar.iac.arm.checks.ElementsOrderResourceCheck;
import org.sonar.iac.arm.tree.ArmTreeUtils;
import org.sonar.iac.arm.tree.api.FunctionCall;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.Variable;
import org.sonar.iac.arm.tree.api.bicep.Decorator;
import org.sonar.iac.arm.tree.api.bicep.MemberExpression;
import org.sonar.iac.arm.tree.impl.bicep.ResourceDeclarationImpl;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;

/**
 * It is a sub check of S6956, see {@link ElementsOrderResourceCheck}.
 */
public class ElementsOrderResourceCheckBicep extends AbstractElementsOrderResourceCheck {

  private static final String MESSAGE_DECORATOR = "Reorder the decorators to match the recommended order.";

  private static final Map<String, Integer> DECORATORS_ORDER = Map.ofEntries(
    Map.entry("description", 1),
    Map.entry("sys.description", 1),
    Map.entry("batchSize", 2),
    Map.entry("sys.batchSize", 2));

  @Override
  public void initialize(InitContext init) {
    init.register(ResourceDeclarationImpl.class, this::checkResource);
    init.register(ResourceDeclarationImpl.class, ElementsOrderResourceCheckBicep::checkResourceDecorators);
  }

  @Override
  protected List<Map<String, Integer>> getElementsOrderSets() {
    return ElementOrders.BICEP_ELEMENTS_ORDER_SETS;
  }

  private static void checkResourceDecorators(CheckContext checkContext, ResourceDeclarationImpl resourceDeclaration) {
    var prevIndex = 0;
    for (Decorator decorator : resourceDeclaration.decorators()) {
      var valueAndHighlight = toValueAndHighlight(decorator);
      if (valueAndHighlight == null) {
        continue;
      }
      var index = DECORATORS_ORDER.getOrDefault(valueAndHighlight.value, ElementOrders.DEFAULT_ORDER_FOR_UNKNOWN_PROPERTY);
      if (index < prevIndex) {
        var textRange = TextRanges.merge(
          decorator.keyword().textRange(),
          valueAndHighlight.highlight);
        checkContext.reportIssue(textRange, MESSAGE_DECORATOR);
        break;
      }
      prevIndex = index;
    }
  }

  @CheckForNull
  private static ValueAndHighlight toValueAndHighlight(Decorator decorator) {
    if (decorator.expression() instanceof FunctionCall functionCall) {
      var identifier = ArmTreeUtils.functionCallNameOrNull(functionCall);
      return new ValueAndHighlight(identifier.value(), identifier.textRange());
    } else if (decorator.expression() instanceof MemberExpression memberExpression) {
      var prefix = Optional.ofNullable(memberExpression.memberAccess())
        .filter(Variable.class::isInstance)
        .map(it -> ((Variable) it).identifier())
        .filter(Identifier.class::isInstance)
        .map(it -> ((Identifier) it).value())
        .map(it -> it + memberExpression.separatingToken().value())
        .orElse(null);

      var identifier = Optional.ofNullable(memberExpression.expression())
        .map(ArmTreeUtils::functionCallNameOrNull)
        .orElse(null);

      if (identifier != null && prefix != null) {
        var highlight = TextRanges.merge(
          memberExpression.memberAccess().textRange(),
          memberExpression.separatingToken().textRange(),
          identifier.textRange());
        return new ValueAndHighlight(prefix + identifier.value(), highlight);
      }
    }
    return null;
  }

  record ValueAndHighlight(String value, TextRange highlight) {
  }
}
