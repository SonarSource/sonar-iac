/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.terraform.checks.aws;

import java.util.List;
import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;

import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isFalse;

public class AzureDisabledLoggingCheckPart extends AbstractNewResourceCheck {

  @Override
  protected void registerResourceConsumer() {
    register(List.of("azurerm_function_app", "azurerm_function_app_slot"),
      resource -> resource.attribute("enable_builtin_logging")
        .reportIf(isFalse(), "Make sure that disabling built-in logging is safe here."));

    register("azurerm_automation_runbook",
      resource -> resource.attribute("log_progress")
        .reportIfAbsent("Make sure that omitting the activation of progress logging is safe here.")
        .reportIf(isFalse(), "Make sure that disabling progress logging is safe here."));
  }
}
