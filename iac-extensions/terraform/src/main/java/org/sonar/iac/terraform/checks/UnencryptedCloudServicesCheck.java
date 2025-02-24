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

import java.util.List;
import org.sonar.check.Rule;

import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.equalTo;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isFalse;

@Rule(key = "S6388")
public class UnencryptedCloudServicesCheck extends AbstractNewResourceCheck {
  public static final String UNENCRYPTED_MESSAGE = "Make sure using unencrypted cloud storage is safe here.";
  public static final String FORMAT_OMITTING = "Omitting \"%s\" enables clear-text storage. Make sure it is safe here.";

  @Override
  protected void registerResourceConsumer() {
    register("azurerm_data_lake_store",
      resource -> resource.attribute("encryption_state")
        .reportIf(equalTo("Disabled"), UNENCRYPTED_MESSAGE));

    register("azurerm_managed_disk",
      resource -> resource.attribute("disk_encryption_set_id")
        .reportIfAbsent(FORMAT_OMITTING));

    register("azurerm_mysql_server",
      resource -> resource.attribute("infrastructure_encryption_enabled")
        .reportIf(isFalse(), UNENCRYPTED_MESSAGE)
        .reportIfAbsent(FORMAT_OMITTING));

    register("azurerm_windows_virtual_machine_scale_set",
      resource -> resource.attribute("encryption_at_host_enabled")
        .reportIf(isFalse(), UNENCRYPTED_MESSAGE)
        .reportIfAbsent(FORMAT_OMITTING));

    register("azurerm_windows_virtual_machine_scale_set",
      resource -> List.of("os_disk", "data_disk")
        .forEach(blockName -> resource.block(blockName)
          .attribute("disk_encryption_set_id")
          .reportIfAbsent(FORMAT_OMITTING)));
  }
}
