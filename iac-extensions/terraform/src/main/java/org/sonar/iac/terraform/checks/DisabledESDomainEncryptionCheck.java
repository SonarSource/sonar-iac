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

import org.sonar.check.Rule;

import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isFalse;

@Rule(key = "S6308")
public class DisabledESDomainEncryptionCheck extends AbstractNewResourceCheck {

  private static final String MESSAGE = "Make sure that using unencrypted Elasticsearch domains is safe here.";
  private static final String OMITTING_MESSAGE = "Omitting \"encrypt_at_rest.enabled\" disables Elasticsearch domains encryption. Make sure it is safe here.";
  private static final String SECONDARY_MESSAGE = "Related domain";

  @Override
  protected void registerResourceConsumer() {
    register("aws_elasticsearch_domain",
      resource -> resource.block("encrypt_at_rest")
        .reportIfAbsent(OMITTING_MESSAGE)
        .attribute("enabled")
        .reportIfAbsent(OMITTING_MESSAGE, resource.toSecondary(SECONDARY_MESSAGE))
        .reportIf(isFalse(), MESSAGE, resource.toSecondary(SECONDARY_MESSAGE)));
  }
}
