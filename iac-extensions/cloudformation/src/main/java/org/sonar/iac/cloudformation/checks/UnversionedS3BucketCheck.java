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
package org.sonar.iac.cloudformation.checks;

import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;


@Rule(key = "S6252")
public class UnversionedS3BucketCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure using %s S3 bucket is safe here.";
  private static final String UNVERSIONED_MSG = "unversioned";
  private static final String SUSPENDED_MSG = "suspended versioned";
  private static final String SECONDARY_MSG = "Related bucket";

  private static final String SUSPENDED_VALUE = "Suspended";



  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (isS3Bucket(resource)) {
      checkVersioning(ctx, resource);
    }
  }

  protected void checkVersioning(CheckContext ctx, Resource resource) {
    CloudformationTree properties = resource.properties();
    Optional<Tree> versioning = PropertyUtils.value(properties, "VersioningConfiguration");
    if (versioning.isPresent()) {
      Optional<Tree> status = PropertyUtils.value(versioning.get(), "Status");
      if (status.isPresent()) {
        TextUtils.getValue(status.get()).filter(SUSPENDED_VALUE::equals).ifPresent(
         s -> ctx.reportIssue(status.get(), String.format(MESSAGE, SUSPENDED_MSG), new SecondaryLocation(resource.type(), SECONDARY_MSG)));
      } else if (properties != null){
        ctx.reportIssue(versioningKey(properties), String.format(MESSAGE, UNVERSIONED_MSG), new SecondaryLocation(resource.type(), SECONDARY_MSG));
      }
    } else {
      ctx.reportIssue(resource.type(), String.format(MESSAGE, UNVERSIONED_MSG));
    }
  }

  private static Tree versioningKey(CloudformationTree properties) {
    return PropertyUtils.key(properties, "VersioningConfiguration").orElse(properties);
  }
}
