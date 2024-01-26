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
import org.sonar.check.Rule;
import org.sonar.iac.common.yaml.object.BlockObject;

import static org.sonar.iac.common.yaml.TreePredicates.isSet;
import static org.sonar.iac.common.yaml.TreePredicates.isTrue;

@Rule(key = "S6865")
public class AutomountServiceAccountTokenCheck extends AbstractKubernetesObjectCheck {
  private static final String MESSAGE = "Set automountServiceAccountToken to false for this specification of kind %s.";
  private static final String KEY = "automountServiceAccountToken";
  private static final String KIND_POD = "Pod";
  private static final List<String> KIND_WITH_TEMPLATE = List.of("DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet", "CronJob");

  @Override
  boolean shouldVisitWholeDocument() {
    return true;
  }

  @Override
  void registerObjectCheck() {
    register(KIND_POD, document -> checkAndReport(document, String.format(MESSAGE, KIND_POD)));

    for (String kind : KIND_WITH_TEMPLATE) {
      register(kind, (BlockObject document) -> checkAndReport(document.block("spec").block("template"), String.format(MESSAGE, kind)));
    }
  }

  private static void checkAndReport(BlockObject blockObject, String message) {
    var specAsBlockObject = blockObject.block("spec");
    var specAsAttributeObject = blockObject.attribute("spec");
    if (specAsAttributeObject.tree != null && isContainersPresentInSpecBlock(specAsBlockObject)) {
      var tokenAttribute = specAsBlockObject.attribute(KEY);
      tokenAttribute.reportIfAbsent(specAsAttributeObject.tree.key(), message);
      tokenAttribute.reportIfValue(isSet().negate(), message);
      tokenAttribute.reportIfValue(isTrue(), message);
    }
  }

  private static boolean isContainersPresentInSpecBlock(BlockObject blockObject) {
    return blockObject.attribute("containers").tree != null;
  }
}
