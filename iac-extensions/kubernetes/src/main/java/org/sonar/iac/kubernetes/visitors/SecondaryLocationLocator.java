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
package org.sonar.iac.kubernetes.visitors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;
import org.sonar.iac.helm.tree.utils.GoTemplateAstHelper;
import org.sonar.iac.helm.tree.utils.ValuePath;
import org.sonar.iac.kubernetes.plugin.filesystem.FileSystemProvider;

public final class SecondaryLocationLocator {
  private static final Logger LOG = LoggerFactory.getLogger(SecondaryLocationLocator.class);
  private static final YamlParser PARSER = new YamlParser();

  private SecondaryLocationLocator() {
  }

  public static List<SecondaryLocation> findSecondaryLocationsInAdditionalFiles(InputFileContext inputFileContext, TextRange shiftedTextRange,
    FileSystemProvider fileSystemProvider) {
    if (inputFileContext instanceof HelmInputFileContext helmContext) {
      LOG.trace("Find secondary location for issue in additional files for textRange {} in file {}", shiftedTextRange, inputFileContext.inputFile);
      return new ArrayList<>(doFindSecondaryLocationsInAdditionalFiles(helmContext, shiftedTextRange, fileSystemProvider));
    }
    return new ArrayList<>();
  }

  static List<SecondaryLocation> doFindSecondaryLocationsInAdditionalFiles(HelmInputFileContext helmContext, TextRange primaryLocationTextRange,
    FileSystemProvider fileSystemProvider) {
    var ast = helmContext.getGoTemplateTree();
    if (ast == null || helmContext.getHelmProjectDirectory() == null || helmContext.getValuesFilePath() == null) {
      return List.of();
    }

    var valuesFromProjectRootPath = helmContext.getHelmProjectDirectory().resolve("values.yaml");
    var valuesFilePath = fileSystemProvider.getBasePath().relativize(valuesFromProjectRootPath).normalize().toString();
    var secondaryLocations = new ArrayList<SecondaryLocation>();
    var valuePaths = GoTemplateAstHelper.findValuePaths(ast, primaryLocationTextRange);
    for (ValuePath valuePath : valuePaths) {
      var secondaryTextRange = toTextRangeInValuesFile(valuePath, helmContext);
      if (secondaryTextRange != null) {
        secondaryLocations.add(new SecondaryLocation(secondaryTextRange, "This value is used in a noncompliant part of a template", valuesFilePath));
      }
    }
    return secondaryLocations;
  }

  @CheckForNull
  static TextRange toTextRangeInValuesFile(ValuePath valuePath, HelmInputFileContext inputFileContext) {
    var valuesFileContent = inputFileContext.getValuesFile();
    var valuesFileTree = buildTreeFrom(valuesFileContent);
    var path = filteredPaths(valuePath);

    if (valuesFileTree == null || valuesFileTree.documents().isEmpty() || path.isEmpty()) {
      return null;
    }

    // Hopefully, values.yaml contains only a single document
    var node = valuesFileTree.documents().get(0);
    for (String pathPart : path) {
      for (Tree child : node.children()) {
        node = findByKey(child, pathPart);
        if (node != null) {
          break;
        }
      }
      if (node == null) {
        return null;
      }
    }
    return node.textRange();
  }

  @CheckForNull
  private static FileTree buildTreeFrom(@Nullable String yamlFileContent) {
    if (yamlFileContent != null && !yamlFileContent.isBlank()) {
      return PARSER.parse(yamlFileContent, null);
    }
    return null;
  }

  private static List<String> filteredPaths(ValuePath valuePath) {
    var path = valuePath.path();
    return switch (valuePath.path().get(0)) {
      case "Values" -> path.subList(1, path.size());
      case "Chart", "Release" ->
        // these come not from values.yaml
        List.of();
      default -> valuePath.path();
    };
  }

  @CheckForNull
  private static YamlTree findByKey(Tree node, String key) {
    if (node instanceof TupleTree tuple &&
      tuple.key() instanceof ScalarTree scalarTree &&
      scalarTree.value().equals(key)) {
      return tuple.value();
    }
    return null;
  }
}
