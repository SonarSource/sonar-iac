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
import org.sonar.iac.arm.checks.ElementsOrderResourceCheck;
import org.sonar.iac.arm.tree.impl.json.ResourceDeclarationImpl;
import org.sonar.iac.common.api.checks.InitContext;

/**
 * It is a sub check of S6956, see {@link ElementsOrderResourceCheck}.
 */
public class ElementsOrderResourceCheckJson extends AbstractElementsOrderResourceCheck {

  @Override
  public void initialize(InitContext init) {
    init.register(ResourceDeclarationImpl.class, this::checkResource);
  }

  @Override
  protected List<Map<String, Integer>> getElementsOrderSets() {
    return ElementOrders.JSON_ELEMENTS_ORDER_SETS;
  }
}
