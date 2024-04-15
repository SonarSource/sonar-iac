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
package org.sonar.iac.arm.checks.elementsorder;

import java.util.Locale;
import java.util.Map;
import org.sonar.iac.arm.tree.impl.json.FileImpl;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.TupleTree;

public class ElementsOrderTopLevelJson implements IacCheck {
  private static final String MESSAGE = "Reorder the elements to match the recommended order.";

  private static final Map<String, Integer> topLevelJsonElements = Map.of(
    "$schema", 0,
    "contentversion", 1,
    "metadata", 2,
    "apiprofile", 3,
    "parameters", 4,
    "functions", 5,
    "variables", 6,
    "resources", 7,
    "outputs", 8);

  @Override
  public void initialize(InitContext init) {
    init.register(FileImpl.class, ElementsOrderTopLevelJson::checkTopLevelJson);

  }

  private static void checkTopLevelJson(CheckContext checkContext, FileImpl file) {
    var document = file.document();
    var elements = document.elements().stream()
      .map(TupleTree::key)
      .filter(ScalarTree.class::isInstance)
      .map(ScalarTree.class::cast)
      .filter(tree -> topLevelJsonElements.containsKey(tree.value().toLowerCase(Locale.ROOT)))
      .toList();
    var prevIndex = 0;
    for (ScalarTree element : elements) {
      var index = topLevelJsonElements.get(element.value().toLowerCase(Locale.ROOT));
      if (index < prevIndex) {
        checkContext.reportIssue(element, MESSAGE);
        break;
      }
      prevIndex = index;
    }
  }
}
