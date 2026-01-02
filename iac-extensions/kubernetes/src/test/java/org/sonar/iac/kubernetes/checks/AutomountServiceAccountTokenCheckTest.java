/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.kubernetes.checks;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.testing.Verifier;

class AutomountServiceAccountTokenCheckTest {
  IacCheck check = new AutomountServiceAccountTokenCheck();

  @Test
  void testPodKind() {
    KubernetesVerifier.verify("AutomountServiceAccountTokenCheck/automount_service_account_token_pod.yaml", check);
  }

  @Test
  void testKindWithTemplate() {
    KubernetesVerifier.verify("AutomountServiceAccountTokenCheck/automount_service_account_token_deployment.yaml", check);
  }

  @ParameterizedTest
  @CsvSource(value = {
    "RoleBinding/service_account_role_bonded_token_pod_linked.yaml,RoleBinding/role_binding_service_token.yaml",
    "ClusterRoleBinding/service_account_role_bonded_token_pod_linked.yaml,ClusterRoleBinding/role_binding_service_token.yaml",
    "ClusterRoleBindingDifferentNamespace/service_account_role_bonded_token_pod_linked.yaml,ClusterRoleBindingDifferentNamespace/role_binding_service_token.yaml",
    "ClusterRoleBindingNoNamespace/service_account_role_bonded_token_pod_linked.yaml,ClusterRoleBindingNoNamespace/role_binding_service_token.yaml",
    "ClusterRoleBindingNoNamespaceOneSideOnly/service_account_role_bonded_token_pod_linked.yaml,ClusterRoleBindingNoNamespaceOneSideOnly/role_binding_service_token.yaml",
    "ServiceAccountLinkedInSubfolder/automount_service_account_token_pod_linked.yaml,ServiceAccountLinkedInSubfolder/subfolder/linked_account_service_token.yaml",
    "ServiceAccountLinkedNoNamespace/automount_service_account_token_pod_linked.yaml,ServiceAccountLinkedNoNamespace/linked_account_service_token.yaml",
    "ServiceAccountLinkedSameNamespace/automount_service_account_token_pod_linked.yaml,ServiceAccountLinkedSameNamespace/linked_account_service_token.yaml",
  })
  void testLinkedAccountCompliant(String pod, String linkedAccount) {
    String rootFolder = "AutomountServiceAccountTokenCheck/LinkedAccount/Compliant/";
    KubernetesVerifier.verifyNoIssue(rootFolder + pod, check, rootFolder + linkedAccount);
  }

  @ParameterizedTest
  @CsvSource(value = {
    "RoleBindingDifferentSubjectAccountName/service_account_role_bonded_token_pod_linked.yaml,RoleBindingDifferentSubjectAccountName/role_binding_service_token.yaml",
    "RoleBindingDifferentSubjectKind/service_account_role_bonded_token_pod_linked.yaml,RoleBindingDifferentSubjectKind/role_binding_service_token.yaml",
    "RoleBindingDifferentSubjectNamespace/service_account_role_bonded_token_pod_linked.yaml,RoleBindingDifferentSubjectNamespace/role_binding_service_token.yaml",
    "RoleBindingNoSubjectAccountName/service_account_role_bonded_token_pod_linked.yaml,RoleBindingNoSubjectAccountName/role_binding_service_token.yaml",
    "RoleBindingNoSubjectKind/service_account_role_bonded_token_pod_linked.yaml,RoleBindingNoSubjectKind/role_binding_service_token.yaml",
    "RoleBindingNoSubjectNamespace/service_account_role_bonded_token_pod_linked.yaml,RoleBindingNoSubjectNamespace/role_binding_service_token.yaml",
    "RoleBindingEmptySubjects/service_account_role_bonded_token_pod_linked.yaml,RoleBindingEmptySubjects/role_binding_service_token.yaml",
    "RoleBindingNoSubjects/service_account_role_bonded_token_pod_linked.yaml,RoleBindingNoSubjects/role_binding_service_token.yaml",
    "RoleBindingSubjectsElementsInvalidFormat/service_account_role_bonded_token_pod_linked.yaml,RoleBindingSubjectsElementsInvalidFormat/role_binding_service_token.yaml",
    "RoleBindingSubjectsInvalidFormat/service_account_role_bonded_token_pod_linked.yaml,RoleBindingSubjectsInvalidFormat/role_binding_service_token.yaml",
    "ServiceAccountLinkedDifferentName/automount_service_account_token_pod_linked.yaml,ServiceAccountLinkedDifferentName/linked_account_service_token.yaml",
    "ServiceAccountLinkedDifferentNamespace/automount_service_account_token_pod_linked.yaml,ServiceAccountLinkedDifferentNamespace/linked_account_service_token.yaml",
    "ServiceAccountLinkedInParentFolder/subfolder/automount_service_account_token_pod_linked.yaml,ServiceAccountLinkedInParentFolder/linked_account_service_token.yaml",
    "ServiceAccountLinkedInvalidAccountName/automount_service_account_token_pod_linked.yaml,ServiceAccountLinkedInvalidAccountName/linked_account_service_token.yaml",
    "ServiceAccountLinkedMissingValue/automount_service_account_token_pod_linked.yaml,ServiceAccountLinkedMissingValue/linked_account_service_token.yaml",
    "ServiceAccountLinkedSensitiveValue/automount_service_account_token_pod_linked.yaml,ServiceAccountLinkedSensitiveValue/linked_account_service_token.yaml",
  })
  void testLinkedNonAccountCompliant(String pod, String linkedAccount) {
    String rootFolder = "AutomountServiceAccountTokenCheck/LinkedAccount/NonCompliant/";
    KubernetesVerifier.verify(rootFolder + pod, check, rootFolder + linkedAccount);
  }

  @Test
  void testLinkedAccountNonCompliantChartWithShiftedLocation() {
    String root = "AutomountServiceAccountTokenCheck/LinkedAccount/NonCompliant/ServiceAccountLinkedSensitiveValueChart/";
    var secondaryLocation = new SecondaryLocation(TextRanges.range(4, 0, 4, 72), "Change this setting",
      root + "templates/linked_account_service_token.yaml");
    var secondaryLocationValues = new SecondaryLocation(TextRanges.range(3, 20, 3, 43), "This value is used in a noncompliant part of a template",
      root + "values.yaml");
    var expectedIssue = new Verifier.Issue(TextRanges.range(10, 25, 10, 51), "Bind this Service Account to RBAC or disable \"automountServiceAccountToken\".",
      List.of(secondaryLocation, secondaryLocationValues));
    KubernetesVerifier.verify(root + "templates/automount_service_account_token_pod_linked.yaml", check, List.of(expectedIssue));
  }

  @Test
  void shouldRaiseSecondaryLocationOnLinkedAccount() {
    String root = "AutomountServiceAccountTokenCheck/LinkedAccount/NonCompliant/ServiceAccountLinkedSensitiveValue/";
    var secondaryLocation = new SecondaryLocation(TextRanges.range(6, 30, 6, 34), "Change this setting", root + "linked_account_service_token.yaml");
    Verifier.Issue expectedIssue = new Verifier.Issue(TextRanges.range(10, 22, 10, 45), "Bind this Service Account to RBAC or disable \"automountServiceAccountToken\".",
      List.of(secondaryLocation));
    KubernetesVerifier.verify(root + "automount_service_account_token_pod_linked.yaml", check, List.of(expectedIssue), root + "linked_account_service_token.yaml");
  }
}
