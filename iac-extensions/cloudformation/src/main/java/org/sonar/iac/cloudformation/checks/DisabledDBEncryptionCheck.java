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
package org.sonar.iac.cloudformation.checks;

import java.util.List;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6303")
public class DisabledDBEncryptionCheck extends AbstractResourceCheck {

  private static final String MESSAGE_RDS_DB_INSTANCE = "Make sure that using unencrypted RDS DB Instances is safe here.";
  private static final String MESSAGE_RDS_CLUSTER = "Make sure that using an unencrypted RDS DB Cluster is safe here.";
  private static final String MESSAGE_RDS_DB_GLOBAL_CLUSTER = "Make sure that using an unencrypted RDS DB GlobalCluster is safe here.";

  private static final String OMITTING_MESSAGE = "Omitting \"StorageEncrypted\" disables databases encryption. Make sure it is safe here.";
  private static final String SECONDARY_MESSAGE = "Related RDS DBInstance";
  private static final List<String> EXCLUDE_AURORA_ATTRIBUTE = List.of("aurora", "aurora-mysql", "aurora-postgresql");
  private static final String KEY_STORAGE_ENCRYPTED = "StorageEncrypted";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (resource.isType("AWS::RDS::DBCluster")) {
      checkForDBClusterProperties(ctx, resource);
    } else if (resource.isType("AWS::RDS::GlobalCluster")) {
      checkForGlobalClusterProperties(ctx, resource);
    } else if (resource.isType("AWS::RDS::DBInstance")) {
      // S6303 should not raise an issue if the property Engine of the resource AWS::RDS::DBInstance is one of EXCLUDE_AURORA_ATTRIBUTE
      if (PropertyUtils.get(resource.properties(), "Engine").stream()
        .anyMatch(engine -> TextUtils.matchesValue(engine.value(), EXCLUDE_AURORA_ATTRIBUTE::contains).isTrue())) {
        return;
      }
      checkStorageEncrypted(ctx, resource, MESSAGE_RDS_DB_INSTANCE);
    }
  }

  private static void checkForGlobalClusterProperties(CheckContext ctx, Resource resource) {
    if (PropertyUtils.get(resource.properties(), "SourceDBClusterIdentifier").isPresent()) {
      return;
    }
    checkStorageEncrypted(ctx, resource, MESSAGE_RDS_DB_GLOBAL_CLUSTER);
  }

  private static void checkForDBClusterProperties(CheckContext ctx, Resource resource) {
    if (PropertyUtils.get(resource.properties(), "SourceDBClusterIdentifier").isPresent()
      || PropertyUtils.get(resource.properties(), "SnapshotIdentifier").isPresent()) {
      return;
    }
    checkStorageEncrypted(ctx, resource, MESSAGE_RDS_CLUSTER);
  }

  private static void checkStorageEncrypted(CheckContext ctx, Resource resource, String message) {
    Optional<PropertyTree> maybeEncryption = PropertyUtils.get(resource.properties(), KEY_STORAGE_ENCRYPTED);
    maybeEncryption.ifPresentOrElse(
      encryption -> {
        if (TextUtils.isValueFalse(encryption.value())) {
          ctx.reportIssue(encryption.key(), message, new SecondaryLocation(resource.type(), SECONDARY_MESSAGE));
        }
      }, () -> ctx.reportIssue(resource.type(), OMITTING_MESSAGE));
  }
}
