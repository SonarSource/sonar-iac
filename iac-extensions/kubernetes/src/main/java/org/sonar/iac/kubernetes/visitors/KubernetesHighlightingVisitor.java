/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
import java.util.Set;
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
import org.sonar.iac.kubernetes.plugin.KubernetesParser;

import static org.sonar.api.batch.sensor.highlighting.TypeOfText.COMMENT;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD_LIGHT;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.STRING;

/*
  * This visitor will highlight kubernetes and helm files based on regex matching.
  * Currently, it's not being used in the Sensor as well, but will be in one of the next commits.
  *
  * Known limitations:
  *  - Inside single quoted strings, the only escape character is the single quote itself, which is not supported yet
  *  - I'm not sure about quoted scalar flows, they need to be tested
  *  - quoteless values don't allow the '#' character
  *
  * Test cases to add:
  *   - multiline values after different keys or just scalar values
  *   - multiline values with comments
  *   - empty key / values
 */
public class KubernetesHighlightingVisitor extends SyntaxHighlightingVisitor {

  private static final Logger LOG = LoggerFactory.getLogger(KubernetesHighlightingVisitor.class);
  private static final Pattern LINE_SEPARATOR = Pattern.compile("\\r\\n|[\\n\\r\\u2028\\u2029]");

  private static final String DOUBLE_QUOTED_VALUE_GROUP_NAME = "doubleQuotedValue";
  private static final String SINGLE_QUOTED_VALUE_GROUP_NAME = "singleQuotedValue";
  private static final String QUOTELESS_VALUE_GROUP_NAME = "quotelessValue";

  private static final String DOUBLE_QUOTED_KEY = quoted("doubleQuotedKey", "\"");
  private static final String DOUBLE_QUOTED_VALUE = quoted(DOUBLE_QUOTED_VALUE_GROUP_NAME, "\"");
  private static final String SINGLE_QUOTED_KEY = quoted("singleQuotedKey", "'");
  private static final String SINGLE_QUOTED_VALUE = quoted(SINGLE_QUOTED_VALUE_GROUP_NAME, "'");
  private static final String QUOTELESS_KEY = quoteless("quotelessKey");
  private static final String QUOTELESS_VALUE = quoteless(QUOTELESS_VALUE_GROUP_NAME);
  private static final String MULTI_LINE_OPERATORS = "(?<multilineOperator>[|>])";
  private static final String KEY = DOUBLE_QUOTED_KEY + "|" + SINGLE_QUOTED_KEY + "|" + QUOTELESS_KEY;
  private static final String VALUE = DOUBLE_QUOTED_VALUE + "|" + SINGLE_QUOTED_VALUE + "|" + QUOTELESS_VALUE;

  // still needs to be tested
  private static final Pattern YAML_DIRECTIVES = Pattern.compile("\\s*(?:---|%).*");

  private static final String COMMENT_S = "(?:\\h(?<comment>#.*))?";
  private static final String TAG = "(?<tag>!\\H+\\h?)?";
  private static final String COMBINED = "\\h*-?\\h*(?:" + KEY + "):(?:\\h+" + TAG + "(?:" +
    MULTI_LINE_OPERATORS + "|" + VALUE + ")?)?\\h*" + COMMENT_S;

  private static final Pattern COMBINED_PATTERN = Pattern.compile(COMBINED);

  private static final String SCALAR_VALUE = "\\h*-?\\h*" + TAG + "(?:" + MULTI_LINE_OPERATORS + "|" + VALUE + ")?\\h*" + COMMENT_S;
  private static final Pattern SCALAR_VALUE_PATTERN = Pattern.compile(SCALAR_VALUE);

  private static final Set<String> GROUPNAMES_IN_SCALAR_VALUES = Set.of("tag",
    "multilineOperator",
    DOUBLE_QUOTED_VALUE_GROUP_NAME,
    SINGLE_QUOTED_VALUE_GROUP_NAME,
    QUOTELESS_VALUE_GROUP_NAME,
    "comment");

  private static final Map<String, TypeOfText> GROUPNAME_TO_TYPE = Map.of(
    "doubleQuotedKey", KEYWORD,
    "singleQuotedKey", KEYWORD,
    "quotelessKey", KEYWORD,
    "multilineOperator", KEYWORD_LIGHT,
    "tag", KEYWORD_LIGHT,
    DOUBLE_QUOTED_VALUE_GROUP_NAME, STRING,
    SINGLE_QUOTED_VALUE_GROUP_NAME, STRING,
    QUOTELESS_VALUE_GROUP_NAME, STRING,
    "comment", COMMENT);

  private static String quoted(String groupName, String quotes) {
    return "(?<" + groupName + ">" + quotes + "[^" + quotes + "]*" + quotes + ")";
  }

  // Current limitation: We don't allow '#' in quoteless variable names
  private static String quoteless(String groupName) {
    return "(?<" + groupName + ">[^'\"%#]*)";
  }

  @Override
  protected void languageSpecificHighlighting() {
    register(Tree.class, (ctx, tree) -> highlightContent(ctx));
  }

  private void highlightContent(InputFileContext context) {
    try {
      String[] lines = LINE_SEPARATOR.split(context.inputFile.contents());
      for (var i = 0; i < lines.length; i++) {
        processLine(lines[i], i + 1);
      }
    } catch (IOException e) {
      LOG.error("Unable to read file: {}.", context.inputFile.uri(), e);
    }
  }

  void processLine(String line, int lineNumber) {
    if (KubernetesParser.hasHelmContentInLine(line) || YAML_DIRECTIVES.matcher(line).matches()) {
      // don't highlight anything here yet
      return;
    }

    var combinedMatcher = COMBINED_PATTERN.matcher(line);
    if (combinedMatcher.matches()) {
      for (Map.Entry<String, TypeOfText> groupNameToType : GROUPNAME_TO_TYPE.entrySet()) {
        highlightMatch(combinedMatcher, groupNameToType.getKey(), lineNumber, groupNameToType.getValue());
      }

    } else {
      var scalarMatcher = SCALAR_VALUE_PATTERN.matcher(line);
      if (scalarMatcher.matches()) {
        for (String groupName : GROUPNAMES_IN_SCALAR_VALUES) {
          highlightMatch(scalarMatcher, groupName, lineNumber, GROUPNAME_TO_TYPE.get(groupName));
        }
      }
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
}
