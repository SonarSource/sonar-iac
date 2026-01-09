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
import java.util.Locale;
import java.util.Map;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;

public abstract class AbstractElementsOrderResourceCheck implements IacCheck {

  protected static final String MESSAGE = "Reorder the elements to match the recommended order.";

  protected abstract List<Map<String, Integer>> getElementsOrderSets();

  protected void checkResource(CheckContext checkContext, ResourceDeclaration resourceDeclaration) {
    var properties = resourceDeclaration.resourceProperties();
    Property firstViolatingProperty = null;

    for (Map<String, Integer> elementsOrder : getElementsOrderSets()) {
      var violation = findFirstViolation(properties, elementsOrder);
      if (violation == null) {
        // Matches this order set, no issue
        return;
      }
      if (firstViolatingProperty == null) {
        firstViolatingProperty = violation;
      }
    }

    if (firstViolatingProperty != null) {
      checkContext.reportIssue(firstViolatingProperty.key(), MESSAGE);
    }
  }

  private static Property findFirstViolation(List<Property> properties, Map<String, Integer> elementsOrder) {
    var prevIndex = 0;
    for (Property property : properties) {
      var index = elementsOrder.getOrDefault(property.key().value().toLowerCase(Locale.ROOT), ElementOrders.DEFAULT_ORDER_FOR_UNKNOWN_PROPERTY);
      if (index < prevIndex) {
        return property;
      }
      prevIndex = index;
    }
    return null;
  }
}
