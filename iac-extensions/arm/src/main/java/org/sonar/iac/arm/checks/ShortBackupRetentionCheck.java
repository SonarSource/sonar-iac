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

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.DoublePredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.arm.checkdsl.ContextualObject;
import org.sonar.iac.arm.checkdsl.ContextualProperty;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.NumericLiteral;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.common.api.tree.Tree;

import static org.sonar.iac.arm.checks.utils.CheckUtils.isEqual;
import static org.sonar.iac.arm.checks.utils.CheckUtils.isValue;

@Rule(key = "S6364")
public class ShortBackupRetentionCheck extends AbstractArmResourceCheck {

  private static final String RETENTION_PERIOD_TOO_SHORT_MESSAGE = "Make sure that defining a short backup retention duration is safe here.";
  private static final String NO_RETENTION_PERIOD_PROPERTY_MESSAGE = "Omitting \"%s\" causes a short backup retention period to be set. " + RETENTION_PERIOD_TOO_SHORT_MESSAGE;

  private static final Set<String> TYPES_BASIC_RETENTION = Set.of("AzureIaasVM", "AzureSql", "AzureStorage", "MAB");
  private static final Set<String> TYPES_SUBPROTECTION_RETENTION = Set.of("GenericProtectionPolicy", "AzureWorkload");
  private static final Map<String, Function<Double, Double>> POLICY_TO_DAYS = Map.of(
    "Days", days -> days,
    "Weeks", days -> days * 7.0,
    "Months", days -> days * 30.0,
    "Years", days -> days * 365.0);

  private static final int DEFAULT_RETENTION_PERIOD = 30;

  @RuleProperty(
    key = "backup_retention_duration",
    defaultValue = "" + DEFAULT_RETENTION_PERIOD,
    description = "Default minimum retention period in days.")
  public int retentionPeriodInDays = DEFAULT_RETENTION_PERIOD;

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Web/sites/config", this::checkBackupRetentionWebSitesConfig);
    register("Microsoft.DocumentDB/databaseAccounts", this::checkBackupRetentionDatabaseAccounts);
    register("Microsoft.RecoveryServices/vaults/backupPolicies", this::checkBackupRetentionRecoveryServicesVaults);
  }

  private void checkBackupRetentionWebSitesConfig(ContextualResource resource) {
    resource.object("backupSchedule")
      .property("retentionPeriodInDays")
      .reportIf(isNumericValue(backupRetentionIntervalInDays -> backupRetentionIntervalInDays < retentionPeriodInDays), RETENTION_PERIOD_TOO_SHORT_MESSAGE);
  }

  private void checkBackupRetentionDatabaseAccounts(ContextualResource resource) {
    ContextualObject backupPolicy = resource.object("backupPolicy");
    if (backupPolicy.property("type").is(isEqual("Periodic"))) {
      backupPolicy.object("periodicModeProperties")
        .reportIfAbsent(String.format(NO_RETENTION_PERIOD_PROPERTY_MESSAGE, "periodicModeProperties.backupRetentionIntervalInHours"))
        .property("backupRetentionIntervalInHours")
        .reportIf(isNumericValue(backupRetentionIntervalInHours -> backupRetentionIntervalInHours / 24.0 < retentionPeriodInDays), RETENTION_PERIOD_TOO_SHORT_MESSAGE)
        .reportIfAbsent(NO_RETENTION_PERIOD_PROPERTY_MESSAGE);
    }
  }

  private void checkBackupRetentionRecoveryServicesVaults(ContextualResource resource) {
    retrieveRetentionPolicy(resource).forEach(retentionPolicyObject -> {
      ContextualObject retentionDurationObject = retrieveRetentionDuration(retentionPolicyObject);
      Double durationInDays = computeRetentionInDays(retentionDurationObject);
      if (durationInDays != null && durationInDays < retentionPeriodInDays) {
        retentionDurationObject.property("count").report(RETENTION_PERIOD_TOO_SHORT_MESSAGE, retentionDurationObject.property("durationType").toSecondary("Duration type"));
      }
    });
  }

  private static Stream<ContextualObject> retrieveRetentionPolicy(ContextualResource resource) {
    ContextualProperty backupManagementType = resource.property("backupManagementType");

    if (backupManagementType.is(isValue(TYPES_BASIC_RETENTION::contains))) {
      return Stream.of(resource.object("retentionPolicy"));
    } else if (backupManagementType.is(isValue(TYPES_SUBPROTECTION_RETENTION::contains))) {
      return resource.list("subProtectionPolicy")
        .objects()
        .map(contextualObject -> contextualObject.object("retentionPolicy"));
    } else {
      return Stream.empty();
    }
  }

  private static ContextualObject retrieveRetentionDuration(ContextualObject retentionPolicyObject) {
    ContextualProperty retentionPolicyType = retentionPolicyObject.property("retentionPolicyType");

    if (retentionPolicyType.is(isEqual("SimpleRetentionPolicy"))) {
      return retentionPolicyObject.object("retentionDuration");
    } else if (retentionPolicyType.is(isEqual("LongTermRetentionPolicy"))) {
      return retentionPolicyObject.object("dailySchedule").object("retentionDuration");
    } else {
      return ContextualObject.fromAbsent(retentionPolicyObject.ctx, null, retentionPolicyObject);
    }
  }

  @CheckForNull
  private static Double computeRetentionInDays(ContextualObject retentionPolicyObject) {
    Double count = toDouble(retentionPolicyObject.property("count").tree);
    String durationType = toString(retentionPolicyObject.property("durationType").tree);

    if (count != null && durationType != null) {
      return POLICY_TO_DAYS.getOrDefault(durationType, val -> null).apply(count);
    } else {
      return null;
    }
  }

  private static Predicate<Expression> isNumericValue(DoublePredicate predicate) {
    return expr -> expr.is(ArmTree.Kind.NUMERIC_LITERAL) && predicate.test(((NumericLiteral) expr).doubleValue());
  }

  @CheckForNull
  private static Double toDouble(@Nullable Tree tree) {
    return Optional.ofNullable(tree)
      .map(Property.class::cast)
      .map(Property::value)
      .filter(expr -> expr.is(ArmTree.Kind.NUMERIC_LITERAL))
      .map(expr -> ((NumericLiteral) expr).doubleValue())
      .orElse(null);
  }

  @CheckForNull
  private static String toString(@Nullable Tree tree) {
    return Optional.ofNullable(tree)
      .map(Property.class::cast)
      .map(Property::value)
      .filter(expr -> expr.is(ArmTree.Kind.STRING_LITERAL))
      .map(expr -> ((StringLiteral) expr).value())
      .orElse(null);
  }
}
