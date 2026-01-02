/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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
package org.sonar.iac.terraform.checks;

import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.iac.terraform.symbols.ResourceSymbol;

import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isFalse;

@Rule(key = "S6308")
public class DisabledOSDomainEncryptionCheck extends AbstractNewResourceCheck {

  private static final String MESSAGE = "Make sure that using unencrypted %s domains is safe here.";
  private static final String OMITTING_MESSAGE = "Omitting \"encrypt_at_rest.enabled\" disables %s domains encryption. Make sure it is safe here.";
  private static final String SECONDARY_MESSAGE = "Related domain";

  @Override
  protected void registerResourceConsumer() {
    register(Set.of("aws_opensearch_domain", "aws_elasticsearch_domain"),
      resource -> resource.block("encrypt_at_rest")
        .reportIfAbsent(OMITTING_MESSAGE.formatted(convertToDisplayName(resource)))
        .attribute("enabled")
        .reportIfAbsent(OMITTING_MESSAGE.formatted(convertToDisplayName(resource)), resource.toSecondary(SECONDARY_MESSAGE))
        .reportIf(isFalse(), MESSAGE.formatted(convertToDisplayName(resource)), resource.toSecondary(SECONDARY_MESSAGE)));
  }

  private static String convertToDisplayName(ResourceSymbol resource) {
    if ("aws_opensearch_domain".equals(resource.type)) {
      return "OpenSearch";
    }
    return "Elasticsearch";
  }
}
