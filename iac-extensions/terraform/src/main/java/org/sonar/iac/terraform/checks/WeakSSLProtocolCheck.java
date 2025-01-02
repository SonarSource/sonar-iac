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
import org.sonar.iac.terraform.checks.aws.AwsWeakSSLProtocolCheckPart;
import org.sonar.iac.terraform.checks.azure.AzureWeakSSLProtocolCheckPart;
import org.sonar.iac.terraform.checks.gcp.GcpWeakSSLProtocolCheckPart;

@Rule(key = "S4423")
public class WeakSSLProtocolCheck implements IacCheck {

  public static final String WEAK_SSL_MESSAGE = "Change this code to disable support of older TLS versions.";
  public static final String OMITTING_WEAK_SSL_MESSAGE = "Set \"%s\" to disable support of older TLS versions.";

  @Override
  public void initialize(InitContext init) {
    new AwsWeakSSLProtocolCheckPart().initialize(init);
    new AzureWeakSSLProtocolCheckPart().initialize(init);
    new GcpWeakSSLProtocolCheckPart().initialize(init);
  }
}
