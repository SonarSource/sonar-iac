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
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.terraform.checks.aws.AwsClearTextProtocolsCheckPart;
import org.sonar.iac.terraform.checks.azure.AzureClearTextProtocolsCheckPart;
import org.sonar.iac.terraform.checks.gcp.GcpClearTextProtocolsCheckPart;

@Rule(key = "S5332")
public class ClearTextProtocolsCheck implements IacCheck {

  public static final String MESSAGE_CLEAR_TEXT = "Make sure allowing clear-text traffic is safe here.";
  public static final String MESSAGE_OMITTING = "Omitting \"%s\" enables clear-text traffic. Make sure it is safe here.";

  @Override
  public void initialize(InitContext init) {
    new AwsClearTextProtocolsCheckPart().initialize(init);
    new AzureClearTextProtocolsCheckPart().initialize(init);
    new GcpClearTextProtocolsCheckPart().initialize(init);
  }
}
