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
package org.sonar.iac.kubernetes.checks;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.helm.tree.impl.LocationImpl;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;
import org.sonar.iac.kubernetes.visitors.KubernetesCheckContext;

@Rule(key = "S6893")
public class WhitespaceBracesCheck implements IacCheck {

  private static final String MESSAGE_OPEN_BRACKETS = "Add a whitespace after \"{{\" in the template directive.";
  private static final String MESSAGE_CLOSE_BRACKETS = "Add a whitespace before \"}}\" in the template directive.";
  // The [^\r\n\t\f\v -] in regex is like \S (non-space character) and not dash
  private static final Predicate<String> VALID_SEPARATOR = Pattern.compile("[\\r\\n\\t\\f\\v -]").asMatchPredicate();
  private static final String OPEN_BRACKETS_GROUP_NAME = "openBrackets";
  private static final String HELM_CONTENT_GROUP_NAME = "helmContent";
  private static final String CLOSE_BRACKETS_GROUP_NAME = "closeBrackets";
  // Regex strategy: match brackets by couples and check if there is a whitespace after open brackets and/or before close brackets
  private static final String OPEN_BRACKETS = "(?<" + OPEN_BRACKETS_GROUP_NAME + ">\\{\\{)";
  private static final String DONT_MATCH_CLOSING_BRACKETS = "(?!}})";
  private static final String GO_STRING_DOUBLE_QUOTES = "\"(?:\\\\.|[^\"])*+\"";
  private static final String GO_STRING_BACK_QUOTES = "`[^`]*+`";
  private static final String GO_CHAR_SINGLE_QUOTE = "'.'";
  private static final String GO_COMMENT = "/\\*(?:(?!\\*/).)*+\\*/";
  private static final String HELM_EXPRESSION = "(?:" + DONT_MATCH_CLOSING_BRACKETS + "(?:" + GO_STRING_DOUBLE_QUOTES + "|" + GO_STRING_BACK_QUOTES
    + "|" + GO_CHAR_SINGLE_QUOTE + "|" + GO_COMMENT + "|.))*+";
  private static final String HELM_CONTENT = "(?<" + HELM_CONTENT_GROUP_NAME + ">" + HELM_EXPRESSION + ")";
  private static final String CLOSE_BRACKETS = "(?<" + CLOSE_BRACKETS_GROUP_NAME + ">}})";
  private static final Pattern HELM_INSTRUCTION_PATTERN = Pattern.compile(OPEN_BRACKETS + HELM_CONTENT + CLOSE_BRACKETS);

  @Override
  public void initialize(InitContext init) {
    init.register(FileTree.class, (ctx, tree) -> visit((KubernetesCheckContext) ctx));
  }

  private static void visit(KubernetesCheckContext kubernetesContext) {
    var content = readContentWithComments(kubernetesContext);
    if (content != null) {
      var matcher = HELM_INSTRUCTION_PATTERN.matcher(content);
      while (matcher.find()) {
        String helmContent = matcher.group(HELM_CONTENT_GROUP_NAME);
        if (!isHelmComment(helmContent)) {
          checkBrackets(kubernetesContext, content, matcher, OPEN_BRACKETS_GROUP_NAME, helmContent.substring(0, 1), MESSAGE_OPEN_BRACKETS);
          checkBrackets(kubernetesContext, content, matcher, CLOSE_BRACKETS_GROUP_NAME, helmContent.substring(helmContent.length() - 1), MESSAGE_CLOSE_BRACKETS);
        }
      }
    }
  }

  private static void checkBrackets(KubernetesCheckContext kubernetesContext, String content, Matcher matcher, String bracketGroupName, String charToTest, String message) {
    if (!VALID_SEPARATOR.test(charToTest)) {
      var locationOpen = new LocationImpl(matcher.start(bracketGroupName), matcher.end(bracketGroupName) - matcher.start(bracketGroupName));
      var textRangeOpen = locationOpen.toTextRange(content);
      kubernetesContext.reportIssueNoLineShift(textRangeOpen, message);
    }
  }

  private static boolean isHelmComment(String helmContent) {
    return helmContent.startsWith("/*");
  }

  private static String readContentWithComments(KubernetesCheckContext ctx) {
    if (ctx.inputFileContext() instanceof HelmInputFileContext helmContext) {
      return helmContext.getSourceWithComments();
    }
    return null;
  }
}
