/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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

import java.util.Optional;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6303")
public class DisabledRDSEncryptionCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure that using unencrypted databases is safe here.";
  private static final String SECONDARY_MESSAGE = "Related RDS DBInstance";
  private static final String STORAGE_ENCRYPTED = "StorageEncrypted";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (!resource.isType("AWS::RDS::DBInstance")) {
      return;
    }
    Optional<PropertyTree> maybeEncryption = PropertyUtils.get(resource.properties(), STORAGE_ENCRYPTED);
    if (maybeEncryption.isPresent()) {
      PropertyTree encryption = maybeEncryption.get();
      if (TextUtils.isValueFalse(encryption.value())) {
        ctx.reportIssue(encryption.key(), MESSAGE, new SecondaryLocation(resource.type(), SECONDARY_MESSAGE));
      }
    } else {
      ctx.reportIssue(resource.type(), MESSAGE);
    }
  }
}
