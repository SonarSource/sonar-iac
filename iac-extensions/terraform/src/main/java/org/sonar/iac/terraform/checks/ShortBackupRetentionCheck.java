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
package org.sonar.iac.terraform.checks;

import java.util.Set;
import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.symbols.AttributeSymbol;
import org.sonar.iac.terraform.symbols.BlockSymbol;
import org.sonar.iac.terraform.symbols.ResourceSymbol;

import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isFalse;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isTrue;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.lessThan;

@Rule(key = "S6364")
public class ShortBackupRetentionCheck extends AbstractNewResourceCheck {

  public static final String MESSAGE = "Make sure that defining a short backup retention duration is safe here.";
  public static final String OMITTING_MESSAGE = "Omitting \"%s\" results in a short backup retention duration. Make sure it is safe here.";
  public static final int DEFAULT = 7;
  private static final Set<String> ENGINES_EXCEPTION = Set.of("aurora", "aurora-mysql", "aurora-postgresql");

  @RuleProperty(
    key = "backup_retention_duration",
    defaultValue = "" + DEFAULT,
    description = "Minimum backup retention duration in days")
  int backupRetentionDuration = DEFAULT;

  @Override
  protected void registerResourceConsumer() {
    register("aws_db_instance",
      resource -> {
        if (resource.attribute("source_db_instance_identifier").isAbsent()
          && !resource.attribute("engine").is(isEngineException())) {
          checkAwsRetentionRate(resource);
        }
      });

    register("aws_rds_cluster", this::checkAwsRetentionRate);

    register("azurerm_backup_policy_file_share",
      resource -> resource.block("retention_daily")
        .attribute("count")
        .reportIf(lessThan(backupRetentionDuration), MESSAGE));

    register("azurerm_cosmosdb_account",
      resource -> resource.block("backup")
        .reportIfAbsent(String.format(OMITTING_MESSAGE, "backup.retention_in_hours"))
        .attribute("retention_in_hours")
        .reportIfAbsent(OMITTING_MESSAGE)
        .reportIf(lessThan(backupRetentionDuration * 24), MESSAGE));

    register("azurerm_app_service",
      resource -> {
        BlockSymbol backup = resource.block("backup").reportIfAbsent(OMITTING_MESSAGE);
        AttributeSymbol enabled = backup.attribute("enabled");
        if (enabled.isPresent()) {
          enabled.reportIf(isFalse(), "Make sure disabling backup is safe here.");
          if (enabled.is(isTrue())) {
            backup.block("schedule")
              .attribute("retention_period_in_days")
              .reportIf(lessThan(backupRetentionDuration), MESSAGE);
          }
        }
      });
  }

  private static Predicate<ExpressionTree> isEngineException() {
    return expr -> TextUtils.getValue(expr).filter(ENGINES_EXCEPTION::contains).isPresent();
  }

  private void checkAwsRetentionRate(ResourceSymbol resource) {
    AttributeSymbol retentionPeriod = resource.attribute("backup_retention_period")
      .reportIf(lessThan(backupRetentionDuration), MESSAGE);

    if (retentionPeriod.isAbsent() && backupRetentionDuration != 1) {
      retentionPeriod.reportIfAbsent(OMITTING_MESSAGE);
    }
  }

}
