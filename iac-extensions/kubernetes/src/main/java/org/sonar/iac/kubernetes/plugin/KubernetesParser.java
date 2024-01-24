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
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.helm.utils.HelmFilesystemUtils;
import org.sonar.iac.kubernetes.visitors.LocationShifter;

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
  private static final Pattern CONTAINS_LINE_NUMBER_OR_RANGE = Pattern.compile("#(?<rangeStart>\\d++)(:(?<rangeEnd>\\d++))?( #\\d++:?\\d*+)*+$");
  private static final List<String> LINES_IGNORE_LINE_COUNTER = List.of("---", "...");

  private final HelmProcessor helmProcessor;
  private final LocationShifter locationShifter;

  public KubernetesParser(HelmProcessor helmProcessor, LocationShifter locationShifter) {
    this.helmProcessor = helmProcessor;
    this.locationShifter = locationShifter;
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
    var evaluatedAndCleanedSource = removeBlankLines(evaluatedSource, inputFileContext);
    if (evaluatedAndCleanedSource.isBlank()) {
      LOG.debug("Blank evaluated file, skipping processing of Helm file {}", inputFileContext.inputFile);
      return super.parse("{}", null, FileTree.Template.HELM);
    }
    return super.parse(evaluatedAndCleanedSource, inputFileContext, FileTree.Template.HELM);
  }

  private static String getFileRelativePath(InputFileContext inputFileContext) {
    var filePath = Path.of(inputFileContext.inputFile.uri());
    var chartRootDirectory = HelmFilesystemUtils.retrieveHelmProjectFolder(filePath, inputFileContext.sensorContext.fileSystem().baseDir());
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
   * Also lines like {@code --- #5} or {@code ... #5} are added without comment.
   * Such lines may be produced after evaluation of Helm template.
   * In some cases such lines may cause parsing issues in snakeyaml-engine.
   */
  private String removeBlankLines(String source, InputFileContext inputFileContext) {
    var sb = new StringBuilder();
    var matcher = LINE_PATTERN.matcher(source);

    var lastIndex = 0;
    var lineCounter = 0;
    while (matcher.find()) {
      var lineContent = matcher.group("lineContent");
      var lineAndComment = toLineAndComment(lineContent);
      if (!lineAndComment.contentWithoutComment.isBlank()) {
        if (LINES_IGNORE_LINE_COUNTER.contains(lineAndComment.contentWithoutComment)) {
          sb.append(lineAndComment.contentWithoutComment);
          lineAndComment.addToLocationShifter(locationShifter, inputFileContext, lineCounter);
        } else {
          sb.append(lineContent);
        }
        sb.append(matcher.group("newLine"));
        lastIndex = matcher.end();
      } else {
        lineAndComment.addToLocationShifter(locationShifter, inputFileContext, lineCounter);
      }
      lineCounter++;
    }
    lineCounter++;
    var lastLine = source.substring(lastIndex);
    var lineAndComment = toLineAndComment(lastLine);
    if (!lineAndComment.contentWithoutComment.isBlank()) {
      if (LINES_IGNORE_LINE_COUNTER.contains(lineAndComment.contentWithoutComment)) {
        sb.append(lineAndComment.contentWithoutComment);
        lineAndComment.addToLocationShifter(locationShifter, inputFileContext, lineCounter);
      } else {
        sb.append(lastLine);
      }
    }
    return sb.toString();
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

  private static LineAndComment toLineAndComment(String lineContent) {
    var commentMatcher = CONTAINS_LINE_NUMBER_OR_RANGE.matcher(lineContent);
    if (commentMatcher.find()) {
      var comment = commentMatcher.group();
      var endIndex = lineContent.indexOf(comment);
      var lineContentWithoutComment = lineContent.substring(0, Math.max(endIndex - 1, 0));
      var lineCommentRangeStart = Integer.parseInt(commentMatcher.group("rangeStart"));
      var lineCommentRangeEnd = Optional.ofNullable(commentMatcher.group("rangeEnd"))
        .map(Integer::parseInt)
        .orElse(lineCommentRangeStart);
      return new LineAndComment(lineContentWithoutComment, lineCommentRangeStart, lineCommentRangeEnd);
    }
    return new LineAndComment(lineContent);
  }

  private static class LineAndComment {
    private final String contentWithoutComment;
    private final Integer lineCommentRangeStart;
    private final Integer lineCommentRangeEnd;

    public LineAndComment(String contentWithoutComment) {
      this.contentWithoutComment = contentWithoutComment;
      lineCommentRangeStart = null;
      lineCommentRangeEnd = null;
    }

    public LineAndComment(String contentWithoutComment, Integer lineCommentRangeStart, Integer lineCommentRangeEnd) {
      this.contentWithoutComment = contentWithoutComment;
      this.lineCommentRangeStart = lineCommentRangeStart;
      this.lineCommentRangeEnd = lineCommentRangeEnd;
    }

    public void addToLocationShifter(LocationShifter locationShifter, InputFileContext inputFileContext, int lineCounter) {
      if (lineCommentRangeStart != null && lineCommentRangeEnd != null) {
        locationShifter.addShiftedLine(inputFileContext, lineCounter, lineCommentRangeStart, lineCommentRangeEnd);
      }
    }
  }
}
