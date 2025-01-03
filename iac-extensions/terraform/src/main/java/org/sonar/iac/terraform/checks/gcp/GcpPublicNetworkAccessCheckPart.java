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
package org.sonar.iac.terraform.checks.gcp;

import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;

import static org.sonar.iac.terraform.checks.PublicNetworkAccessCheck.NETWORK_ACCESS_MESSAGE;
import static org.sonar.iac.terraform.checks.PublicNetworkAccessCheck.OMITTING_MESSAGE;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isFalse;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isTrue;

public class GcpPublicNetworkAccessCheckPart extends AbstractNewResourceCheck {

  @Override
  protected void registerResourceConsumer() {
    register("google_cloudbuild_worker_pool",
      resource -> resource.block("worker_config")
        .reportIfAbsent(String.format(OMITTING_MESSAGE, "worker_config.no_external_ip"))
        .attribute("no_external_ip")
        .reportIf(isFalse(), NETWORK_ACCESS_MESSAGE)
        .reportIfAbsent(OMITTING_MESSAGE));

    register("google_compute_instance",
      resource -> resource.blocks("network_interface").forEach(
        block -> {
          block.block("access_config").report(NETWORK_ACCESS_MESSAGE);
          block.block("ipv6_access_config").report(NETWORK_ACCESS_MESSAGE);
        }));

    register("google_notebooks_instance",
      resource -> resource.attribute("no_public_ip")
        .reportIf(isFalse(), NETWORK_ACCESS_MESSAGE)
        .reportIfAbsent(OMITTING_MESSAGE));

    register("google_sql_database_instance",
      resource -> resource.block("settings").block("ip_configuration").attribute("ipv4_enabled")
        .reportIf(isTrue(), NETWORK_ACCESS_MESSAGE));
  }
}
