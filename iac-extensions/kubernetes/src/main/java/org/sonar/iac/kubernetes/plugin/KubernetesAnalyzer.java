/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.kubernetes.plugin;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.analyzer.CrossFileAnalyzer;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;

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
  @Nullable
  private final SonarLintFileListener sonarLintFileListener;

  public KubernetesAnalyzer(String repositoryKey,
    TreeParser<? extends Tree> parser,
    List<TreeVisitor<InputFileContext>> visitors,
    DurationStatistics statistics,
    HelmParser helmParser,
    KubernetesParserStatistics kubernetesParserStatistics,
    TreeVisitor<InputFileContext> checksVisitor,
    @Nullable SonarLintFileListener sonarLintFileListener) {
    super(repositoryKey, parser, visitors, checksVisitor, statistics);
    this.helmParser = helmParser;
    this.kubernetesParserStatistics = kubernetesParserStatistics;
    this.sonarLintFileListener = sonarLintFileListener;
  }

  @Override
  protected InputFileContext createInputFileContext(SensorContext sensorContext, InputFile inputFile) {
    if (isHelmFile(inputFile)) {
      return new HelmInputFileContext(sensorContext, inputFile, sonarLintFileListener);
    }
    return new InputFileContext(sensorContext, inputFile);
  }

  @Override
  public Tree parse(String content, @Nullable InputFileContext inputFileContext) {
    try {
      if (inputFileContext instanceof HelmInputFileContext helmInputFileContext) {
        return kubernetesParserStatistics.recordFile(() -> helmParser.parseHelmFile(content, helmInputFileContext), helmInputFileContext);
      } else {
        return kubernetesParserStatistics.recordFile(() -> parser.parse(content, inputFileContext), inputFileContext);
      }
    } catch (ParseException e) {
      throw e;
    } catch (RuntimeException e) {
      throw ParseException.toParseException("parse", inputFileContext, e);
    }
  }

  protected static boolean isHelmFile(InputFile inputFile) {
    try {
      return hasHelmContent(inputFile.contents());
    } catch (IOException | RuntimeException e) {
      // The purpose of this method is not to throw a parse exception but only to check if the file is a helm file
      // The parse exception would be thrown at a later stage
      return false;
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
