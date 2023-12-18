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
package org.sonar.iac.helm;

import com.google.protobuf.InvalidProtocolBufferException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.iac.helm.utils.ExecutableHelper;
import org.sonar.iac.helm.utils.OperatingSystemUtils;
import org.sonarsource.iac.helm.TemplateEvaluationResult;

public class HelmEvaluator {
  private static final Logger LOG = LoggerFactory.getLogger(HelmEvaluator.class);
  public static final String HELM_FOR_IAC_EXECUTABLE = "sonar-helm-for-iac";

  private final File workingDir;
  private final int processTimeoutSeconds;
  private final ExecutorService processMonitor = Executors.newSingleThreadExecutor();
  private ProcessBuilder pb;

  public HelmEvaluator(File workingDir, int processTimeoutMillis) {
    this.workingDir = workingDir;
    this.processTimeoutSeconds = processTimeoutMillis;
  }

  public void initialize() throws IOException {
    this.pb = prepareProcessBuilder();
  }

  public TemplateEvaluationResult evaluateTemplate(String path, String content, String valuesFileContent) throws IOException {
    LOG.debug("Executing: {}", pb.command());
    var process = startProcess(pb, path, content, valuesFileContent);
    // if the process hangs while we are interacting with it from the main thread, it will be destroyed from another thread
    processMonitor.submit(() -> destroyProcessAfterTimeout(process));

    byte[] rawEvaluationResult = ExecutableHelper.readProcessOutput(process);
    try {
      // Process has returned all the data; wait for it to finish. If it hangs, it will be destroyed from another thread.
      var hasExited = process.waitFor(processTimeoutSeconds, TimeUnit.MILLISECONDS);
      if (hasExited && process.exitValue() != 0) {
        throw new IllegalStateException(HELM_FOR_IAC_EXECUTABLE + " exited with non-zero exit code: " + process.exitValue());
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while waiting for " + HELM_FOR_IAC_EXECUTABLE + " to finish", e);
    }
    if (rawEvaluationResult == null || rawEvaluationResult.length == 0) {
      throw new IllegalStateException("Empty evaluation result (serialization failed?)");
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
    var suffix = OperatingSystemUtils.getCurrentPlatform();
    var executable = ExecutableHelper.extractFromClasspath(workingDir, HELM_FOR_IAC_EXECUTABLE + "-" + suffix);
    return new ProcessBuilder(executable);
  }

  Process startProcess(ProcessBuilder pb, String name, String content, String valuesFileContent) throws IOException {
    var process = pb.start();
    try (var os = process.getOutputStream()) {
      writeStringAsBytes(os, String.format("%s%n", name));
      writeStringAsBytes(os, String.format("%d%n", content.lines().count()));
      if (!content.endsWith("\n")) {
        content += "\n";
      }
      writeStringAsBytes(os, content);
      writeStringAsBytes(os, String.format("values.yaml%n"));
      writeStringAsBytes(os, String.format("%d%n", valuesFileContent.lines().count()));
      if (!valuesFileContent.endsWith("\n")) {
        valuesFileContent += "\n";
      }
      writeStringAsBytes(os, valuesFileContent);
      writeStringAsBytes(os, String.format("END%n"));
    }
    return process;
  }

  private static void writeStringAsBytes(OutputStream os, String content) throws IOException {
    os.write(content.getBytes(StandardCharsets.UTF_8));
  }

  private void destroyProcessAfterTimeout(Process process) {
    try {
      if (!process.waitFor(processTimeoutSeconds, TimeUnit.MILLISECONDS)) {
        LOG.debug(HELM_FOR_IAC_EXECUTABLE + " is taking longer than 5 seconds to finish");
        process.destroyForcibly();
        throw new IllegalStateException(HELM_FOR_IAC_EXECUTABLE + " took too long. External process killed forcibly");
      }
    } catch (InterruptedException e) {
      LOG.warn("Interrupted while waiting for process to finish", e);
      Thread.currentThread().interrupt();
    }
  }
}
