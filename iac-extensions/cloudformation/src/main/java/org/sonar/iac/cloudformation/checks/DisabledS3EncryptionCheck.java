/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.cloudformation.checks;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.yaml.tree.MappingTree;

@Rule(key = "S6245")
public class DisabledS3EncryptionCheck extends AbstractResourceCheck {
  private static final String MESSAGE = "Omitting \"BucketEncryption\" disables server-side encryption. Make sure it is safe here.";

  @Override
  protected void checkResource(CheckContext ctx, AbstractResourceCheck.Resource resource) {
    if (isS3Bucket(resource) && !isBucketEncrypted(resource)) {
      ctx.reportIssue(resource.type(), MESSAGE);
    }
  }

  private static boolean isBucketEncrypted(AbstractResourceCheck.Resource resource) {
    var properties = resource.properties();
    // If properties is not a MappingTree, it is not a valid configuration, so we skip it.
    boolean shouldSkip = properties != null && !(properties instanceof MappingTree);
    return shouldSkip || PropertyUtils.value(properties, "BucketEncryption").isPresent();
  }
}
