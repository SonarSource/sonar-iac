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
package org.sonar.iac.kubernetes.plugin;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.CrossFileAnalyzer;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.regex.Pattern;

import static org.sonar.iac.common.yaml.YamlFileUtils.splitLines;

public class KubernetesAnalyzer extends CrossFileAnalyzer {
  private static final String DIRECTIVE_IN_COMMENT = "#.*\\{\\{";
  private static final String DIRECTIVE_IN_SINGLE_QUOTE = "'[^']*\\{\\{[^']*'";
  private static final String DIRECTIVE_IN_DOUBLE_QUOTE = "\"[^\"]*\\{\\{[^\"]*\"";
  private static final String CODEFRESH_VARIABLES = "\\$\\{\\{[\\w\\s]+}}";
  private static final Pattern HELM_DIRECTIVE_IN_COMMENT_OR_STRING = Pattern.compile("(" +
    String.join("|", DIRECTIVE_IN_COMMENT, DIRECTIVE_IN_SINGLE_QUOTE, DIRECTIVE_IN_DOUBLE_QUOTE, CODEFRESH_VARIABLES) + ")");

  private final HelmParser helmParser;
  private final KubernetesParserStatistics kubernetesParserStatistics;

  public KubernetesAnalyzer(String repositoryKey, TreeParser<? extends Tree> parser, List<TreeVisitor<InputFileContext>> visitors, DurationStatistics statistics,
    HelmParser helmParser, KubernetesParserStatistics kubernetesParserStatistics) {
    super(repositoryKey, parser, visitors, statistics);
    this.helmParser = helmParser;
    this.kubernetesParserStatistics = kubernetesParserStatistics;
  }

  @Override
  protected InputFileContext createInputFileContext(SensorContext sensorContext, InputFile inputFile) {
    return new HelmInputFileContext(sensorContext, inputFile);
  }

  @Override
  public Tree parse(String content, @Nullable InputFileContext inputFileContext) {
    try {
      if (!hasHelmContent(content)) {
        return kubernetesParserStatistics.recordPureKubernetesFile(() -> parser.parse(content, inputFileContext));
      } else {
        return kubernetesParserStatistics.recordHelmFile(() -> helmParser.parseHelmFile(content, (HelmInputFileContext) inputFileContext));
      }
    } catch (ParseException e) {
      throw e;
    } catch (RuntimeException e) {
      throw ParseException.toParseException("parse", inputFileContext, e);
    }
  }

  public static boolean hasHelmContent(String text) {
    String[] lines = splitLines(text);
    for (String line : lines) {
      if (hasHelmContentInLine(line)) {
        return true;
      }
    }
    return false;
  }

  public static boolean hasHelmContentInLine(String line) {
    return line.contains("{{") && !HELM_DIRECTIVE_IN_COMMENT_OR_STRING.matcher(line).find();
  }
}
