/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.iac.cloudformation.checks.utils;

import java.util.Optional;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.cloudformation.api.tree.TupleTree;

public class MappingTreeUtils {

  private MappingTreeUtils() {
  }

  public static Optional<CloudformationTree> getValue(MappingTree map, String key) {
    return map.elements().stream()
      .filter(e -> e.key() instanceof ScalarTree && key.equals(((ScalarTree) e.key()).value()))
      .map(TupleTree::value)
      .findFirst();
  }
}
