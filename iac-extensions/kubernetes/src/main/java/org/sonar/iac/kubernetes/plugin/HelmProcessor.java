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

import java.io.IOException;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.ExtensionPoint;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.helm.HelmEvaluator;
import org.sonar.iac.helm.jna.Loader;
import org.sonar.iac.helm.jna.library.IacHelmLibrary;
import org.sonarsource.api.sonarlint.SonarLintSide;

import static org.sonar.iac.helm.utils.HelmFilesystemUtils.findValuesFile;

@ScannerSide
@SonarLintSide
@ExtensionPoint
public class HelmProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(HelmProcessor.class);
  @Nullable
  private HelmEvaluator helmEvaluator;
  protected Loader loader = new Loader();

  public HelmProcessor() {
  }

  // for testing
  HelmProcessor(HelmEvaluator helmEvaluator) {
    this.helmEvaluator = helmEvaluator;
  }

  public void initialize() {
    HelmEvaluator newHelmEvaluator;
    try {
      IacHelmLibrary library = loader.load("/sonar-helm-for-iac", IacHelmLibrary.class);
      newHelmEvaluator = new HelmEvaluator(library);
    } catch (RuntimeException e) {
      LOG.debug("Native library not loaded, Helm integration will be disabled", e);
      newHelmEvaluator = null;
    }
    this.helmEvaluator = newHelmEvaluator;
  }

  @Nullable
  String processHelmTemplate(String filename, String source, InputFileContext inputFileContext) {
    // TODO: better support of Helm project structure
    var valuesFile = findValuesFile(inputFileContext);
    if (valuesFile != null) {
      try {
        return evaluateHelmTemplate(filename, source, valuesFile.contents());
      } catch (IOException e) {
        LOG.debug("Failed to read values file at {}, skipping processing of Helm file '{}'", valuesFile.uri(), filename, e);
      }
    } else {
      LOG.debug("Failed to find values file, skipping processing of Helm file '{}'", filename);
    }

    return null;
  }

  @Nullable
  private String evaluateHelmTemplate(String path, String content, String valuesFileContent) {
    if (helmEvaluator == null || valuesFileContent.isBlank()) {
      LOG.debug("Template cannot be evaluated, skipping processing of Helm file '{}'", path);
      return "{}";
    }
    try {
      var evaluationResult = helmEvaluator.evaluateTemplate(path, content, valuesFileContent);
      return evaluationResult.getTemplate();
    } catch (IllegalStateException e) {
      LOG.debug("Template evaluation failed, skipping processing of Helm file '{}'. Reason: ", path, e);
      return null;
    }
  }
}
