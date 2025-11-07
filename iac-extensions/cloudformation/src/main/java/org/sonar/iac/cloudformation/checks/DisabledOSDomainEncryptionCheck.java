/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import javax.annotation.CheckForNull;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.yaml.tree.YamlTree;

@Rule(key = "S6308")
public class DisabledOSDomainEncryptionCheck extends AbstractResourceCheck {

  private enum SensitiveDomains {
    ELASTICSEARCH("Elasticsearch"),
    OPENSEARCH("OpenSearch Service");

    final String messageSubstitution;

    SensitiveDomains(String messageSubstitution) {
      this.messageSubstitution = messageSubstitution;
    }
  }

  private static final String MESSAGE = "Make sure that using unencrypted %s domains is safe here.";
  private static final String OMITTING_MESSAGE = "Omitting \"EncryptionAtRestOptions.Enabled\" disables %s domains encryption. Make sure it is safe here.";
  private static final String SECONDARY_MESSAGE = "Related domain";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    SensitiveDomains sensitiveDomain = checkForSensitiveDomain(resource);
    if (sensitiveDomain == null) {
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
          ctx.reportIssue(enabled.key(), MESSAGE.formatted(sensitiveDomain.messageSubstitution), new SecondaryLocation(resourceType, SECONDARY_MESSAGE));
        }
        return;
      }
      ctx.reportIssue(encryption.key(), OMITTING_MESSAGE.formatted(sensitiveDomain.messageSubstitution), new SecondaryLocation(resourceType, SECONDARY_MESSAGE));
      return;
    }
    ctx.reportIssue(resourceType, OMITTING_MESSAGE.formatted(sensitiveDomain.messageSubstitution));
  }

  @CheckForNull
  private static SensitiveDomains checkForSensitiveDomain(Resource resource) {
    if (resource.isType("AWS::Elasticsearch::Domain")) {
      return SensitiveDomains.ELASTICSEARCH;
    } else if (resource.isType("AWS::OpenSearchService::Domain")) {
      return SensitiveDomains.OPENSEARCH;
    }
    return null;
  }

}
