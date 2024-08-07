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
    "InSubfolder/automount_service_account_token_pod_linked.yaml,InSubfolder/subfolder/linked_account_service_token.yaml",
    "NoNamespace/automount_service_account_token_pod_linked.yaml,NoNamespace/linked_account_service_token.yaml",
    "SameNamespace/automount_service_account_token_pod_linked.yaml,SameNamespace/linked_account_service_token.yaml",
  })
  void testLinkedAccountCompliant(String pod, String linkedAccount) {
    String rootFolder = "AutomountServiceAccountTokenCheck/LinkedAccount/Compliant/";
    KubernetesVerifier.verifyNoIssue(rootFolder + pod, check, rootFolder + linkedAccount);
  }

  @ParameterizedTest
  @CsvSource(value = {
    "DifferentName/automount_service_account_token_pod_linked.yaml,DifferentName/linked_account_service_token.yaml",
    "DifferentNamespace/automount_service_account_token_pod_linked.yaml,DifferentNamespace/linked_account_service_token.yaml",
    "InParentFolder/subfolder/automount_service_account_token_pod_linked.yaml,InParentFolder/linked_account_service_token.yaml",
    "InvalidAccountName/automount_service_account_token_pod_linked.yaml,InvalidAccountName/linked_account_service_token.yaml",
    "MissingValue/automount_service_account_token_pod_linked.yaml,MissingValue/linked_account_service_token.yaml",
    "SensitiveValue/automount_service_account_token_pod_linked.yaml,SensitiveValue/linked_account_service_token.yaml",
  })
  void testLinkedNonAccountCompliant(String pod, String linkedAccount) {
    String rootFolder = "AutomountServiceAccountTokenCheck/LinkedAccount/NonCompliant/";
    KubernetesVerifier.verify(rootFolder + pod, check, rootFolder + linkedAccount);
  }

  @Test
  void testLinkedAccountNonCompliantChartWithShiftedLocation() {
    var secondaryLocation1 = new SecondaryLocation(TextRanges.range(10, 0, 10, 54), "Through this service account");
    var secondaryLocation2 = new SecondaryLocation(TextRanges.range(4, 0, 4, 72), "Change this setting",
      "AutomountServiceAccountTokenCheck/LinkedAccount/NonCompliant/SensitiveValueChart/templates/linked_account_service_token.yaml");
    var expectedIssue = new Verifier.Issue(TextRanges.range(6, 0, 6, 4), "Set automountServiceAccountToken to false for this specification of kind Pod.",
      List.of(secondaryLocation1, secondaryLocation2));
    KubernetesVerifier.verify("AutomountServiceAccountTokenCheck/LinkedAccount/NonCompliant/SensitiveValueChart/templates/automount_service_account_token_pod_linked.yaml", check,
      List.of(expectedIssue));
  }

  @Test
  void shouldRaiseSecondaryLocationOnLinkedAccount() {
    String root = "AutomountServiceAccountTokenCheck/LinkedAccount/NonCompliant/SensitiveValue/";
    var secondaryLocation1 = new SecondaryLocation(TextRanges.range(10, 22, 10, 45), "Through this service account");
    var secondaryLocation2 = new SecondaryLocation(TextRanges.range(6, 30, 6, 34), "Change this setting", root + "linked_account_service_token.yaml");
    Verifier.Issue expectedIssue = new Verifier.Issue(TextRanges.range(6, 0, 6, 4), "Set automountServiceAccountToken to false for this specification of kind Pod.",
      List.of(secondaryLocation1, secondaryLocation2));
    KubernetesVerifier.verify(root + "automount_service_account_token_pod_linked.yaml", check, List.of(expectedIssue), root + "linked_account_service_token.yaml");
  }
}
