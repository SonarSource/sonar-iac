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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.rule.RuleKey;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.visitors.ChecksVisitor;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;
import org.sonar.iac.helm.tree.utils.GoTemplateAstHelper;
import org.sonar.iac.helm.tree.utils.ValuePath;

public class AdjustableChecksVisitor extends ChecksVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(AdjustableChecksVisitor.class);
  /**
   * TODO SONARIAC-1301: Until values.yaml is published, there is no sense in enabling secondary locations in values.yaml
   */
  protected static final String ENABLE_SECONDARY_LOCATIONS_IN_VALUES_YAML_KEY = "sonar.kubernetes.internal.helm.secondaryLocationsInValuesEnable";

  private final LocationShifter locationShifter;
  private final YamlParser yamlParser;

  public AdjustableChecksVisitor(Checks<IacCheck> checks, DurationStatistics statistics, LocationShifter locationShifter, YamlParser yamlParser) {
    super(checks, statistics);
    this.locationShifter = locationShifter;
    this.yamlParser = yamlParser;
  }

  @Override
  protected InitContext context(RuleKey ruleKey) {
    return new AdjustableContextAdapter(ruleKey);
  }

  public class AdjustableContextAdapter extends ContextAdapter {

    private InputFileContext currentCtx;

    public AdjustableContextAdapter(RuleKey ruleKey) {
      super(ruleKey);
    }

    @Override
    public <T extends Tree> void register(Class<T> cls, BiConsumer<CheckContext, T> visitor) {
      AdjustableChecksVisitor.this.register(cls, statistics.time(ruleKey.rule(), (InputFileContext ctx, T tree) -> {
        currentCtx = ctx;
        visitor.accept(this, tree);
      }));
    }

    @Override
    protected void reportIssue(@Nullable TextRange textRange, String message, List<SecondaryLocation> secondaryLocations) {
      var shiftedTextRange = textRange;
      List<SecondaryLocation> enhancedAndAdjustedSecondaryLocations = new ArrayList<>();
      if (textRange != null) {
        shiftedTextRange = locationShifter.computeShiftedLocation(currentCtx, textRange);

        enhancedAndAdjustedSecondaryLocations = maybeFindSecondaryLocationsInAdditionalFiles(currentCtx, shiftedTextRange);
      }
      List<SecondaryLocation> shiftedSecondaryLocations = secondaryLocations.stream()
        .map(secondaryLocation -> locationShifter.computeShiftedSecondaryLocation(currentCtx, secondaryLocation))
        .collect(Collectors.toList());

      enhancedAndAdjustedSecondaryLocations.addAll(shiftedSecondaryLocations);
      currentCtx.reportIssue(ruleKey, shiftedTextRange, message, enhancedAndAdjustedSecondaryLocations);
    }

    List<SecondaryLocation> maybeFindSecondaryLocationsInAdditionalFiles(InputFileContext inputFileContext, TextRange shiftedTextRange) {
      if (inputFileContext instanceof HelmInputFileContext && inputFileContext.sensorContext.config().getBoolean(ENABLE_SECONDARY_LOCATIONS_IN_VALUES_YAML_KEY).orElse(false)) {
        return new ArrayList<>(findSecondaryLocationsInAdditionalFiles((HelmInputFileContext) inputFileContext, shiftedTextRange));
      }
      return new ArrayList<>();
    }

    List<SecondaryLocation> findSecondaryLocationsInAdditionalFiles(HelmInputFileContext inputFileContext, TextRange primaryLocationTextRange) {
      var ast = inputFileContext.getGoTemplateTree();
      var valuesFile = inputFileContext.getValuesFile();
      if (ast == null || valuesFile == null) {
        return List.of();
      }
      var locations = new ArrayList<SecondaryLocation>();
      try {
        var valuePaths = GoTemplateAstHelper.findNodes(ast, primaryLocationTextRange, inputFileContext.inputFile.contents());
        for (ValuePath valuePath : valuePaths) {
          var secondaryTextRange = toTextRangeInValuesFile(valuePath, inputFileContext);
          if (secondaryTextRange != null) {
            var valuesFilePath = Path.of(valuesFile.uri());
            locations.add(new SecondaryLocation(secondaryTextRange, "This value is used in a noncompliant part of a template", valuesFilePath.toString()));
          }
        }
      } catch (IOException e) {
        LOG.debug("Failed to find secondary locations in additional files", e);
      }
      return locations;
    }

    private SecondaryLocation adaptSecondaryLocation(SecondaryLocation secondaryLocation) {
      var shiftedTextRange = locationShifter.computeShiftedLocation(currentCtx, secondaryLocation.textRange);
      return new SecondaryLocation(shiftedTextRange, secondaryLocation.message, secondaryLocation.filePath);
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
          node = find(child, pathPart);
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

    private List<String> filteredPaths(ValuePath valuePath) {
      var path = valuePath.path();
      switch (valuePath.path().get(0)) {
        case "Values":
          return path.subList(1, path.size());
        case "Chart":
        case "Release":
          // these come not from values.yaml
          return List.of();
        default:
          return valuePath.path();
      }
    }

    @CheckForNull
    private YamlTree find(Tree node, String key) {
      if (node instanceof TupleTree) {
        var tuple = (TupleTree) node;
        if (tuple.key() instanceof ScalarTree && ((ScalarTree) tuple.key()).value().equals(key)) {
          return tuple.value();
        }
      }
      return null;
    }
  }
}
