/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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

import org.sonar.check.Rule;
import org.sonar.iac.arm.checkdsl.ContextualResource;

import static org.sonar.iac.arm.checks.utils.CheckUtils.isEqual;
import static org.sonar.iac.arm.checks.utils.CheckUtils.isFalse;

@Rule(key = "S5332")
public class ClearTextProtocolsCheck extends AbstractArmResourceCheck {

  private static final String GENERAL_ISSUE_MESSAGE = "Make sure that using clear-text protocols is safe here.";
  private static final String ISSUE_MESSAGE_ON_MISSING_PROPERTY = "Omitting \"%s\" allows the use of clear-text protocols. Make sure it is safe here.";

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Web/sites", ClearTextProtocolsCheck::checkHttpsFlag);
    register("Microsoft.Web/sites/config", ClearTextProtocolsCheck::checkFtpsState);
    register("Microsoft.Storage/storageAccounts", ClearTextProtocolsCheck::checkHttpsTraffic);
  }

  private static void checkHttpsFlag(ContextualResource resource) {
    resource.property("httpsOnly")
      .reportIfAbsent(ISSUE_MESSAGE_ON_MISSING_PROPERTY)
      .reportIf(isFalse(), GENERAL_ISSUE_MESSAGE);
  }

  private static void checkFtpsState(ContextualResource resource) {
    resource.property("ftpsState")
      .reportIf(isEqual("AllAllowed"), GENERAL_ISSUE_MESSAGE);
  }

  private static void checkHttpsTraffic(ContextualResource resource) {
    resource.property("supportsHttpsTrafficOnly")
      .reportIf(isFalse(), GENERAL_ISSUE_MESSAGE);
  }
}
