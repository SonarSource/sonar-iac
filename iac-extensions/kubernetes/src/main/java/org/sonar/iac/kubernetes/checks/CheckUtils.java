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

import java.util.Optional;
import javax.annotation.CheckForNull;
import org.sonar.iac.common.yaml.object.BlockObject;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.TupleTree;

public final class CheckUtils {

  private CheckUtils() {
  }

  /**
   * Retrieve the namespace of the document from the `metadata.namespace` attribute.<br/>
   * If it is not set, the objects are installed in the namespace `default`. However, a namespace can be set during deployment using the
   * `--namespace [custom-name]` flag. Because of that, an empty string is returned if the namespace is not set.
   */
  public static String retrieveNamespace(BlockObject document) {
    return Optional.ofNullable(retrieveAttributeAsString(document, "metadata", "namespace"))
      .orElse("");
  }

  @CheckForNull
  public static String retrieveAttributeAsString(BlockObject document, String... path) {
    BlockObject block = document;
    for (var i = 0; i < path.length - 1; i++) {
      block = block.block(path[i]);
    }
    return Optional.ofNullable(block.attribute(path[path.length - 1]).tree)
      .map(TupleTree::value)
      .filter(ScalarTree.class::isInstance)
      .map(ScalarTree.class::cast)
      .map(ScalarTree::value)
      .orElse(null);
  }
}
