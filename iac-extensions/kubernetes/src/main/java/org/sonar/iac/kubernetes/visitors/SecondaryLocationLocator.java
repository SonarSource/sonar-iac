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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
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

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SecondaryLocationLocator {
  private static final Logger LOG = LoggerFactory.getLogger(SecondaryLocationLocator.class);
  private final YamlParser yamlParser;

  public SecondaryLocationLocator(YamlParser yamlParser) {
    this.yamlParser = yamlParser;
  }

  public List<SecondaryLocation> findSecondaryLocationsInAdditionalFiles(InputFileContext inputFileContext, TextRange shiftedTextRange) {
    if (inputFileContext instanceof HelmInputFileContext helmContext) {
      return new ArrayList<>(doFindSecondaryLocationsInAdditionalFiles(helmContext, shiftedTextRange));
    }
    return new ArrayList<>();
  }

  List<SecondaryLocation> doFindSecondaryLocationsInAdditionalFiles(HelmInputFileContext helmContext, TextRange primaryLocationTextRange) {
    var ast = helmContext.getGoTemplateTree();
    var valuesFile = helmContext.getValuesFile();
    var sourceWithComments = helmContext.getSourceWithComments();
    if (ast == null || valuesFile == null || sourceWithComments == null) {
      return List.of();
    }
    var secondaryLocations = new ArrayList<SecondaryLocation>();
    try {
      var valuePaths = GoTemplateAstHelper.findValuePaths(ast, primaryLocationTextRange, sourceWithComments);
      for (ValuePath valuePath : valuePaths) {
        var secondaryTextRange = toTextRangeInValuesFile(valuePath, helmContext);
        if (secondaryTextRange != null) {
          secondaryLocations.add(new SecondaryLocation(secondaryTextRange, "This value is used in a noncompliant part of a template", valuesFile.toString()));
        }
      }
    } catch (IOException e) {
      LOG.debug("Failed to find secondary locations in additional file {}", valuesFile, e);
    }
    return secondaryLocations;
  }

  @CheckForNull
  TextRange toTextRangeInValuesFile(ValuePath valuePath, HelmInputFileContext inputFileContext) throws IOException {
    var valuesFile = inputFileContext.getValuesFile();
    var valuesFileTree = buildTreeFrom(valuesFile);
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
  private FileTree buildTreeFrom(@Nullable InputFile yamlFile) throws IOException {
    if (yamlFile != null) {
      var valuesFileContent = yamlFile.contents();
      if (!valuesFileContent.isBlank()) {
        return yamlParser.parse(valuesFileContent, null);
      }
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
      tuple.key()instanceof ScalarTree scalarTree &&
      scalarTree.value().equals(key)) {
      return tuple.value();
    }
    return null;
  }
}
