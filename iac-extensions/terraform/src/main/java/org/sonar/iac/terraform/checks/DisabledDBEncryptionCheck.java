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

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.iac.terraform.symbols.AttributeSymbol;
import org.sonar.iac.terraform.symbols.ResourceSymbol;

import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.equalTo;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isFalse;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.notEqualTo;

@Rule(key = "S6303")
public class DisabledDBEncryptionCheck extends AbstractNewResourceCheck {

  private static final String MESSAGE_DB_INSTANCE = "Make sure that using unencrypted RDS DB Instances is safe here.";
  private static final String MESSAGE_RDS_CLUSTER = "Make sure that using an unencrypted RDS DB Cluster is safe here.";
  private static final String MESSAGE_DB_INSTANCE_BACKUP_REPLICATION = "Make sure that using an unencrypted DB backup replication is safe here.";
  private static final String OMITTING_MESSAGE = "Omitting \"storage_encrypted\" disables databases encryption. Make sure it is safe here.";
  private static final String SECONDARY_MESSAGE_DB_INSTANCE = "Related RDS DB Instance";
  private static final String SECONDARY_MESSAGE_RDS_CLUSTER = "Related RDS Cluster";
  private static final List<String> EXCLUDE_AURORA_ATTRIBUTE = List.of("aurora", "aurora-mysql", "aurora-postgresql");

  protected void registerResourceConsumer() {
    register("aws_db_instance", DisabledDBEncryptionCheck::checkAwsDbInstance);
    register("aws_rds_cluster", DisabledDBEncryptionCheck::checkAwsRdsCluster);
    register("aws_db_instance_automated_backups_replication", DisabledDBEncryptionCheck::checkAwsDbInstanceBackupReplication);
  }

  private static void checkAwsDbInstance(ResourceSymbol resource) {
    AttributeSymbol engine = resource.attribute("engine");
    if (EXCLUDE_AURORA_ATTRIBUTE.stream().anyMatch(auroraAttribute -> engine.is(equalTo(auroraAttribute)))) {
      return;
    }
    resource.attribute("storage_encrypted")
      .reportIf(isFalse(), MESSAGE_DB_INSTANCE, resource.toSecondary(SECONDARY_MESSAGE_DB_INSTANCE))
      .reportIfAbsent(OMITTING_MESSAGE);
  }

  private static void checkAwsRdsCluster(ResourceSymbol resource) {
    AttributeSymbol engineMode = resource.attribute("engine_mode");
    AttributeSymbol storageEncrypted = resource.attribute("storage_encrypted");
    storageEncrypted.reportIf(isFalse(), MESSAGE_RDS_CLUSTER, resource.toSecondary(SECONDARY_MESSAGE_RDS_CLUSTER));
    if (engineMode.isAbsent() || engineMode.is(notEqualTo("serverless"))) {
      storageEncrypted.reportIfAbsent(OMITTING_MESSAGE);
    }
  }

  private static void checkAwsDbInstanceBackupReplication(ResourceSymbol resource) {
    resource.attribute("kms_key_id").reportIfAbsent(MESSAGE_DB_INSTANCE_BACKUP_REPLICATION);
  }
}
