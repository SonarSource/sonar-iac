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

import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.tree.FileTree;

public class KubernetesParser extends YamlParser {

  private static final Logger LOG = LoggerFactory.getLogger(KubernetesParser.class);

  private static final Pattern LINE_TERMINATOR = Pattern.compile("[\\n\\r\\u2028\\u2029]");
  private static final String DIRECTIVE_IN_COMMENT = "#.*\\{\\{";
  private static final String DIRECTIVE_IN_SINGLE_QUOTE = "'[^']*\\{\\{[^']*'";
  private static final String DIRECTIVE_IN_DOUBLE_QUOTE = "\"[^\"]*\\{\\{[^\"]*\"";
  private static final String CODEFRESH_VARIABLES = "\\$\\{\\{[\\w\\s]+}}";
  private static final Pattern HELM_DIRECTIVE_IN_COMMENT_OR_STRING = Pattern.compile("(" +
    String.join("|", DIRECTIVE_IN_COMMENT, DIRECTIVE_IN_SINGLE_QUOTE, DIRECTIVE_IN_DOUBLE_QUOTE, CODEFRESH_VARIABLES) + ")");

  private final HelmProcessor helmProcessor;

  public KubernetesParser(HelmProcessor helmProcessor) {
    this.helmProcessor = helmProcessor;
  }

  @Override
  public FileTree parse(String source, @Nullable InputFileContext inputFileContext) {
    if (hasHelmContent(source)) {
      if (inputFileContext != null) {
        var filename = inputFileContext.inputFile.filename();
        LOG.debug("Helm content detected in file '{}'", inputFileContext.inputFile);
        var evaluatedSource = helmProcessor.processHelmTemplate(filename, source, inputFileContext);
        if (evaluatedSource != null) {
          return super.parse(evaluatedSource, inputFileContext, FileTree.Template.HELM);
        }
      } else {
        LOG.debug("No InputFileContext provided, skipping processing of Helm file");
      }

      return super.parse("{}", inputFileContext, FileTree.Template.HELM);
    }
    return super.parse(source, inputFileContext);
  }

  public static boolean hasHelmContent(String text) {
    String[] lines = LINE_TERMINATOR.split(text);
    for (String line : lines) {
      if (line.contains("{{") && !HELM_DIRECTIVE_IN_COMMENT_OR_STRING.matcher(line).find()) {
        return true;
      }
    }
    return false;
  }
}
