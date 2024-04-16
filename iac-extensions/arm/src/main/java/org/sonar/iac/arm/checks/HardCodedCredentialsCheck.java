/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.arm.checks;

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.HasProperties;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6437")
public class HardCodedCredentialsCheck implements IacCheck {

  private static final String MESSAGE = "Revoke and change this secret, as it might be compromised.";

  private static final List<String> CREDENTIAL_PROPERTIES = List.of(
    "administratorLogin",
    "administratorLoginPassword",
    "password",
    "secret",
    "adminPassword",
    "adminUsername",
    "publishingPassword",
    "publishingUserName");

  @Override
  public void initialize(InitContext init) {
    init.register(Tree.class, HardCodedCredentialsCheck::checkCredentials);
  }

  private static void checkCredentials(CheckContext ctx, Tree tree) {
    if (tree instanceof HasProperties hasProperties) {
      for (PropertyTree property : hasProperties.properties()) {
        TextUtils.getValue(property.key())
          .ifPresent(propertyName -> {
            if (CREDENTIAL_PROPERTIES.contains(propertyName) && isHardcoded(property.value())) {
              ctx.reportIssue(property, MESSAGE);
            }
          });
      }
    }
  }

  /** TODO <a href="https://sonarsource.atlassian.net/browse/SONARIAC-1420">SONARIAC-1420</a>: S6437 should raise when credential is not defined via parameter */
  private static boolean isHardcoded(Tree tree) {
    return TextUtils.matchesValue(tree, String::isBlank).isFalse();
  }
}
