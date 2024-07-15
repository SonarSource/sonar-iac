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
package org.sonar.iac.kubernetes.checks;

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

  private static final String MESSAGE_OPEN_BRACKETS = "Add a whitespace after {{ in the template directive.";
  private static final String MESSAGE_CLOSE_BRACKETS = "Add a whitespace before }} in the template directive.";
  private static final String GROUP_NAME = "brackets";
  // The [^\r\n\t\f\v -] in regex is like \S (non-space character) and not dash
  private static final String EXCLUDING_CHARS = "\\r\\n\\t\\f\\v -";
  public static final String EXCLUDING_COMMENT_START = "(?!/\\*)";
  private static final Pattern OPEN_BRACKETS = Pattern.compile("(?<" + GROUP_NAME + ">\\{\\{)" + EXCLUDING_COMMENT_START + "[^" + EXCLUDING_CHARS + "]");
  private static final Pattern CLOSE_BRACKETS = Pattern.compile("(?<!\\*/|[" + EXCLUDING_CHARS + "])(?<" + GROUP_NAME + ">\\}\\})");

  @Override
  public void initialize(InitContext init) {
    init.register(FileTree.class, (ctx, tree) -> visit((KubernetesCheckContext) ctx));
  }

  private static void visit(KubernetesCheckContext kubernetesContext) {
    var content = readContentWithComments(kubernetesContext);
    if (content != null) {
      verifyContent(kubernetesContext, OPEN_BRACKETS, content, MESSAGE_OPEN_BRACKETS);
      verifyContent(kubernetesContext, CLOSE_BRACKETS, content, MESSAGE_CLOSE_BRACKETS);
    }
  }

  private static String readContentWithComments(KubernetesCheckContext ctx) {
    if (ctx.inputFileContext() instanceof HelmInputFileContext helmContext) {
      return helmContext.getSourceWithComments();
    }
    return null;
  }

  private static void verifyContent(KubernetesCheckContext context, Pattern pattern, String content, String message) {
    var matcher = pattern.matcher(content);
    while (matcher.find()) {
      var location = new LocationImpl(matcher.start(GROUP_NAME), matcher.end(GROUP_NAME) - matcher.start(GROUP_NAME));
      var textRange = location.toTextRange(content);
      context.reportIssueNoLineShift(textRange, message);
    }
  }
}
