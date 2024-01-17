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

import java.io.File;
import java.nio.file.Path;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.helm.utils.HelmFilesystemUtils;

import static org.sonar.iac.common.yaml.YamlFileUtils.splitLines;

public class KubernetesParser extends YamlParser {

  private static final Logger LOG = LoggerFactory.getLogger(KubernetesParser.class);

  private static final String DIRECTIVE_IN_COMMENT = "#.*\\{\\{";
  private static final String DIRECTIVE_IN_SINGLE_QUOTE = "'[^']*\\{\\{[^']*'";
  private static final String DIRECTIVE_IN_DOUBLE_QUOTE = "\"[^\"]*\\{\\{[^\"]*\"";
  private static final String CODEFRESH_VARIABLES = "\\$\\{\\{[\\w\\s]+}}";
  private static final Pattern HELM_DIRECTIVE_IN_COMMENT_OR_STRING = Pattern.compile("(" +
    String.join("|", DIRECTIVE_IN_COMMENT, DIRECTIVE_IN_SINGLE_QUOTE, DIRECTIVE_IN_DOUBLE_QUOTE, CODEFRESH_VARIABLES) + ")");

  private static final String NEW_LINE = "\\n\\r\\u2028\\u2029";
  private static final Pattern LINE_PATTERN = Pattern.compile("(?<lineContent>[^" + NEW_LINE + "]*+)(?<newLine>\\r\\n|[" + NEW_LINE + "])");
  private static final Pattern CONTAINS_LINE_NUMBER = Pattern.compile("(?<comment>#\\d+( #\\d+)*+)$");

  private final HelmProcessor helmProcessor;

  public KubernetesParser(HelmProcessor helmProcessor) {
    this.helmProcessor = helmProcessor;
  }

  @Override
  public FileTree parse(String source, @Nullable InputFileContext inputFileContext) {
    if (!hasHelmContent(source)) {
      return super.parse(source, inputFileContext);
    } else {
      return parseHelmFile(source, inputFileContext);
    }
  }

  private FileTree parseHelmFile(String source, @Nullable InputFileContext inputFileContext) {
    if (inputFileContext == null) {
      LOG.debug("No InputFileContext provided, skipping processing of Helm file");
      return super.parse("{}", null, FileTree.Template.HELM);
    }

    LOG.debug("Helm content detected in file '{}'", inputFileContext.inputFile);
    if (!helmProcessor.isHelmEvaluatorInitialized()) {
      LOG.debug("Helm evaluator is not initialized, skipping processing of Helm file {}", inputFileContext.inputFile);
      return super.parse("{}", null, FileTree.Template.HELM);
    }

    return evaluateAndParseHelmFile(source, inputFileContext);
  }

  private FileTree evaluateAndParseHelmFile(String source, InputFileContext inputFileContext) {
    var fileRelativePath = getFileRelativePath(inputFileContext);
    var evaluatedSource = helmProcessor.processHelmTemplate(fileRelativePath, source, inputFileContext);
    var evaluatedAndCleanedSource = removeBlankLines(evaluatedSource);
    if (evaluatedAndCleanedSource.isBlank()) {
      LOG.debug("Blank evaluated file, skipping processing of Helm file {}", inputFileContext.inputFile);
      return super.parse("{}", null, FileTree.Template.HELM);
    }
    return super.parse(evaluatedAndCleanedSource, inputFileContext, FileTree.Template.HELM);
  }

  private static String getFileRelativePath(InputFileContext inputFileContext) {
    var filePath = Path.of(inputFileContext.inputFile.uri());
    var chartRootDirectory = HelmFilesystemUtils.retrieveHelmProjectFolder(filePath);
    String fileRelativePath;
    if (chartRootDirectory == null) {
      fileRelativePath = inputFileContext.inputFile.filename();
    } else {
      fileRelativePath = chartRootDirectory.relativize(filePath).normalize().toString();
      // transform windows to unix path
      fileRelativePath = fileRelativePath.replace(File.separatorChar, '/');
    }
    return fileRelativePath;
  }

  /**
   * This method remove blank lines that contains only trailing line comment number.
   * Such lines may be produced after evaluation of Helm template.
   * In some cases such lines may cause parsing issues in snakeyaml-engine.
   */
  private static String removeBlankLines(String source) {
    var sb = new StringBuilder();
    var matcher = LINE_PATTERN.matcher(source);

    var lastIndex = 0;
    while (matcher.find()) {
      var lineContent = matcher.group("lineContent");
      if (isLineNotBlank(lineContent)) {
        sb.append(lineContent);
        sb.append(matcher.group("newLine"));
        lastIndex = matcher.end();
      }
    }
    var lastLine = source.substring(lastIndex);
    if (isLineNotBlank(lastLine)) {
      sb.append(lastLine);
    }
    return sb.toString();
  }

  private static boolean isLineNotBlank(String lineContent) {
    var commentMatcher = CONTAINS_LINE_NUMBER.matcher(lineContent);
    if (commentMatcher.find()) {
      var comment = commentMatcher.group("comment");
      var endIndex = lineContent.indexOf(comment);
      var contentWithoutComment = lineContent.substring(0, endIndex);
      return !contentWithoutComment.isBlank();
    }
    return !lineContent.isBlank();
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
