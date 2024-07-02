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
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.testing.Verifier;
import org.sonar.iac.common.yaml.object.AttributeObject;
import org.sonar.iac.common.yaml.object.BlockObject;
import org.sonar.iac.kubernetes.model.LimitRange;
import org.sonar.iac.kubernetes.visitors.KubernetesCheckContext;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class KubernetesVerifierTest {

  private static final String PRIMARY_FILE_PATH = "KubernetesVerifierCheck/helm/templates/pod.yaml";
  private static final SecondaryLocation SECONDARY_IN_VALUES = new SecondaryLocation(
    range(2, 9, 2, 21),
    "in values file",
    "KubernetesVerifierCheck/values.yaml");
  private static final SecondaryLocation SECONDARY_IN_PRIMARY_FILE = new SecondaryLocation(
    range(1, 0, 1, 1),
    "In template file itself");
  private static final SecondaryLocation SECONDARY_IN_PRIMARY_WITH_FILE_PATH = new SecondaryLocation(
    range(2, 0, 2, 1),
    "In template file itself, filePath",
    PRIMARY_FILE_PATH);
  private static final String PRIMARY_ISSUE_MESSAGE = "Container name is missing";

  @Test
  void verifierShouldSucceedOnHelmProjectWithSecondaryLocationsRaised() {
    TextRange expectedShiftedTextRangePrimaryIssue = range(7, 15, 7, 37);

    List<SecondaryLocation> expectedSecondaryLocations = expectedSecondaryLocations();

    Verifier.Issue issue = new Verifier.Issue(expectedShiftedTextRangePrimaryIssue, PRIMARY_ISSUE_MESSAGE, expectedSecondaryLocations);
    KubernetesVerifier.verify(PRIMARY_FILE_PATH, new ContainerNamePresentCheck(), List.of(issue));
  }

  @Test
  void shouldAccessOtherFilesForPureK8s() {
    var baseDir = "KubernetesVerifierCheck/kubernetes/";

    KubernetesVerifier.verify(baseDir + "pod.yaml", new ProjectResourceVisitedCheck(), baseDir + "limit-range.yaml");
  }

  private static List<SecondaryLocation> expectedSecondaryLocations() {
    TextRange shiftedTextRangeSecondaryInPrimaryFile = range(1, 0, 1, 14);
    TextRange shiftedTextRangeSecondaryInPrimaryFileWithFilePath = range(2, 0, 2, 9);
    SecondaryLocation expectedShiftedSecondaryInPrimaryFile = new SecondaryLocation(shiftedTextRangeSecondaryInPrimaryFile, "In template file itself");
    SecondaryLocation expectedShiftedSecondaryInPrimaryFileWithFilePath = new SecondaryLocation(
      shiftedTextRangeSecondaryInPrimaryFileWithFilePath,
      "In template file itself, filePath", PRIMARY_FILE_PATH);

    return List.of(
      SECONDARY_IN_VALUES,
      expectedShiftedSecondaryInPrimaryFile,
      expectedShiftedSecondaryInPrimaryFileWithFilePath);
  }

  public static class ContainerNamePresentCheck extends AbstractKubernetesObjectCheck {
    @Override
    void registerObjectCheck() {
      register("Pod", (BlockObject pod) -> pod.blocks("containers").forEach(container -> reportIssueWithSecondaryInValuesFile(container.attribute("name"))));
    }

    @Override
    void initializeCheck(KubernetesCheckContext ctx) {
      ctx.setShouldReportSecondaryInValues(false);
    }

    private void reportIssueWithSecondaryInValuesFile(AttributeObject attribute) {
      List<SecondaryLocation> secondaryLocations = List.of(SECONDARY_IN_VALUES, SECONDARY_IN_PRIMARY_FILE, SECONDARY_IN_PRIMARY_WITH_FILE_PATH);
      attribute.ctx.reportIssue(attribute.tree, PRIMARY_ISSUE_MESSAGE, secondaryLocations);
    }
  }

  private static class ProjectResourceVisitedCheck extends AbstractKubernetesObjectCheck {
    @Override
    void registerObjectCheck() {
      register("Pod", (BlockObject pod) -> {
        var projectContext = ((KubernetesCheckContext) pod.ctx).projectContext();
        var currentContext = ((KubernetesVerifier.KubernetesTestContext) pod.ctx).inputFileContext();
        var resources = projectContext.getProjectResources("", currentContext, LimitRange.class);
        if (!resources.isEmpty()) {
          pod.ctx.reportIssue(
            TextRanges.range(2, 0, 2, 10),
            "LimitRange is present");
        }
      });
    }
  }
}
