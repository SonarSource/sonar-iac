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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import javax.annotation.Nullable;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.ExtensionPoint;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.helm.HelmEvaluator;
import org.sonar.iac.helm.utils.HelmFilesystemUtils;
import org.sonar.iac.helm.utils.OperatingSystemUtils;
import org.sonarsource.api.sonarlint.SonarLintSide;

import static org.sonar.iac.helm.LineNumberCommentInserter.addLineComments;
import static org.sonar.iac.helm.utils.HelmFilesystemUtils.additionalFilesOfHelmProjectDirectory;
import static org.sonar.iac.helm.utils.HelmFilesystemUtils.normalizeToRuntimePathSeparator;

@ScannerSide
@SonarLintSide
@ExtensionPoint
public class HelmProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(HelmProcessor.class);
  private HelmEvaluator helmEvaluator;

  public HelmProcessor(HelmEvaluator helmEvaluator) {
    this.helmEvaluator = helmEvaluator;
  }

  public static boolean isHelmEvaluatorExecutableAvailable() {
    return OperatingSystemUtils.getCurrentPlatformIfSupported().isPresent();
  }

  public void initialize() {
    try {
      helmEvaluator.initialize();
    } catch (IOException e) {
      LOG.debug("Failed to initialize Helm evaluator, analysis of Helm files will be disabled", e);
      this.helmEvaluator = null;
    }
  }

  public boolean isHelmEvaluatorInitialized() {
    return helmEvaluator != null;
  }

  String processHelmTemplate(String path, String source, InputFileContext inputFileContext) {
    if (!isHelmEvaluatorInitialized()) {
      throw new IllegalStateException("Attempt to process Helm template with uninitialized Helm evaluator");
    }
    if (source.isBlank()) {
      LOG.debug("The file {} is blank, skipping evaluation", inputFileContext.inputFile);
      return source;
    }

    var sourceWithComments = addLineComments(source);
    Map<String, InputFile> additionalFiles = additionalFilesOfHelmProjectDirectory(inputFileContext);
    var fileContents = validateAndReadFiles(inputFileContext.inputFile, additionalFiles);
    return evaluateHelmTemplate(path, inputFileContext.inputFile, sourceWithComments, fileContents);
  }

  private static Map<String, String> validateAndReadFiles(InputFile inputFile, Map<String, InputFile> files) {
    // Currently we are only looking for the default location of the values file
    if (!files.containsKey("values.yaml") && !files.containsKey("values.yml")) {
      throw parseExceptionFor(inputFile, "Failed to find values file", null);
    }

    Map<String, String> fileContents = new HashMap<>(files.size());

    for (Map.Entry<String, InputFile> filenameToInputFile : files.entrySet()) {
      if (filenameToInputFile.getKey().endsWith("tgz") || filenameToInputFile.getKey().endsWith("gz")) {
        fileContents.putAll(readCompressedFile(filenameToInputFile.getKey(), filenameToInputFile.getValue()));
      } else {
        var fileContent = readTextFile(inputFile, filenameToInputFile);
        fileContents.put(filenameToInputFile.getKey(), fileContent);
      }
    }
    return fileContents;
  }

  // exposed for tests
  static Map<String, String> readCompressedFile(String name, InputFile inputFile) {
    LOG.debug("Read dependency chart {}", name);
    var pathPrefix = pathPrefix(name);
    Map<String, String> result = new HashMap<>();
    try (var source = inputFile.inputStream();
      var gzip = new GZIPInputStream(source);
      var tar = new TarArchiveInputStream(gzip)) {
      TarArchiveEntry entry;
      while ((entry = tar.getNextEntry()) != null) {
        var normalizedName = normalizeToRuntimePathSeparator(entry.getName());
        if (entry.isFile() && HelmFilesystemUtils.includeFile(normalizedName)) {
          var bytes = tar.readAllBytes();
          var s = new String(bytes, StandardCharsets.UTF_8);
          result.put(pathPrefix + File.separator + normalizedName, s);
        }
      }
    } catch (IOException e) {
      throw parseExceptionFor(inputFile, "Failed to read compressed file", e.getMessage());
    }
    return result;
  }

  private static String pathPrefix(String name) {
    var index = name.lastIndexOf("/");
    if (index == -1) {
      return name;
    }
    return name.substring(0, index);
  }

  private static String readTextFile(InputFile inputFile, Map.Entry<String, InputFile> filenameToInputFile) {
    var additionalInputFile = filenameToInputFile.getValue();
    String fileContent;
    try {
      fileContent = additionalInputFile.contents();
    } catch (IOException e) {
      throw parseExceptionFor(inputFile, "Failed to read file at " + additionalInputFile, e.getMessage());
    }
    return fileContent;
  }

  private String evaluateHelmTemplate(String path, InputFile inputFile, String content, Map<String, String> templateDependencies) {
    try {
      var evaluationResult = helmEvaluator.evaluateTemplate(path, content, templateDependencies);
      return evaluationResult.getTemplate();
    } catch (IllegalStateException | IOException e) {
      throw parseExceptionFor(inputFile, "Template evaluation failed", e.getMessage());
    }
  }

  private static ParseException parseExceptionFor(InputFile inputFile, String cause, @Nullable String details) {
    return new ParseException("Failed to evaluate Helm file " + inputFile + ": " + cause, null, details);
  }
}
