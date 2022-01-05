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
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;

@Rule(key = "S6332")
public class DisabledEFSEncryptionCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure that using unencrypted EFS file systems is safe here.";
  private static final String SECONDARY_MESSAGE = "Related file system";

  @Override
  protected void registerResourceChecks() {
    register(DisabledEFSEncryptionCheck::checkFileSystem, "aws_efs_file_system");
  }

  private static void checkFileSystem(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "encrypted", AttributeTree.class)
      .ifPresentOrElse(encrypted -> reportOnFalse(ctx, encrypted, MESSAGE, new SecondaryLocation(resource.labels().get(0), SECONDARY_MESSAGE)),
        () -> reportResource(ctx, resource, MESSAGE));
  }
}
