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
import java.util.Map;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.impl.json.ResourceDeclarationImpl;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;

public class ElementsOrderResourceJson implements IacCheck {

  private static final String MESSAGE = "Reorder the elements to match the recommended order.";

  private static final Map<String, Integer> elementsOrder = new HashMap<>();
  private static final int DEFAULT_ORDER_FOR_UNKNOWN_PROPERTY = 20;

  static {
    elementsOrder.put("comments", 0);
    elementsOrder.put("condition", 1);
    elementsOrder.put("scope", 2);
    elementsOrder.put("type", 3);
    elementsOrder.put("apiVersion", 4);
    elementsOrder.put("name", 5);
    elementsOrder.put("location", 6);
    elementsOrder.put("zones", 7);
    elementsOrder.put("sku", 8);
    elementsOrder.put("kind", 9);
    elementsOrder.put("scale", 10);
    elementsOrder.put("plan", 11);
    elementsOrder.put("identity", 12);
    elementsOrder.put("copy", 13);
    elementsOrder.put("dependsOn", 14);
    elementsOrder.put("tags", 15);
    // between tags and properties is a place for elements not defined here
    elementsOrder.put("properties", 100);
  }

  @Override
  public void initialize(InitContext init) {
    init.register(ResourceDeclarationImpl.class, ElementsOrderResourceJson::checkResource);
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
}
