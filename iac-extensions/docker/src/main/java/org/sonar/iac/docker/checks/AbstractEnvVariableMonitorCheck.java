/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.docker.checks;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.EnvInstruction;
import org.sonar.iac.docker.tree.api.FromInstruction;
import org.sonar.iac.docker.tree.api.KeyValuePair;

public abstract class AbstractEnvVariableMonitorCheck implements IacCheck {
  private final Map<String, String> globalEnvironmentVariables = new HashMap<>();

  @Override
  public void initialize(InitContext init) {
    init.register(EnvInstruction.class, this::updateEnvVariable);
    init.register(FromInstruction.class, this::cleanEnvVariables);
    init(init);
  }

  private void cleanEnvVariables(CheckContext checkContext, FromInstruction fromInstruction) {
    globalEnvironmentVariables.clear();
  }

  private void updateEnvVariable(CheckContext checkContext, EnvInstruction envInstruction) {
    envInstruction.environmentVariables().forEach((KeyValuePair keyValuePair) -> {
      var variableName = ArgumentResolution.of(keyValuePair.key());
      if (variableName.isResolved()) {
        var value = ArgumentResolution.of(keyValuePair.value());
        globalEnvironmentVariables.put(variableName.value(), value.value());
      }
    });
  }

  protected Map<String, String> getGlobalEnvironmentVariables() {
    return Collections.unmodifiableMap(globalEnvironmentVariables);
  }

  public abstract void init(InitContext init);
}
