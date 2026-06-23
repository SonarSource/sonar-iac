/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.terraform.checks.azure;

import org.sonar.check.Rule;
import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;

import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isFalse;

@Rule(key = "S8847")
public class KeyVaultPurgeProtectionCheck extends AbstractNewResourceCheck {

  private static final String DISABLED_MESSAGE = "Make sure that disabling purge protection is safe here.";
  private static final String OMITTING_MESSAGE = "Omitting \"purge_protection_enabled\" disables purge protection. Make sure it is safe here.";

  @Override
  protected void registerResourceConsumer() {
    register("azurerm_key_vault",
      resource -> resource.attribute("purge_protection_enabled")
        .reportIfAbsent(OMITTING_MESSAGE)
        .reportIf(isFalse(), DISABLED_MESSAGE));
  }
}
