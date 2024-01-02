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
package org.sonar.iac.docker.checks;

import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.EnvInstruction;
import org.sonar.iac.docker.tree.api.KeyValuePair;

@Rule(key = "S4507")
public class DebugModeCheck implements IacCheck {

  private static final String MESSAGE = "Make sure this debug feature is deactivated before delivering the code in production.";

  private static final Pattern ENV_DEV_NAME_PATTERN = Pattern.compile("^([_A-Z]+)?ENV(IRONMENT)?$", Pattern.CASE_INSENSITIVE);
  private static final Pattern ENV_DEV_VALUE_PATTERN = Pattern.compile("^dev(el(op(ment)?)?)?$", Pattern.CASE_INSENSITIVE);
  private static final Pattern DEBUG_ENABLED_NAME_PATTERN = Pattern.compile("^([_A-Z]+)?DEBUG$", Pattern.CASE_INSENSITIVE);
  private static final Pattern DEBUG_ENABLED_VALUE_PATTERN = Pattern.compile("^(true|yes|on|1)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern PHPX_DEBUG_ENABLED_NAME_PATTERN = Pattern.compile("^([_A-Z]+)?XDEBUG_MODE$", Pattern.CASE_INSENSITIVE);

  @Override
  public void initialize(InitContext init) {
    init.register(EnvInstruction.class, DebugModeCheck::checkEnvDebug);
  }

  private static void checkEnvDebug(CheckContext ctx, EnvInstruction envInstruction) {
    for (KeyValuePair variable : envInstruction.environmentVariables()) {
      String name = ArgumentResolution.of(variable.key()).value();
      String value = ArgumentResolution.of(variable.value()).value();
      if (name != null && value != null && isVariableSensitive(name, value)) {
        ctx.reportIssue(variable, MESSAGE);
      }
    }
  }

  private static boolean isVariableSensitive(String name, String value) {
    return isDevEnv(name, value) || isDebugMode(name, value) || isPhpXDebugMode(name, value);
  }

  private static boolean isDevEnv(String name, String value) {
    return ENV_DEV_NAME_PATTERN.matcher(name).matches()
      && ENV_DEV_VALUE_PATTERN.matcher(value).matches();
  }

  private static boolean isDebugMode(String name, String value) {
    return DEBUG_ENABLED_NAME_PATTERN.matcher(name).matches()
      && DEBUG_ENABLED_VALUE_PATTERN.matcher(value).matches();
  }

  private static boolean isPhpXDebugMode(String name, String value) {
    return PHPX_DEBUG_ENABLED_NAME_PATTERN.matcher(name).matches()
      && !value.equalsIgnoreCase("off");
  }
}
