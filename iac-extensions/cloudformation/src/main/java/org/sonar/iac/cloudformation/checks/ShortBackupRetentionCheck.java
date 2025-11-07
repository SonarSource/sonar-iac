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
package org.sonar.iac.cloudformation.checks;

import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6364")
public class ShortBackupRetentionCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure that defining a short backup retention duration is safe here.";
  private static final String OMITTING_MESSAGE = "Omitting \"BackupRetentionPeriod\" sets the backup retention period to 1 day. " + MESSAGE;
  private static final int DEFAULT = 7;
  private static final Set<String> ENGINES_EXCEPTION = Set.of("aurora", "aurora-mysql", "aurora-postgresql");

  @RuleProperty(
    key = "backup_retention_duration",
    defaultValue = "" + DEFAULT,
    description = "Minimum backup retention duration in days")
  int backupRetentionDuration = DEFAULT;

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if ((resource.isType("AWS::RDS::DBInstance")
      && PropertyUtils.isMissing(resource.properties(), "SourceDBInstanceIdentifier")
      && isNotEngineException(resource))
      || resource.isType("AWS::RDS::DBCluster")) {
      checkBackupRetentionPeriod(ctx, resource, backupRetentionDuration);
    }
  }

  private static boolean isNotEngineException(Resource resource) {
    return PropertyUtils.has(resource.properties(), "Engine").isFalse()
      || PropertyUtils.valueIs(resource.properties(), "Engine", tree -> TextUtils.getValue(tree).filter(ENGINES_EXCEPTION::contains).isEmpty());
  }

  private static void checkBackupRetentionPeriod(CheckContext ctx, Resource resource, int minPeriod) {
    PropertyUtils.value(resource.properties(), "BackupRetentionPeriod").ifPresentOrElse(period -> TextUtils.getIntValue(period).filter(currentPeriod -> currentPeriod < minPeriod)
      .ifPresent(currentPeriod -> ctx.reportIssue(period, MESSAGE)),
      () -> {
        if (minPeriod != 1) {
          reportResource(ctx, resource, OMITTING_MESSAGE);
        }
      });
  }
}
