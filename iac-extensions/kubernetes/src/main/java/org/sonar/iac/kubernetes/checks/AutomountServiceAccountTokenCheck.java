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
package org.sonar.iac.kubernetes.checks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.yaml.object.AttributeObject;
import org.sonar.iac.common.yaml.object.BlockObject;
import org.sonar.iac.common.yaml.tree.YamlTree;
import org.sonar.iac.kubernetes.model.ClusterRoleBinding;
import org.sonar.iac.kubernetes.model.RoleBinding;
import org.sonar.iac.kubernetes.model.ServiceAccount;
import org.sonar.iac.kubernetes.model.Subject;

@Rule(key = "S6865")
public class AutomountServiceAccountTokenCheck extends AbstractGlobalResourceCheck {
  private static final String MESSAGE_BIND_ACCOUNT_RESOURCE = "Bind this Service Account to RBAC or disable \"automountServiceAccountToken\".";
  private static final String MESSAGE_BIND_ACCOUNT_NAME = "Bind this resource's automounted service account to RBAC or disable automounting.";
  private static final String KIND_POD = "Pod";
  private static final String SERVICE_ACCOUNT_NAME = "serviceAccountName";
  private static final List<String> KIND_WITH_TEMPLATE = List.of("DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet", "CronJob");
  private static final Predicate<YamlTree> IS_FALSE = tree -> "false".equals(TextUtils.getValue(tree).orElse(""));

  @Override
  void registerObjectCheck() {
    register(KIND_POD, document -> checkResource(document, document.block("spec")));
    register(KIND_WITH_TEMPLATE, document -> checkResource(document, document.block("spec").block("template").block("spec")));
  }

  private void checkResource(BlockObject document, BlockObject spec) {
    if (isAutomountDisabled(spec) || isContainersAbsent(spec)) {
      return;
    }
    var namespace = CheckUtils.retrieveNamespace(document);
    var accountName = CheckUtils.retrieveAttributeAsString(spec, SERVICE_ACCOUNT_NAME);
    checkAccountSecurity(document, spec, namespace, accountName);
  }

  private static boolean isAutomountDisabled(BlockObject spec) {
    return spec.attribute("automountServiceAccountToken").isValue(IS_FALSE);
  }

  private static boolean isContainersAbsent(BlockObject spec) {
    return spec.attribute("containers").tree == null;
  }

  private void checkAccountSecurity(BlockObject document, BlockObject spec, String namespace, @Nullable String accountName) {
    // If we can find any RoleBinding or ClusterRoleBinding that references the service account, it's compliant and we stop there
    if (hasAnyBondedRole(document, namespace, accountName)) {
      return;
    }

    // If we can find a ServiceAccount of that name with automountServiceAccountToken set to false, it's compliant and we stop there
    var serviceAccounts = findGlobalResources(ServiceAccount.class, namespace, document).stream()
      .filter(serviceAccount -> serviceAccount.name().equals(accountName))
      .toList();
    if (serviceAccounts.stream().anyMatch(serviceAccount -> serviceAccount.automountServiceAccountToken().isFalse())) {
      return;
    }

    // If we reach there, then we couldn't find any security measure in place -> raise an issue
    if (serviceAccounts.isEmpty()) {
      if (accountName == null) {
        spec.attribute("containers")
          .reportOnKey(MESSAGE_BIND_ACCOUNT_NAME);
      } else {
        spec.attribute(SERVICE_ACCOUNT_NAME)
          .reportOnValue(MESSAGE_BIND_ACCOUNT_RESOURCE);
      }
    } else {
      // report on first account - there should be only one
      reportIssueWithLinkedAccount(spec.attribute(SERVICE_ACCOUNT_NAME), serviceAccounts.iterator().next());
    }
  }

  private boolean hasAnyBondedRole(BlockObject document, String namespace, @Nullable String accountName) {
    var roleBindings = findGlobalResources(RoleBinding.class, namespace, document);
    var clusterRoleBindings = findGlobalResources(ClusterRoleBinding.class, namespace, document);
    Stream<Subject> subjects = Stream.concat(
      roleBindings.stream().flatMap(roleBinding -> roleBinding.subjects().stream()),
      clusterRoleBindings.stream().flatMap(clusterRoleBinding -> clusterRoleBinding.subjects().stream()));
    return subjects.anyMatch(subject -> isValidSubject(subject, namespace, accountName));
  }

  private static boolean isValidSubject(Subject subject, String namespace, @Nullable String accountName) {
    return "ServiceAccount".equals(subject.kind()) && namespace.equals(subject.namespace()) && accountName != null && accountName.equals(subject.name());
  }

  private static void reportIssueWithLinkedAccount(AttributeObject accountNameAttribute, ServiceAccount linkedAccount) {
    List<SecondaryLocation> secondaryLocations = new ArrayList<>();
    if (linkedAccount.automountServiceAccountToken().isTrue()) {
      secondaryLocations.add(new SecondaryLocation(linkedAccount.valueLocation(), "Change this setting", linkedAccount.filePath()));
    }
    accountNameAttribute.reportOnValue(MESSAGE_BIND_ACCOUNT_RESOURCE, secondaryLocations);
  }
}
