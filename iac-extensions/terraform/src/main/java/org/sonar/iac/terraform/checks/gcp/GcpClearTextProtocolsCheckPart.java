/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import static org.sonar.iac.terraform.checks.ClearTextProtocolsCheck.MESSAGE_CLEAR_TEXT;
import static org.sonar.iac.terraform.checks.ClearTextProtocolsCheck.MESSAGE_OMITTING;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.equalTo;

public class GcpClearTextProtocolsCheckPart extends AbstractNewResourceCheck {

  private static final String CTX_GCP_LOAD_BALANCERS = "gcp_load_balancers";

  @Override
  protected void registerResourceConsumer() {
    register("google_compute_region_backend_service", resource -> resource
      .attribute("protocol")
      .reportIf(equalTo("HTTP"), MESSAGE_CLEAR_TEXT, CTX_GCP_LOAD_BALANCERS)
      .reportIfAbsent(MESSAGE_OMITTING, CTX_GCP_LOAD_BALANCERS));
  }

}
