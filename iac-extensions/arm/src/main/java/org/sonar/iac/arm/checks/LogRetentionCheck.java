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

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.arm.checkdsl.ContextualObject;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.NumericLiteral;

import static org.sonar.iac.arm.checks.utils.CheckUtils.isFalse;

@Rule(key = "S6413")
public class LogRetentionCheck extends AbstractArmResourceCheck {

  private static final int DEFAULT_RETENTION_THRESHOLD = 14;
  private static final String RETENTION_DAY_NAME = "retentionDays";
  private static final String PROPERTY_OR_TYPE_OMITTED_MESSAGE = "Omitting \"%s\" results in a short log retention duration. Make sure it is safe here.";
  private static final String PROPERTY_DISABLED_MESSAGE = "Disabling \"%s\" results in a short log retention duration. Make sure it is safe here.";
  private static final String SHORT_LOG_RETENTION_DEFINED_MESSAGE = "Make sure that defining a short log retention duration is safe here.";

  private static final List<String> SIMPLE_LOG_RETENTION_RESOURCES = List.of(
    "Microsoft.Sql/servers/auditingSettings",
    "Microsoft.Sql/servers/databases/securityAlertPolicies",
    "Microsoft.Sql/servers/auditingPolicies",
    "Microsoft.Synapse/workspaces/auditingSettings",
    "Microsoft.Synapse/workspaces/sqlPools/securityAlertPolicies",
    "Microsoft.DBforMariaDB/servers/securityAlertPolicies");

  @RuleProperty(
    key = "log_retention_duration",
    defaultValue = "" + DEFAULT_RETENTION_THRESHOLD,
    description = "Default minimum retention period in days.")
  public int retentionPeriodInDays = DEFAULT_RETENTION_THRESHOLD;

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Network/firewallPolicies",
      checkLogRetention("insights", "isEnabled", RETENTION_DAY_NAME));

    register("Microsoft.Network/networkWatchers/flowLogs",
      checkLogRetention("retentionPolicy", "enabled", "days"));

    register(SIMPLE_LOG_RETENTION_RESOURCES, this::checkLogRetentionAsSimpleProperty);
  }

  private Consumer<ContextualResource> checkLogRetention(String propertyName, String enablingTypeName, String retentionDayTypeName) {
    return resource -> {
      ContextualObject object = resource
        .object(propertyName);
      object.reportIfAbsent(String.format(PROPERTY_OR_TYPE_OMITTED_MESSAGE, propertyName));
      object.property(enablingTypeName)
        .reportIf(isFalse(), String.format(PROPERTY_DISABLED_MESSAGE, enablingTypeName))
        .reportIfAbsent(String.format(PROPERTY_OR_TYPE_OMITTED_MESSAGE, enablingTypeName));
      object.property(retentionDayTypeName)
        .reportIf(isRetentionDaySensitive(), SHORT_LOG_RETENTION_DEFINED_MESSAGE)
        .reportIfAbsent(String.format(PROPERTY_OR_TYPE_OMITTED_MESSAGE, retentionDayTypeName));
    };
  }

  private void checkLogRetentionAsSimpleProperty(ContextualResource resource) {
    resource.property(RETENTION_DAY_NAME)
      .reportIf(isRetentionDaySensitive(), SHORT_LOG_RETENTION_DEFINED_MESSAGE)
      .reportIfAbsent(String.format(PROPERTY_OR_TYPE_OMITTED_MESSAGE, RETENTION_DAY_NAME));
  }

  private Predicate<Expression> isRetentionDaySensitive() {
    return expr -> {
      if (!expr.is(ArmTree.Kind.NUMERIC_LITERAL)) {
        return false;
      }
      float retentionDays = ((NumericLiteral) expr).value();
      return retentionDays < retentionPeriodInDays && retentionDays != 0;
    };
  }
}
