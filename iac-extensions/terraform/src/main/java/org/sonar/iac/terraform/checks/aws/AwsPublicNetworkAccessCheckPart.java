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
package org.sonar.iac.terraform.checks.aws;

import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;

import static org.sonar.iac.terraform.checks.PublicNetworkAccessCheck.NETWORK_ACCESS_MESSAGE;
import static org.sonar.iac.terraform.checks.PublicNetworkAccessCheck.OMITTING_MESSAGE;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isTrue;

public class AwsPublicNetworkAccessCheckPart extends AbstractNewResourceCheck {

  private static final String SECONDARY_INSTANCE_MESSAGE = "Related instance";
  private static final String SECONDARY_TEMPLATE_MESSAGE = "Related template";

  @Override
  protected void registerResourceConsumer() {
    register("aws_dms_replication_instance",
      resource -> resource.attribute("publicly_accessible")
        .reportIfAbsent(OMITTING_MESSAGE)
        .reportIf(isTrue(), NETWORK_ACCESS_MESSAGE, resource.toSecondary(SECONDARY_INSTANCE_MESSAGE)));

    register("aws_instance",
      resource -> resource.attribute("associate_public_ip_address")
        .reportIfAbsent(OMITTING_MESSAGE)
        .reportIf(isTrue(), NETWORK_ACCESS_MESSAGE, resource.toSecondary(SECONDARY_INSTANCE_MESSAGE)));

    register("aws_launch_template",
      resource -> resource.block("network_interfaces")
        .reportIfAbsent(String.format(OMITTING_MESSAGE, "network_interfaces.associate_public_ip_address"))
        .attribute("associate_public_ip_address")
        .reportIfAbsent(OMITTING_MESSAGE, resource.toSecondary(SECONDARY_TEMPLATE_MESSAGE))
        .reportIf(isTrue(), NETWORK_ACCESS_MESSAGE, resource.toSecondary(SECONDARY_TEMPLATE_MESSAGE)));
  }
}
