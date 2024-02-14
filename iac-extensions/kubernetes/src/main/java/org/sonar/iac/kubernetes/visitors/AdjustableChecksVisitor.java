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
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
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
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

public class AdjustableChecksVisitor extends ChecksVisitor {

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
      if (textRange != null) {
        shiftedTextRange = locationShifter.computeShiftedLocation(currentCtx, textRange);
      }
      List<SecondaryLocation> shiftedSecondaryLocations = secondaryLocations.stream().map(this::adaptSecondaryLocation).collect(Collectors.toList());
      currentCtx.reportIssue(ruleKey, shiftedTextRange, message, shiftedSecondaryLocations);
    }

    private SecondaryLocation adaptSecondaryLocation(SecondaryLocation secondaryLocation) {
      var shiftedTextRange = locationShifter.computeShiftedLocation(currentCtx, secondaryLocation.textRange);
      return new SecondaryLocation(shiftedTextRange, secondaryLocation.message, secondaryLocation.filePath);
    }

    TextRange toLocationInValuesFile(List<String> valuePaths, HelmInputFileContext inputFileContext) throws IOException {
      var valuesFile = inputFileContext.getValuesFile();
      if (valuesFile == null) {
        return null;
      }

      var valuesFileTree = yamlParser.parse(valuesFile.contents(), null);
      // Hopefully, values.yaml contains only a single document
      var node = valuesFileTree.documents().get(0);
      for (String pathPart : valuePaths) {
        for (Tree child : node.children()) {
          node = find(child, pathPart);
          if (node == null) {
            return null;
          }
        }
      }
      return node.textRange();
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
