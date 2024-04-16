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
package org.sonar.iac.arm.checks.elementsorder;

import java.util.HashMap;
import java.util.Map;
import org.sonar.iac.arm.tree.api.FunctionCall;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.bicep.Decorator;
import org.sonar.iac.arm.tree.impl.bicep.ResourceDeclarationImpl;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.impl.TextRanges;

/**
 * It is a sub check of S6956, see {@link org.sonar.iac.arm.checks.ElementsOrderCheck}.
 */
public class ElementsOrderResourceBicep implements IacCheck {

  private static final String MESSAGE = "Reorder the elements to match the recommended order.";
  private static final String MESSAGE_DECORATOR = "Reorder the decorators to match the recommended order.";

  private static final Map<String, Integer> DECORATORS_ORDER = new HashMap<>();

  private static final Map<String, Integer> ELEMENTS_ORDER = new HashMap<>();
  private static final int DEFAULT_ORDER_FOR_UNKNOWN_PROPERTY = 20;

  static {
    ELEMENTS_ORDER.put("parent", 0);
    ELEMENTS_ORDER.put("scope", 1);
    ELEMENTS_ORDER.put("name", 2);
    ELEMENTS_ORDER.put("location", 3);
    // extendedLocation has the same weight as location
    ELEMENTS_ORDER.put("extendedLocation", 3);
    ELEMENTS_ORDER.put("zones", 4);
    ELEMENTS_ORDER.put("sku", 5);
    ELEMENTS_ORDER.put("kind", 6);
    ELEMENTS_ORDER.put("scale", 7);
    ELEMENTS_ORDER.put("plan", 8);
    ELEMENTS_ORDER.put("identity", 9);
    ELEMENTS_ORDER.put("dependsOn", 10);
    ELEMENTS_ORDER.put("tags", 11);
    // between tags and properties is a place for elements not defined here
    ELEMENTS_ORDER.put("properties", 100);

    DECORATORS_ORDER.put("description", 1);
    DECORATORS_ORDER.put("batchSize", 2);
  }

  @Override
  public void initialize(InitContext init) {
    init.register(ResourceDeclarationImpl.class, ElementsOrderResourceBicep::checkResource);
    init.register(ResourceDeclarationImpl.class, ElementsOrderResourceBicep::checkResourceDecorators);
  }

  private static void checkResource(CheckContext checkContext, ResourceDeclarationImpl resourceDeclaration) {
    var prevIndex = 0;
    for (Property property : resourceDeclaration.resourceProperties()) {
      var index = ELEMENTS_ORDER.getOrDefault(property.key().value(), DEFAULT_ORDER_FOR_UNKNOWN_PROPERTY);
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
      var identifier = ((FunctionCall) decorator.expression()).name();
      var index = DECORATORS_ORDER.getOrDefault(identifier.value(), DEFAULT_ORDER_FOR_UNKNOWN_PROPERTY);
      if (index < prevIndex) {
        var textRange = TextRanges.merge(
          decorator.keyword().textRange(),
          identifier.textRange());
        checkContext.reportIssue(textRange, MESSAGE_DECORATOR);
        break;
      }
      prevIndex = index;
    }
  }
}
