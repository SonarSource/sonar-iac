/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
import org.sonar.iac.terraform.checks.aws.AwsPublicNetworkAccessCheckPart;
import org.sonar.iac.terraform.checks.azure.AzurePublicNetworkAccessCheckPart;
import org.sonar.iac.terraform.checks.gcp.GcpPublicNetworkAccessCheckPart;

@Rule(key = "S6329")
public class PublicNetworkAccessCheck implements IacCheck {

  public static final String NETWORK_ACCESS_MESSAGE = "Make sure allowing public network access is safe here.";
  public static final String GATEWAYS_AND_INTERFACE_MESSAGE = "Make sure it is safe to use this public IP address.";
  public static final String FIREWALL_MESSAGE = "Make sure that allowing public IP addresses is safe here.";

  public static final String OMITTING_MESSAGE = "Omitting \"%s\" allows network access from the Internet. Make sure it is safe here.";

  @Override
  public void initialize(InitContext init) {
    new AwsPublicNetworkAccessCheckPart().initialize(init);
    new AzurePublicNetworkAccessCheckPart().initialize(init);
    new GcpPublicNetworkAccessCheckPart().initialize(init);
  }
}
