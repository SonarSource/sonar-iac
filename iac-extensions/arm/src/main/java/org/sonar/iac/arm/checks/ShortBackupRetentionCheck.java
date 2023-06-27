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

import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.arm.checkdsl.ContextualObject;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.NumericLiteral;

import static org.sonar.iac.arm.checks.utils.CheckUtils.isValue;

@Rule(key = "S6364")
public class ShortBackupRetentionCheck extends AbstractArmResourceCheck {

  private static final String RETENTION_PERIOD_TOO_SHORT_MESSAGE = "Make sure that defining a short backup retention duration is safe here.";
  private static final String NO_RETENTION_PERIOD_PROPERTY_MESSAGE = "Omitting \"%s\" causes a short backup retention period to be set. " + RETENTION_PERIOD_TOO_SHORT_MESSAGE;

  private static final int DEFAULT_RETENTION_PERIOD = 30;

  @RuleProperty(
    key = "backup_retention_duration",
    defaultValue = "" + DEFAULT_RETENTION_PERIOD,
    description = "Default minimum retention period in day.")
  public int retentionPeriod = DEFAULT_RETENTION_PERIOD;

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Web/sites/config", this::checkBackupRetentionWebSitesConfig);
    register("Microsoft.DocumentDB/databaseAccounts", this::checkBackupRetentionDatabaseAccounts);
  }

  private void checkBackupRetentionWebSitesConfig(ContextualResource resource) {
    if ("backup".equals(resource.name)) {
    resource.object("backupSchedule")
      .property("retentionPeriodInDays")
      .reportIf(isNumericValue(numeric -> numeric < retentionPeriod), RETENTION_PERIOD_TOO_SHORT_MESSAGE);
    }
  }

  private void checkBackupRetentionDatabaseAccounts(ContextualResource resource) {
    ContextualObject backupPolicy = resource.object("backupPolicy");
    if (backupPolicy.property("type").is(isValue("Periodic"::equals))) {
      backupPolicy.object("periodicModeProperties")
        .reportIfAbsent(String.format(NO_RETENTION_PERIOD_PROPERTY_MESSAGE, "periodicModeProperties.backupRetentionIntervalInHours"))
        .property("backupRetentionIntervalInHours")
        .reportIf(isNumericValue(numeric -> numeric / 24.0 < retentionPeriod), RETENTION_PERIOD_TOO_SHORT_MESSAGE)
        .reportIfAbsent(NO_RETENTION_PERIOD_PROPERTY_MESSAGE);
    }
  }

  private static Predicate<Expression> isNumericValue(Predicate<Float> predicate) {
    return expr -> expr.is(ArmTree.Kind.NUMERIC_LITERAL) && predicate.test(((NumericLiteral) expr).value());
  }
}
