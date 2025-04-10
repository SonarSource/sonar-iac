/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
import org.sonar.api.Startable;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.utils.TempFolder;
import org.sonar.iac.helm.protobuf.TemplateEvaluationResult;
import org.sonar.iac.helm.utils.ExecutableHelper;
import org.sonar.iac.helm.utils.OperatingSystemUtils;
import org.sonarsource.api.sonarlint.SonarLintSide;

@ScannerSide
@SonarLintSide(lifespan = SonarLintSide.INSTANCE)
public class HelmEvaluator implements Startable {
  private static final Logger LOG = LoggerFactory.getLogger(HelmEvaluator.class);
  public static final String HELM_FOR_IAC_EXECUTABLE = "sonar-helm-for-iac";
  private static final int PROCESS_TIMEOUT_SECONDS = 5;
  private static final int N_THREADS = 2;

  private final File workingDir;
  private ExecutorService processMonitor;
  private ProcessBuilder processBuilder;

  public HelmEvaluator(TempFolder tempFolder) {
    workingDir = tempFolder.newDir();
  }

  public void initialize() throws IOException {
    this.processBuilder = prepareProcessBuilder();
  }

  @Override
  public void start() {
    LOG.debug("Starting HelmEvaluator ExecutorService with {} threads", N_THREADS);
    this.processMonitor = Executors.newFixedThreadPool(N_THREADS);
  }

  @Override
  public void stop() {
    LOG.debug("Closing monitoring resources of Helm evaluator");
    this.processMonitor.shutdownNow();
  }

  public TemplateEvaluationResult evaluateTemplate(String path, String content, Map<String, String> templateDependencies) throws IOException {
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
    LOG.debug("Preparing Helm analysis for platform: {}", suffix);
    var executable = ExecutableHelper.extractFromClasspath(workingDir, HELM_FOR_IAC_EXECUTABLE + "-" + suffix);
    return new ProcessBuilder(executable);
  }

  Process startProcess() throws IOException {
    return this.processBuilder.start();
  }

  void writeTemplateAndDependencies(Process process, String name, String content, Map<String, String> templateDependencies) throws IOException {
    try (var out = process.getOutputStream()) {
      writeFileToProcess(out, name, content);
      out.write(intTo4Bytes(templateDependencies.size()));
      for (var filenameToFileContent : templateDependencies.entrySet()) {
        writeFileToProcess(out, filenameToFileContent.getKey(), filenameToFileContent.getValue());
      }
    }
  }

  private static void writeFileToProcess(OutputStream out, String fileName, String content) throws IOException {
    var fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
    var filenameLength = intTo4Bytes(fileNameBytes.length);
    out.write(filenameLength);
    out.write(fileNameBytes);
    var contentBytes = content.getBytes(StandardCharsets.UTF_8);
    out.write(intTo4Bytes(contentBytes.length));
    out.write(contentBytes);
  }

  static byte[] intTo4Bytes(int number) {
    var array = new byte[4];
    array[0] = (byte) (number >> 24);
    array[1] = (byte) (number >> 16);
    array[2] = (byte) (number >> 8);
    array[3] = (byte) number;
    return array;
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
