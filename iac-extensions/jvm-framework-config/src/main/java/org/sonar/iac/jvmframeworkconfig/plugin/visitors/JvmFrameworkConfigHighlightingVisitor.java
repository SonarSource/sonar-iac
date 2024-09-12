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
package org.sonar.iac.jvmframeworkconfig.plugin.visitors;

import java.util.List;
import java.util.stream.Stream;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.SyntaxHighlightingVisitor;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;
import org.sonar.iac.jvmframeworkconfig.tree.api.File;
import org.sonar.iac.jvmframeworkconfig.tree.api.Scalar;
import org.sonar.iac.jvmframeworkconfig.tree.api.Tuple;

import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.STRING;
import static org.sonar.iac.jvmframeworkconfig.plugin.JvmFrameworkConfigSensor.isPropertiesFile;
import static org.sonar.iac.jvmframeworkconfig.tree.utils.JvmFrameworkConfigUtils.getStringValue;

public class JvmFrameworkConfigHighlightingVisitor extends SyntaxHighlightingVisitor {
  @Override
  protected void languageSpecificHighlighting() {
    register(Tuple.class, (InputFileContext ctx, Tuple tree) -> {
      // When flattening the YAML tree into SpringConfig representation, we sometimes produce overlapping text ranges.
      // Example: key of a YAML array. To be on a safe side, let's only highlight keys of properties in properties files now.
      if (isPropertiesFile(ctx)) {
        highlight(tree.key(), KEYWORD);
      }
    });
    register(File.class, (InputFileContext ctx, File tree) -> {
      var yamlTree = tree.originalYamlTree();
      if (yamlTree != null) {
        retrieveAllChildren(yamlTree)
          .filter(TupleTree.class::isInstance)
          .map(TupleTree.class::cast)
          .forEach(tuple -> highlight(tuple.key(), KEYWORD));
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

  private static Stream<YamlTree> retrieveAllChildren(YamlTree yamlTree) {
    return Stream.iterate(List.of(yamlTree),
      list -> !list.isEmpty(),
      list -> list.stream().flatMap(t -> t.children().stream().map(YamlTree.class::cast)).toList())
      .flatMap(List::stream);
  }
}
