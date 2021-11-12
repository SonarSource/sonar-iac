package org.sonar.iac.cloudformation.checks;

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6364")
public class ShortBackupRetentionCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure that defining a short backup retention duration is safe here.";
  private static final int DEFAULT = 7;

  @RuleProperty(
    key = "backup_retention_duration",
    defaultValue = "" + DEFAULT)
  int backupRetentionDuration = DEFAULT;


  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if ((resource.isType("AWS::RDS::DBInstance") && PropertyUtils.isMissing(resource.properties(), "SourceDBInstanceIdentifier"))
      || resource.isType("AWS::RDS::DBCluster")) {
      checkBackupRetentionPeriod(ctx, resource, backupRetentionDuration);
    }
  }

  private static void checkBackupRetentionPeriod(CheckContext ctx, Resource resource, int minPeriod) {
    PropertyUtils.value(resource.properties(), "BackupRetentionPeriod").ifPresentOrElse(period ->
        TextUtils.getIntValue(period).filter(currentPeriod -> currentPeriod < minPeriod)
          .ifPresent(currentPeriod -> ctx.reportIssue(period, MESSAGE)),
      () -> reportResource(ctx, resource, MESSAGE));
  }
}
