/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;
import org.sonar.iac.terraform.symbols.BlockSymbol;

import static org.sonar.iac.terraform.checks.DisabledLoggingCheck.MESSAGE;
import static org.sonar.iac.terraform.checks.DisabledLoggingCheck.MESSAGE_OMITTING;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.equalTo;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isFalse;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.notEqualTo;

public class GcpDisabledLoggingCheckPart extends AbstractNewResourceCheck {

  private static final List<String> DATABASE_FLAGS = List.of("log_connections", "log_disconnections", "log_checkpoints", "log_lock_waits");

  @Override
  protected void registerResourceConsumer() {
    register("google_storage_bucket",
      resource -> resource.block("logging")
        .reportIfAbsent(MESSAGE_OMITTING));

    register("google_compute_region_backend_service",
      resource -> resource.block("log_config")
        .reportIfAbsent(MESSAGE_OMITTING)
        .attribute("enable")
        .reportIf(isFalse(), MESSAGE));

    register("google_compute_subnetwork",
      resource -> resource.block("log_config")
        .reportIfAbsent(MESSAGE_OMITTING));

    register("google_container_cluster",
      resource -> resource.attribute("logging_service")
        .reportIf(equalTo("none"), MESSAGE));

    register("google_sql_database_instance",
      resource -> {
        BlockSymbol settings = resource.block("settings")
          .reportIfAbsent(MESSAGE_OMITTING);

        List<String> requiredFlags = new ArrayList<>(DATABASE_FLAGS);
        settings.blocks("database_flags").forEach(
          flags -> Optional.ofNullable(flags.attribute("name").asString())
            .filter(DATABASE_FLAGS::contains)
            .ifPresent(name -> {
              requiredFlags.remove(name);
              flags.attribute("value").reportIf(notEqualTo("on"), MESSAGE);
            }));

        if (!requiredFlags.isEmpty()) {
          settings.report(String.format(MESSAGE_OMITTING, "database_flags." + requiredFlags.get(0)));
        }
      });
  }
}
