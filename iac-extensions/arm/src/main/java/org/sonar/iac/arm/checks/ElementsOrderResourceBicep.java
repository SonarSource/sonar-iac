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
package org.sonar.iac.arm.checks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sonar.iac.arm.tree.api.FunctionCall;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.bicep.Decorator;
import org.sonar.iac.arm.tree.impl.bicep.ResourceDeclarationImpl;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.impl.TextRanges;

public class ElementsOrderResourceBicep implements IacCheck {

  private static final String MESSAGE = "Reorder the elements to match the recommended order.";
  private static final String MESSAGE_DECORATOR = "Reorder the decorators to match the recommended order.";

  private static final Map<String, Integer> decoratorsOrder = Map.of(
    "description", 1,
    "batchSize", 2);

  private static final Map<String, Integer> elementsOrder = new HashMap<>();
  private static final int DEFAULT_ORDER_FOR_UNKNOWN_PROPERTY = 20;

  static {
    elementsOrder.put("parent", 0);
    elementsOrder.put("scope", 1);
    elementsOrder.put("name", 2);
    elementsOrder.put("location", 3);
    // extendedLocation has the same weight as location
    elementsOrder.put("extendedLocation", 3);
    elementsOrder.put("zones", 4);
    elementsOrder.put("sku", 5);
    elementsOrder.put("kind", 6);
    elementsOrder.put("scale", 7);
    elementsOrder.put("plan", 8);
    elementsOrder.put("identity", 9);
    elementsOrder.put("dependsOn", 10);
    elementsOrder.put("tags", 11);
    // between tags and properties is a place for elements not defined here
    elementsOrder.put("properties", 100);
  }

  @Override
  public void initialize(InitContext init) {
    init.register(ResourceDeclarationImpl.class, ElementsOrderResourceBicep::checkResource);
    init.register(ResourceDeclarationImpl.class, ElementsOrderResourceBicep::checkResourceDecorators);
  }

  private static void checkResource(CheckContext checkContext, ResourceDeclarationImpl resourceDeclaration) {
    var prevIndex = 0;
    for (Property property : resourceDeclaration.resourceProperties()) {
      var index = elementsOrder.getOrDefault(property.key().value(), DEFAULT_ORDER_FOR_UNKNOWN_PROPERTY);
      if (index < prevIndex) {
        checkContext.reportIssue(property.key(), MESSAGE);
        break;
      }
      prevIndex = index;
    }
  }

  private static void checkResourceDecorators(CheckContext checkContext, ResourceDeclarationImpl resourceDeclaration) {
    var prevIndex = 0;
    for (Decorator decorator : resourceDeclaration.decorators()) {
      var index = decoratorsOrder.getOrDefault(((FunctionCall) decorator.expression()).name().value(), DEFAULT_ORDER_FOR_UNKNOWN_PROPERTY);
      if (index < prevIndex) {
        var textRange = TextRanges.merge(List.of(
          decorator.keyword().textRange(),
          ((FunctionCall) decorator.expression()).name().textRange()));
        checkContext.reportIssue(textRange, MESSAGE_DECORATOR);
        break;
      }
      prevIndex = index;
    }
  }
}
