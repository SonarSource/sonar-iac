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

import java.util.List;
import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;

@Rule(key = "S6413")
public class ShortLogRetentionCheck extends AbstractNewResourceCheck {

  private static final String MESSAGE = "Make sure that defining a short log retention duration is safe here.";
  private static final int DEFAULT = 14;

  @RuleProperty(
    key = "log_retention_duration",
    defaultValue = "" + DEFAULT)
  int logRetentionDuration = DEFAULT;

  @Override
  protected void registerResourceConsumer() {
    register(List.of("google_logging_project_bucket_config",
        "google_logging_billing_account_bucket_config",
        "google_logging_organization_bucket_config",
        "google_logging_folder_bucket_config"),
      resource -> resource.attribute("retention_days")
        .reportIf(isTooShortRetention(), MESSAGE));
  }

  private Predicate<ExpressionTree> isTooShortRetention() {
    return expression -> TextUtils.getIntValue(expression)
      .filter(retention -> retention > 0 && retention < logRetentionDuration)
      .isPresent();
  }

}
