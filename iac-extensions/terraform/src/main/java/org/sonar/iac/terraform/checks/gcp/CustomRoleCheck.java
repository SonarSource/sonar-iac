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

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;

import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.matchesPattern;

@Rule(key = "S6408")
public class CustomRoleCheck extends AbstractNewResourceCheck {

  private static final String MESSAGE = "Make sure that using a permission that allows privilege escalation is safe here.";

  private static final Set<String> SENSITIVE_LABELS = Stream.of("cloudbuild.builds.create",
    "cloudfunctions.functions.create",
    "cloudfunctions.functions.update",
    "cloudscheduler.jobs.create",
    "composer.environments.create",
    "compute.instances.create",
    "dataflow.jobs.create",
    "dataproc.clusters.create",
    "deploymentmanager.deployments.create",
    "iam.roles.update",
    "iam.serviceAccountKeys.create",
    "iam.serviceAccounts.actAs",
    "iam.serviceAccounts.getAccessToken",
    "iam.serviceAccounts.getOpenIdToken",
    "iam.serviceAccounts.implicitDelegation",
    "iam.serviceAccounts.signBlob",
    "iam.serviceAccounts.signJwt",
    "orgpolicy.policy.set",
    "run.services.create",
    "serviceusage.apiKeys.create",
    "serviceusage.apiKeys.list",
    "storage.hmacKeys.create")
    .map(String::toLowerCase)
    .collect(Collectors.toSet());

  private static final Predicate<ExpressionTree> SENSITIVE_LABEL_PREDICATE = expression -> TextUtils
    .matchesValue(expression, s -> SENSITIVE_LABELS.contains(s.toLowerCase(Locale.ROOT))).isTrue();

  @Override
  protected void registerResourceConsumer() {
    register(List.of("google_organization_iam_custom_role", "google_project_iam_custom_role"),
      resource -> resource.list("permissions")
        .reportItemIf(SENSITIVE_LABEL_PREDICATE, MESSAGE)
        .reportItemIf(matchesPattern(".*\\.setIamPolicy"), MESSAGE));
  }
}
