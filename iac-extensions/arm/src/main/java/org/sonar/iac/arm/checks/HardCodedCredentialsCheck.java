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
    "administratorLoginPassword",
    "password",
    "secret",
    "adminPassword",
    "publishingPassword");

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

  /** TODO SONARIAC-1420: S6437 should raise when credential is not defined via parameter */
  private static boolean isHardcoded(Tree tree) {
    return TextUtils.matchesValue(tree, String::isBlank).isFalse();
  }
}
