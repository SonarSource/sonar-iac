/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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

import org.sonar.api.utils.Version;
import org.sonar.check.Rule;

import static org.sonar.iac.terraform.checks.AbstractResourceCheck.S3_BUCKET;
import static org.sonar.iac.terraform.plugin.TerraformProviders.Provider.Identifier.AWS;

@Rule(key = "S6245")
public class DisabledS3EncryptionCheck extends AbstractNewResourceCheck {

  private static final String MESSAGE = "Omitting \"server_side_encryption_configuration\" disables server-side encryption. Make sure it is safe here.";

  private static final Version AWS_V_4 = Version.create(4, 0);

  @Override
  protected void registerResourceConsumer() {
    register(S3_BUCKET, resource -> {
      if (resource.provider(AWS).hasVersionLowerThan(AWS_V_4)
        && resource.block("server_side_encryption_configuration").isAbsent()
        && resource.attribute("server_side_encryption_configuration").isAbsent()) {
        resource.report(MESSAGE);
      }
    });
  }
}
