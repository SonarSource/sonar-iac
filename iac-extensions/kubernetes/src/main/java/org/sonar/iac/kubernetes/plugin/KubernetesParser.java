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

import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.tree.FileTree;

public class KubernetesParser extends YamlParser {

  private static final Logger LOG = LoggerFactory.getLogger(KubernetesParser.class);

  @Override
  public FileTree parse(String source, @Nullable InputFileContext inputFileContext) {
    if (inputFileContext instanceof KubernetesInputFileContext && ((KubernetesInputFileContext) inputFileContext).hasHelmContent()) {
      // TODO process the helm content of this kubernete file
      var filename = inputFileContext.inputFile.filename();
      LOG.debug("Helm content detected in File '{}'", filename);
      LOG.debug("The content will not be processed.");
      return super.parse("{}", inputFileContext);
    }
    return super.parse(source, inputFileContext);
  }
}
