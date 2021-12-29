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
package org.sonar.iac.terraform.checks;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.terraform.api.tree.BlockTree;

@Rule(key = "S6245")
public class DisabledS3EncryptionCheck extends AbstractResourceCheck {
  private static final String MESSAGE = "Make sure not using server-side encryption is safe here.";

  @Override
  protected void registerChecks() {
    register(DisabledS3EncryptionCheck::checkBucket, "aws_s3_bucket");
  }

  private static void checkBucket(CheckContext ctx, BlockTree resource) {
    if (PropertyUtils.isMissing(resource, "server_side_encryption_configuration")) {
      reportResource(ctx, resource, MESSAGE);
    }
  }
}
