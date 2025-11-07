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
package org.sonar.iac.arm.tree.api.bicep;

import java.util.List;
import java.util.Optional;

public interface HasDecorators {
  List<Decorator> decorators();

  default Optional<Decorator> findDecoratorByName(String decoratorName) {
    return decorators().stream()
      .filter(it -> Optional.ofNullable(it.functionCallOrMemberFunctionCall())
        .map(functionCall -> decoratorName.equals(functionCall.name().value()))
        .orElse(false))
      .findFirst();
  }
}
