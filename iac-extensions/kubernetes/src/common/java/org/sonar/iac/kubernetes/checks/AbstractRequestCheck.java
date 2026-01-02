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
package org.sonar.iac.kubernetes.checks;

import java.util.Map;
import org.sonar.iac.kubernetes.model.LimitRangeItem;

public abstract class AbstractRequestCheck extends AbstractLimitCheck {

  private static final String RESOURCE_MANAGEMENT_TYPE = "requests";

  @Override
  String getResourceManagementName() {
    return RESOURCE_MANAGEMENT_TYPE;
  }

  @Override
  protected Map<String, String> retrieveLimitRangeItemMap(LimitRangeItem limitRangeItem) {
    return limitRangeItem.defaultRequestMap();
  }
}
