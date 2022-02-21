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
package org.sonar.iac.terraform.checks.gcp;

import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;

import static org.sonar.iac.terraform.checks.PublicNetworkAccessCheck.NETWORK_ACCESS_MESSAGE;
import static org.sonar.iac.terraform.checks.PublicNetworkAccessCheck.OMITTED_MESSAGE;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isFalse;

public class GcpPublicNetworkAccessCheckPart extends AbstractNewResourceCheck {

  @Override
  protected void registerResourceConsumer() {
    register("google_cloudbuild_worker_pool",
      resource -> resource.block("worker_config")
        .reportIfAbsent(OMITTED_MESSAGE)
        .attribute("no_external_ip")
          .reportIf(isFalse(), NETWORK_ACCESS_MESSAGE)
          .reportIfAbsent(OMITTED_MESSAGE));

    register("google_compute_instance",
      resource -> resource.blocks("network_interface").forEach(
        block -> {
          block.block("access_config").report(NETWORK_ACCESS_MESSAGE);
          block.block("ipv6_access_config").report(NETWORK_ACCESS_MESSAGE);
        }
      ));

    register("google_notebooks_instance",
      resource -> resource.attribute("no_public_ip")
        .reportIf(isFalse(), NETWORK_ACCESS_MESSAGE)
        .reportIfAbsent(OMITTED_MESSAGE));
  }
}
