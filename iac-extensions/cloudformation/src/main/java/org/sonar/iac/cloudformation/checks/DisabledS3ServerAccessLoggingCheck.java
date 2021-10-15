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
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6258")
public class DisabledS3ServerAccessLoggingCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure disabling S3 server access logs is safe here.";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (isS3Bucket(resource)) {
      CloudformationTree properties = resource.properties();
      if (!PropertyUtils.value(properties, "LoggingConfiguration").isPresent() && !isMaybeLoggingBucket(properties)) {
        ctx.reportIssue(resource.type(), MESSAGE);
      }
    }
  }

  private static boolean isMaybeLoggingBucket(@Nullable CloudformationTree properties) {
    Optional<Tree> acl = PropertyUtils.value(properties, "AccessControl");
    if (acl.isPresent()) {
      Optional<String> scalarValue = TextUtils.getValue(acl.get());
      return scalarValue.map(s -> s.equals("LogDeliveryWrite")).orElse(true);
    }
    return false;
  }

}
