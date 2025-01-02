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
package org.sonar.iac.cloudformation.checks;

import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.yaml.tree.YamlTree;

@Rule(key = "S6308")
public class DisabledESDomainEncryptionCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure that using unencrypted Elasticsearch domains is safe here.";
  private static final String OMITTING_MESSAGE = "Omitting \"EncryptionAtRestOptions.Enabled\" disables Elasticsearch domains encryption. Make sure it is safe here.";
  private static final String SECONDARY_MESSAGE = "Related domain";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (!resource.isType("AWS::Elasticsearch::Domain")) {
      return;
    }
    YamlTree resourceType = resource.type();
    Optional<PropertyTree> maybeEncryption = PropertyUtils.get(resource.properties(), "EncryptionAtRestOptions");
    if (maybeEncryption.isPresent()) {
      PropertyTree encryption = maybeEncryption.get();
      Optional<PropertyTree> maybeEnabled = PropertyUtils.get(encryption.value(), "Enabled");
      if (maybeEnabled.isPresent()) {
        PropertyTree enabled = maybeEnabled.get();
        if (TextUtils.isValueFalse(enabled.value())) {
          ctx.reportIssue(enabled.key(), MESSAGE, new SecondaryLocation(resourceType, SECONDARY_MESSAGE));
        }
        return;
      }
      ctx.reportIssue(encryption.key(), OMITTING_MESSAGE, new SecondaryLocation(resourceType, SECONDARY_MESSAGE));
      return;
    }
    ctx.reportIssue(resourceType, OMITTING_MESSAGE);
  }
}
