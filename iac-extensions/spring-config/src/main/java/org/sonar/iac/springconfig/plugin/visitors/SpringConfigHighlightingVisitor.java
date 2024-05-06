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
package org.sonar.iac.springconfig.plugin.visitors;

import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.SyntaxHighlightingVisitor;
import org.sonar.iac.springconfig.plugin.SpringConfigSensor;
import org.sonar.iac.springconfig.tree.api.Scalar;
import org.sonar.iac.springconfig.tree.api.Tuple;

import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.STRING;
import static org.sonar.iac.springconfig.tree.utils.SpringConfigUtils.getStringValue;

public class SpringConfigHighlightingVisitor extends SyntaxHighlightingVisitor {
  @Override
  protected void languageSpecificHighlighting() {
    register(Tuple.class, (InputFileContext ctx, Tuple tree) -> {
      // When flattening the YAML tree into SpringConfig representation, we sometimes produce overlapping text ranges.
      // Example: key of a YAML array. To be on a safe side, let's only highlight keys of properties in properties files now.
      if (SpringConfigSensor.isPropertiesFile(ctx)) {
        highlight(tree.key(), KEYWORD);
      }
    });
    register(Scalar.class, (ctx, tree) -> ctx.ancestors().stream().findFirst()
      // we always build the tree in such a way that Scalars are children of Tuples
      .map(Tuple.class::cast)
      .filter(tuple -> !tuple.key().equals(tree))
      .filter(tuple -> !hasAbsentOrEmptyValue(tuple))
      .ifPresent(ignored -> highlight(tree, STRING)));
  }

  private static boolean hasAbsentOrEmptyValue(Tuple tuple) {
    var stringValue = getStringValue(tuple);
    return stringValue == null || stringValue.isEmpty();
  }
}
