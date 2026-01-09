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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ElementOrders {

  public static final int DEFAULT_ORDER_FOR_UNKNOWN_PROPERTY = 200;

  private static final Map<String, Integer> DEFAULT_ELEMENTS_ORDER = Map.ofEntries(
    Map.entry("comments", 0),
    Map.entry("condition", 10),
    Map.entry("scope", 20),
    Map.entry("type", 30),
    Map.entry("apiversion", 40),
    Map.entry("name", 50),
    Map.entry("location", 50),
    Map.entry("extendedlocation", 50),
    Map.entry("zones", 70),
    Map.entry("sku", 80),
    Map.entry("kind", 90),
    Map.entry("scale", 100),
    Map.entry("plan", 110),
    Map.entry("identity", 120),
    Map.entry("copy", 130),
    Map.entry("dependson", 140),
    Map.entry("tags", 150),
    // between tags and properties is a place for elements not defined here
    Map.entry("properties", 1000));

  // Default order (ARM Tools): type, apiVersion/resourceGroup/name/location, ...
  private static final Map<String, Integer> BASE_ELEMENTS_ORDER = createOrderWithOverrides(DEFAULT_ELEMENTS_ORDER);

  // Visual Studio Code order: type, name/resourceGroup/apiVersion, ...
  private static final Map<String, Integer> VS_CODE_ELEMENTS_ORDER = createOrderWithOverrides(DEFAULT_ELEMENTS_ORDER,
    Map.entry("name", 40),
    Map.entry("location", 40),
    Map.entry("extendedlocation", 40),
    Map.entry("resourcegroup", 50),
    Map.entry("apiversion", 50));

  // JSON order sets
  public static final List<Map<String, Integer>> JSON_ELEMENTS_ORDER_SETS = List.of(
    BASE_ELEMENTS_ORDER,
    VS_CODE_ELEMENTS_ORDER);

  // Bicep order sets (same as JSON but with parent property)
  private static final Map<String, Integer> BICEP_BASE_ELEMENTS_ORDER = createOrderWithOverrides(BASE_ELEMENTS_ORDER,
    Map.entry("parent", 0));

  private static final Map<String, Integer> BICEP_VS_CODE_ELEMENTS_ORDER = createOrderWithOverrides(VS_CODE_ELEMENTS_ORDER,
    Map.entry("parent", 0));

  public static final List<Map<String, Integer>> BICEP_ELEMENTS_ORDER_SETS = List.of(
    BICEP_BASE_ELEMENTS_ORDER,
    BICEP_VS_CODE_ELEMENTS_ORDER);

  private ElementOrders() {
    // utility class
  }

  @SafeVarargs
  public static Map<String, Integer> createOrderWithOverrides(Map<String, Integer> base, Map.Entry<String, Integer>... overrides) {
    var result = new HashMap<>(base);
    for (var override : overrides) {
      result.put(override.getKey(), override.getValue());
    }
    return Map.copyOf(result);
  }
}
