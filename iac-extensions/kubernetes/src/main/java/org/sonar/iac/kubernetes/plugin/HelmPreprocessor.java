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
package org.sonar.iac.kubernetes.plugin;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.iac.kubernetes.jna.Loader;
import org.sonar.iac.kubernetes.jna.library.IacHelmLibrary;
import org.sonarsource.iac.helm.TemplateEvaluationResult;

import javax.annotation.Nullable;
import java.util.regex.Pattern;

public final class HelmPreprocessor {

  private static final Logger LOG = LoggerFactory.getLogger(HelmPreprocessor.class);
  private static final String NEW_LINE = "\\n\\r\\u2028\\u2029";
  private static final Pattern LINE_PATTERN = Pattern.compile("(?<lineContent>[^" + NEW_LINE + "]*+)(?<newLine>\\r\\n|[" + NEW_LINE + "])");
  @Nullable
  private final IacHelmLibrary iacHelmLibrary;

  public HelmPreprocessor() {
    IacHelmLibrary library;
    try {
      library = Loader.load("/sonar-helm-for-iac", IacHelmLibrary.class);
    } catch (RuntimeException e) {
      LOG.debug("Native library not loaded, Helm integration will be disabled", e);
      library = null;
    }
    this.iacHelmLibrary = library;
  }

  public String evaluateTemplate(String path, String content, String valuesFileContent) {
    if (iacHelmLibrary == null || valuesFileContent.isBlank()) {
      LOG.debug("Template cannot be evaluated, skipping processing of Helm file {}", path);
      return "{}";
    }
    var rawEvaluationResult = iacHelmLibrary.evaluateTemplate(path, content, valuesFileContent).getByteArray();
    TemplateEvaluationResult evaluationResult = null;
    var errorMessage = "";
    try {
      evaluationResult = TemplateEvaluationResult.parseFrom(rawEvaluationResult);
      if (!evaluationResult.getError().isEmpty()) {
        errorMessage = "[go] " + evaluationResult.getError();
      }
    } catch (InvalidProtocolBufferException e) {
      errorMessage = e.getMessage();
    }
    if (rawEvaluationResult.length == 0 || !errorMessage.isEmpty() || evaluationResult == null) {
      LOG.debug("Template evaluation failed, skipping processing of Helm file {}", path);
      if (!errorMessage.isEmpty()) {
        LOG.debug("Reason: {}", errorMessage);
      }
      return "{}";
    }
    return evaluationResult.getTemplate();
  }

  /**
   * For a given file content, it will process it line by line, and add a comment with a line number at the end of each of them. ('#5' for line five)
   * This is mean for Helm processing, to be able to track back the original source from the generated content.
   */
  public static String addLineComments(String content) {
    var sb = new StringBuilder();
    var lineCounter = 0;
    var matcher = LINE_PATTERN.matcher(content);

    while (matcher.find()) {
      lineCounter++;
      String lineContent = matcher.group("lineContent");
      String comment = commentLineNumber(lineCounter);
      String newLine = matcher.group("newLine");
      matcher.appendReplacement(sb, lineContent + comment + newLine);
    }
    matcher.appendTail(sb);
    sb.append(commentLineNumber(lineCounter + 1));

    return sb.toString();
  }

  private static String commentLineNumber(int number) {
    return " #" + number;
  }
}
