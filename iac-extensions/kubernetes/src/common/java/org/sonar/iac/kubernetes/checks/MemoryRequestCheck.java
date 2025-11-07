/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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

import org.sonar.check.Rule;

@Rule(key = "S6873")
public class MemoryRequestCheck extends AbstractRequestCheck {
  private static final String MESSAGE = "Specify a memory request for this container.";
  private static final String RESOURCE_NAME = "memory";

  @Override
  String getResourceName() {
    return RESOURCE_NAME;
  }

  @Override
  String getMessage() {
    return MESSAGE;
  }
}
