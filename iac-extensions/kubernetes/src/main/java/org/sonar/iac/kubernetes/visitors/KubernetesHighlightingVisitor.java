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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextPointer;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.SyntaxHighlightingVisitor;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.kubernetes.plugin.KubernetesParser;

import static org.sonar.api.batch.sensor.highlighting.TypeOfText.COMMENT;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD_LIGHT;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.STRING;
import static org.sonar.iac.common.yaml.YamlFileUtils.splitLines;

/*
  * This visitor will highlight kubernetes and helm files based on regex matching.
  *
  * Known limitations: SONARIAC-1180: Extend KubernetesHighlightingVisitor
 */
public class KubernetesHighlightingVisitor extends SyntaxHighlightingVisitor {

  private static final Logger LOG = LoggerFactory.getLogger(KubernetesHighlightingVisitor.class);

  @Override
  protected void languageSpecificHighlighting() {
    register(FileTree.class, (ctx, tree) -> highlightContext(ctx));
  }

  private void highlightContext(InputFileContext context) {
    try {
      highlightContent(context.inputFile.contents());
    } catch (IOException e) {
      LOG.error("Unable to read file: {}.", context.inputFile.uri(), e);
    }
  }

  void highlightContent(String content) {
    String[] lines = splitLines(content);
    for (var i = 0; i < lines.length; i++) {
      highlightLine(lines[i], i + 1);
    }
  }

  void highlightLine(String line, int lineNumber) {
    if (KubernetesParser.hasHelmContentInLine(line)) {
      // don't highlight anything here yet
      return;
    }

    var structureMatcher = YamlRegexPattern.STRUCTURAL_LINE_PATTERN.matcher(line);
    if (structureMatcher.matches()) {
      highlightGroupsInMatcher(structureMatcher, YamlRegexPattern.STRUCTURAL_LINE_GROUPNAMES, lineNumber);
      return;
    }

    var combinedMatcher = YamlRegexPattern.KEY_VALUE_LINE_PATTERN.matcher(line);
    if (combinedMatcher.matches()) {
      highlightGroupsInMatcher(combinedMatcher, YamlRegexPattern.KEY_VALUE_LINE_GROUPNAMES, lineNumber);
      return;
    }

    var scalarMatcher = YamlRegexPattern.VALUE_LINE_PATTERN.matcher(line);
    if (scalarMatcher.matches()) {
      highlightGroupsInMatcher(scalarMatcher, YamlRegexPattern.VALUE_LINE_GROUPNAMES, lineNumber);
    }
  }

  public void highlightGroupsInMatcher(Matcher matcher, Map<String, TypeOfText> groupNamesToType, int lineNumber) {
    for (Map.Entry<String, TypeOfText> groupNameToType : groupNamesToType.entrySet()) {
      highlightMatch(matcher, groupNameToType.getKey(), lineNumber, groupNameToType.getValue());
    }
  }

  public void highlightMatch(Matcher matcher, String groupName, int lineNumber, TypeOfText typeOfText) {
    String capturedGroup = matcher.group(groupName);
    if (capturedGroup == null || capturedGroup.isEmpty()) {
      return;
    }

    var startPointer = new TextPointer(lineNumber, matcher.start(groupName));
    var endPointer = new TextPointer(lineNumber, matcher.end(groupName));
    var textRangeToHighlight = new TextRange(startPointer, endPointer);

    highlight(textRangeToHighlight, typeOfText);
  }

  @Override
  public void highlightComments(Tree tree) {
    // Do nothing as we highlight comments with the 'highlightLine' method
    // This method is registered by the inherited abstract constructor
  }

  private static class YamlRegexPattern {
    private static final String DOUBLE_QUOTED_VALUE_GROUP_NAME = "doubleQuotedValue";
    private static final String SINGLE_QUOTED_VALUE_GROUP_NAME = "singleQuotedValue";
    private static final String QUOTELESS_VALUE_GROUP_NAME = "quotelessValue";
    private static final String COMMENT_GROUP_NAME = "comment";
    private static final String DOUBLE_QUOTED_KEY = doubleQuoted("doubleQuotedKey");
    private static final String DOUBLE_QUOTED_VALUE = doubleQuoted(DOUBLE_QUOTED_VALUE_GROUP_NAME);
    private static final String SINGLE_QUOTED_KEY = singleQuoted("singleQuotedKey");
    private static final String SINGLE_QUOTED_VALUE = singleQuoted(SINGLE_QUOTED_VALUE_GROUP_NAME);
    private static final String QUOTELESS_KEY = quoteless("quotelessKey");
    private static final String QUOTELESS_VALUE = quoteless(QUOTELESS_VALUE_GROUP_NAME);
    private static final String MULTI_LINE_OPERATORS = "(?<multilineOperator>[|>])";
    private static final String KEY = DOUBLE_QUOTED_KEY + "|" + SINGLE_QUOTED_KEY + "|" + QUOTELESS_KEY;
    private static final String VALUE = DOUBLE_QUOTED_VALUE + "|" + SINGLE_QUOTED_VALUE + "|" + QUOTELESS_VALUE;
    private static final String COMMENT_S = "(?<comment>(?:(?<=\\h)|(?<![^.]))#.*+)?+";
    private static final String OPTIONAL_TAG = "(?<tag>!\\H++\\h?+)?+";
    private static final String DIRECTIVES = "(?<directive>%(?:TAG|YAML))\\h*+(?<handle>[!\\d][^#\\h]*+(?:\\h*+[^#\\h]++)?+)";
    private static final String STRUCTURAL_ELEMENTS = "(?<structure>\\.{3}|-{3})";
    private static final String KEY_VALUE_LINE = "\\h*+-?+\\h*+(?:" + KEY + "):(?:\\h++" + OPTIONAL_TAG + "(?:" +
      MULTI_LINE_OPERATORS + "|" + VALUE + ")?+)?+\\h*+" + COMMENT_S;
    private static final String VALUE_LINE = "\\h*+-?+\\h*+" + OPTIONAL_TAG + "(?:" + MULTI_LINE_OPERATORS + "|" + VALUE + ")?+\\h*+" + COMMENT_S;
    private static final Pattern STRUCTURAL_LINE_PATTERN = Pattern.compile("\\h*+(?:" + DIRECTIVES + "|" + STRUCTURAL_ELEMENTS + ")\\h*+" + COMMENT_S);
    private static final Pattern KEY_VALUE_LINE_PATTERN = Pattern.compile(KEY_VALUE_LINE);
    private static final Pattern VALUE_LINE_PATTERN = Pattern.compile(VALUE_LINE);

    private static final Map<String, TypeOfText> STRUCTURAL_LINE_GROUPNAMES = Map.of(
      "directive", KEYWORD_LIGHT,
      "structure", KEYWORD_LIGHT,
      "handle", STRING,
      COMMENT_GROUP_NAME, COMMENT);

    private static final Map<String, TypeOfText> VALUE_LINE_GROUPNAMES = Map.of(
      "multilineOperator", KEYWORD_LIGHT,
      DOUBLE_QUOTED_VALUE_GROUP_NAME, STRING,
      SINGLE_QUOTED_VALUE_GROUP_NAME, STRING,
      QUOTELESS_VALUE_GROUP_NAME, STRING,
      "tag", KEYWORD_LIGHT,
      COMMENT_GROUP_NAME, COMMENT);

    private static final Map<String, TypeOfText> KEY_VALUE_LINE_GROUPNAMES = Map.of(
      "doubleQuotedKey", KEYWORD,
      "singleQuotedKey", KEYWORD,
      "quotelessKey", KEYWORD,
      "multilineOperator", KEYWORD_LIGHT,
      "tag", KEYWORD_LIGHT,
      DOUBLE_QUOTED_VALUE_GROUP_NAME, STRING,
      SINGLE_QUOTED_VALUE_GROUP_NAME, STRING,
      QUOTELESS_VALUE_GROUP_NAME, STRING,
      COMMENT_GROUP_NAME, COMMENT);

    private static String doubleQuoted(String groupName) {
      return "(?<" + groupName + ">\"(?:\\\\.|[^\"])*+\")";
    }

    private static String singleQuoted(String groupName) {
      return "(?<" + groupName + ">'(?:''|[^'])*+')";
    }

    private static String quoteless(String groupName) {
      return "(?<" + groupName + ">(?:[^'\"%#:\\h](?:[^%:\\h]|\\h(?!#)|:(?=\\H))*+)?+)";
    }
  }
}
