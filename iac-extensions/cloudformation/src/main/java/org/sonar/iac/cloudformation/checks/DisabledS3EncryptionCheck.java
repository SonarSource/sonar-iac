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

import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.cloudformation.checks.utils.MappingTreeUtils;
import org.sonar.iac.common.api.checks.CheckContext;

@Rule(key = "S6245")
public class DisabledS3EncryptionCheck extends AbstractResourceCheck {
  private static final String MESSAGE = "Make sure not using server-side encryption is safe here.";

  @Override
  protected void checkResource(CheckContext ctx, AbstractResourceCheck.Resource resource) {
    if (isS3Bucket(resource.type()) && isBucketEncrypted(resource)) {
      ctx.reportIssue(resource.type(), MESSAGE);
    }
  }

  private boolean isBucketEncrypted(AbstractResourceCheck.Resource resource) {
    return resource.properties() instanceof MappingTree && !MappingTreeUtils.getValue((MappingTree) resource.properties(), "BucketEncryption").isPresent();
  }

  private boolean isS3Bucket(CloudformationTree type) {
    return type instanceof ScalarTree && "AWS::S3::Bucket".equalsIgnoreCase(((ScalarTree) type).value());
  }
}
