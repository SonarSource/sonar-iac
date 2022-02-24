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

import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.equalTo;

public class GcpClearTextProtocolsCheckPart extends AbstractNewResourceCheck {

  private static final String MESSAGE = "Using HTTP protocol is insecure. Use HTTPS instead.";
  private static final String OMITTING_MESSAGE = "Omitting 'protocol' enables 'HTTP' by default. Use HTTPS instead.";

  @Override
  protected void registerResourceConsumer() {
    register("google_compute_region_backend_service", resource -> resource
      .attribute("protocol")
      .reportIf(equalTo("HTTP"), MESSAGE)
      .reportIfAbsent(OMITTING_MESSAGE));
  }

}
