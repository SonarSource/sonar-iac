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
package org.sonar.iac.helm;

import com.google.protobuf.InvalidProtocolBufferException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.utils.TempFolder;
import org.sonar.iac.helm.utils.ExecutableHelper;
import org.sonar.iac.helm.utils.OperatingSystemUtils;
import org.sonarsource.api.sonarlint.SonarLintSide;

@ScannerSide
@SonarLintSide(lifespan = SonarLintSide.INSTANCE)
public class HelmEvaluator {
  private static final Logger LOG = LoggerFactory.getLogger(HelmEvaluator.class);
  public static final String HELM_FOR_IAC_EXECUTABLE = "sonar-helm-for-iac";
  private static final int PROCESS_TIMEOUT_SECONDS = 5;

  private final File workingDir;
  private final ExecutorService processMonitor = Executors.newFixedThreadPool(2);
  private ProcessBuilder processBuilder;

  public HelmEvaluator(TempFolder tempFolder) {
    workingDir = tempFolder.newDir();
  }

  public void initialize() throws IOException {
    this.processBuilder = prepareProcessBuilder();
  }

  public TemplateEvaluationResult evaluateTemplate(String path, String content, Map<String, String> templateDependencies) throws IOException {
    LOG.debug("Executing: {}", processBuilder.command());
    var process = startProcess();
    processMonitor.submit(() -> ExecutableHelper.readProcessErrorOutput(process));
    writeTemplateAndDependencies(process, path, content, templateDependencies);
    processMonitor.submit(() -> monitorProcess(process));

    byte[] rawEvaluationResult = ExecutableHelper.readProcessOutput(process);
    if (rawEvaluationResult == null || rawEvaluationResult.length == 0) {
      if (!process.isAlive() && process.exitValue() != 0) {
        throw new IllegalStateException(HELM_FOR_IAC_EXECUTABLE + " exited with non-zero exit code: " + process.exitValue() + ", possible serialization failure");
      }
      throw new IllegalStateException("Empty evaluation result returned from " + HELM_FOR_IAC_EXECUTABLE);
    }

    try {
      var evaluationResult = TemplateEvaluationResult.parseFrom(rawEvaluationResult);
      if (!evaluationResult.getError().isEmpty()) {
        throw new IllegalStateException("Evaluation error in Go library: " + evaluationResult.getError());
      }
      return evaluationResult;
    } catch (InvalidProtocolBufferException e) {
      throw new IllegalStateException("Deserialization error", e);
    }
  }

  ProcessBuilder prepareProcessBuilder() throws IOException {
    var suffix = OperatingSystemUtils.getCurrentPlatformIfSupported()
      .orElseThrow(() -> new IllegalStateException("HelmEvaluator is being initialized on an unsupported platform"));
    var executable = ExecutableHelper.extractFromClasspath(workingDir, HELM_FOR_IAC_EXECUTABLE + "-" + suffix);
    return new ProcessBuilder(executable);
  }

  Process startProcess() throws IOException {
    return this.processBuilder.start();
  }

  void writeTemplateAndDependencies(Process process, String name, String content, Map<String, String> templateDependencies) throws IOException {
    try (var out = process.getOutputStream()) {
      writeFileToProcess(out, name, content);
      for (var filenameToFileContent : templateDependencies.entrySet()) {
        writeFileToProcess(out, filenameToFileContent.getKey(), filenameToFileContent.getValue());
      }
      writeStringAsBytes(out, String.format("END%n"));
    }
  }

  private static void writeFileToProcess(OutputStream out, String fileName, String content) throws IOException {
    writeStringAsBytes(out, String.format("%s%n", fileName));
    writeStringAsBytes(out, String.format("%d%n", content.lines().count()));
    if (!content.endsWith("\n")) {
      content += "\n";
    }
    writeStringAsBytes(out, content);
  }

  private static void writeStringAsBytes(OutputStream out, String content) throws IOException {
    out.write(content.getBytes(StandardCharsets.UTF_8));
  }

  private static void monitorProcess(Process process) {
    try {
      if (!process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
        LOG.debug(HELM_FOR_IAC_EXECUTABLE + " is taking longer than 5 seconds to finish");
        process.destroy();
      }
    } catch (InterruptedException e) {
      LOG.warn("Interrupted while waiting for process to finish", e);
      Thread.currentThread().interrupt();
    }
  }
}
