/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
package org.sonar.iac.terraform.checks;

import org.sonar.api.utils.Version;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.ObjectElementTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.checks.utils.ExpressionPredicate;
import org.sonar.iac.terraform.symbols.AttributeSymbol;
import org.sonar.iac.terraform.symbols.BlockSymbol;

import static org.sonar.iac.terraform.checks.AbstractResourceCheck.S3_BUCKET;
import static org.sonar.iac.terraform.plugin.TerraformProviders.Provider.Identifier.AWS;

@Rule(key = "S6252")
public class UnversionedS3BucketCheck extends AbstractNewResourceCheck {

  private static final String MESSAGE = "Make sure using %s S3 bucket is safe here.";
  private static final String OMITTING_MESSAGE = "Omitting \"versioning\" disables S3 bucket versioning. Make sure it is safe here.";

  private static final String UNVERSIONED_MSG = String.format(MESSAGE, "unversioned");
  private static final String SUSPENDED_MSG = String.format(MESSAGE, "suspended versioned");
  private static final String SECONDARY_MESSAGE = "Related bucket";
  private static final Version AWS_V_4 = Version.create(4, 0);

  @Override
  protected void registerResourceConsumer() {
    register(S3_BUCKET, resource -> {
      SecondaryLocation secondaryLocation = resource.toSecondary(SECONDARY_MESSAGE);

      BlockSymbol versioningBlock = resource.block("versioning");
      versioningBlock.attribute("enabled")
        .reportIf(ExpressionPredicate.isFalse(), SUSPENDED_MSG, secondaryLocation)
        .reportIfAbsent(UNVERSIONED_MSG, secondaryLocation);

      AttributeSymbol versioningAttribute = resource.attribute("versioning");
      if (versioningAttribute.isPresent()) {
        checkVersionAttribute(versioningAttribute, secondaryLocation);
      }

      if (resource.provider(AWS).hasVersionLowerThan(AWS_V_4) && versioningBlock.isAbsent() && versioningAttribute.isAbsent()) {
        resource.report(OMITTING_MESSAGE);
      }
    });

    register("aws_s3_bucket_versioning", resource -> {
      SecondaryLocation secondary = resource.toSecondary(SECONDARY_MESSAGE);
      resource.block("versioning_configuration")
        .attribute("status")
        .reportIf(ExpressionPredicate.equalTo("Disabled"), UNVERSIONED_MSG, secondary)
        .reportIf(ExpressionPredicate.equalTo("Suspended"), SUSPENDED_MSG, secondary);
    });
  }

  private static void checkVersionAttribute(AttributeSymbol attribute, SecondaryLocation secondaryLocation) {
    if (attribute.tree.value().is(TerraformTree.Kind.OBJECT)) {
      PropertyUtils.get(attribute.tree.value(), "enabled", ObjectElementTree.class)
        .ifPresentOrElse(enabled -> {
          if (TextUtils.isValueFalse(enabled.value())) {
            attribute.ctx.reportIssue(enabled, SUSPENDED_MSG, secondaryLocation);
          }
        },
          () -> attribute.ctx.reportIssue(attribute.tree.key(), UNVERSIONED_MSG, secondaryLocation));
    }
  }
}
