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

import static org.sonar.iac.common.yaml.TreePredicates.isTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.yaml.TreePredicates;
import org.sonar.iac.common.yaml.object.AttributeObject;
import org.sonar.iac.common.yaml.object.BlockObject;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.YamlTree;
import org.sonar.iac.kubernetes.model.ServiceAccount;

@Rule(key = "S6865")
public class AutomountServiceAccountTokenCheck extends AbstractResourceManagementCheck<ServiceAccount> {
  private static final String MESSAGE = "Set automountServiceAccountToken to false for this specification of kind %s.";
  private static final String KEY = "automountServiceAccountToken";
  private static final String KIND_POD = "Pod";
  private static final List<String> KIND_WITH_TEMPLATE = List.of("DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet", "CronJob");

  @Override
  void registerObjectCheck() {
    register(KIND_POD, document -> checkAndReport(document, String.format(MESSAGE, KIND_POD)));

    for (String kind : KIND_WITH_TEMPLATE) {
      register(kind, (BlockObject document) -> checkAndReport(document.block("spec").block("template"), String.format(MESSAGE, kind)));
    }
  }

  @Override
  Class<ServiceAccount> getGlobalResourceType() {
    return ServiceAccount.class;
  }

  private void checkAndReport(BlockObject blockObject, String message) {
    var specAsBlockObject = blockObject.block("spec");
    var specAsAttributeObject = blockObject.attribute("spec");
    if (specAsAttributeObject.tree != null && isContainersPresentInSpecBlock(specAsBlockObject)) {
      var tokenAttribute = specAsBlockObject.attribute(KEY);
      if (!tokenAttribute.isAbsent()) {
        tokenAttribute.reportIfValue(TreePredicates.isSet().negate(), message);
        tokenAttribute.reportIfValue(isTrue(), message);
        return;
      }

      List<ServiceAccount> linkedServiceAccounts = retrieveLinkedServiceAccount(blockObject);
      if (linkedServiceAccounts.isEmpty()) {
        specAsAttributeObject.reportOnKey(message);
      } else {
        boolean hasAtLeastOneAccountCompliant = linkedServiceAccounts.stream().anyMatch(account -> account.automountServiceAccountToken().isFalse());
        if (!hasAtLeastOneAccountCompliant) {
          reportIssueWithLinkedAccount(blockObject, message);
        }
      }
    }
  }

  private static void reportIssueWithLinkedAccount(BlockObject blockObject, String message) {
    var blockSpec = blockObject.block("spec");
    var serviceAccountNameAttr = blockSpec.attribute("serviceAccountName");
    List<SecondaryLocation> secondaryLocations = new ArrayList<>();
    secondaryLocations.add(new SecondaryLocation(serviceAccountNameAttr.tree.value(), "Through this service account"));
    // TODO SONARIAC-1532: put back the code to add in the secondary location the field 'automountServiceAccountToken' of the linked account
    blockObject.attribute("spec").reportOnKey(message, secondaryLocations);
  }

  private static boolean isContainersPresentInSpecBlock(BlockObject blockObject) {
    return blockObject.attribute("containers").tree != null;
  }

  private List<ServiceAccount> retrieveLinkedServiceAccount(BlockObject rootBlock) {
    AttributeObject serviceAccountNameAttr = rootBlock.block("spec").attribute("serviceAccountName");
    if (!serviceAccountNameAttr.isAbsent()) {
      YamlTree tree = serviceAccountNameAttr.tree.value();
      if (tree instanceof ScalarTree scalarTree) {
        Collection<ServiceAccount> accounts = getGlobalResources(rootBlock);
        return accounts.stream()
          .filter(account -> account.name().equals(scalarTree.value()))
          .toList();
      }
    }
    return Collections.emptyList();
  }
}
