/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;
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
  private static final int POOL_SHUTDOWN_TIMEOUT_SECONDS = 5;
  private static final int N_THREADS = 2;
  private static final ThreadFactory THREAD_FACTORY = Thread.ofPlatform()
    .name("helm-evaluator-", 1)
    // Platform threads inherit daemon status and priority from the thread that creates them, and pool workers are
    // created lazily by whichever thread calls submit(), so both are set explicitly
    .daemon(false)
    .priority(Thread.NORM_PRIORITY)
    .factory();

  private final File workingDir;
  private final Set<Process> liveProcesses = ConcurrentHashMap.newKeySet();
  // Bound by the component lifecycle: the thread pool in start(), the process builder in initialize()
  @Nullable
  private ExecutorService processMonitor;
  @Nullable
  private ProcessBuilder processBuilder;
  private volatile boolean stopped;

  public HelmEvaluator(TempFolder tempFolder) {
    workingDir = tempFolder.newDir();
  }

  public void initialize() throws IOException {
    this.processBuilder = prepareProcessBuilder();
  }

  @Override
  public void start() {
    stopped = false;
    liveProcesses.clear();
    this.processMonitor = Executors.newFixedThreadPool(N_THREADS, THREAD_FACTORY);
  }

  @Override
  public void stop() {
    stopped = true;
    var monitor = this.processMonitor;
    if (monitor != null) {
      monitor.shutdownNow();
      liveProcesses.forEach(Process::destroy);
      awaitProcessMonitorTermination(monitor);
    }
  }

  private static void awaitProcessMonitorTermination(ExecutorService monitor) {
    try {
      if (!monitor.awaitTermination(POOL_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
        LOG.warn("HelmEvaluator process monitor did not terminate within {}s", POOL_SHUTDOWN_TIMEOUT_SECONDS);
      }
    } catch (InterruptedException e) {
      LOG.warn("Interrupted while waiting for the HelmEvaluator process monitor to terminate", e);
      Thread.currentThread().interrupt();
    }
  }

  public TemplateEvaluationResult evaluateTemplate(String path, String content, Map<String, String> templateDependencies) throws IOException {
    var monitor = Objects.requireNonNull(processMonitor, "HelmEvaluator must be started before evaluating a template");
    if (stopped) {
      // Without this guard, startProcess() would spawn a child that nothing destroys and the submit() calls below
      // would fail with a RejectedExecutionException, which none of the callers of this method handle
      throw abortedEvaluation();
    }
    var process = startProcess();
    // Tracked until the process exits rather than until this method returns, so that stop() can still destroy it
    liveProcesses.add(process);
    process.onExit().thenRun(() -> liveProcesses.remove(process));
    destroyAfterTimeout(process, PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    var isInputWritten = false;
    try {
      if (stopped) {
        throw abortedEvaluation();
      }
      monitor.submit(() -> ExecutableHelper.readProcessErrorOutput(process));
      writeTemplateAndDependencies(process, path, content, templateDependencies);
      isInputWritten = true;

      byte[] rawEvaluationResult = ExecutableHelper.readProcessOutput(process);
      if (stopped) {
        throw abortedEvaluation();
      }
      if (rawEvaluationResult.length == 0) {
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
        if (stopped) {
          throw abortedEvaluation();
        }
        throw new IllegalStateException("Deserialization error", e);
      }
    } finally {
      if (!isInputWritten) {
        // Without its complete input the process can never produce a result, so do not leave it running until the timeout
        process.destroy();
      }
    }
  }

  private static HelmEvaluationAbortedException abortedEvaluation() {
    return new HelmEvaluationAbortedException(HELM_FOR_IAC_EXECUTABLE + " evaluation was aborted: the analyzer is shutting down");
  }

  ProcessBuilder prepareProcessBuilder() throws IOException {
    var suffix = OperatingSystemUtils.getCurrentPlatformIfSupported()
      .orElseThrow(() -> new IllegalStateException("HelmEvaluator is being initialized on an unsupported platform"));
    LOG.debug("Preparing Helm analysis for platform: {}", suffix);
    var executable = ExecutableHelper.extractFromClasspath(workingDir, HELM_FOR_IAC_EXECUTABLE + "-" + suffix);
    return new ProcessBuilder(executable);
  }

  Process startProcess() throws IOException {
    return Objects.requireNonNull(this.processBuilder, "HelmEvaluator must be initialized before evaluating a template").start();
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

  /**
   * Destroying the process closes its pipes, which is the only way to unblock a thread reading them. The JDK already
   * waits for every child process on its own reaper thread, so the timeout is scheduled on its exit future instead of
   * dedicating a pool thread to {@link Process#waitFor}.
   */
  static void destroyAfterTimeout(Process process, long timeout, TimeUnit unit) {
    process.onExit()
      .orTimeout(timeout, unit)
      .exceptionally(timeoutException -> {
        LOG.debug("{} is taking longer than {} {} to finish", HELM_FOR_IAC_EXECUTABLE, timeout, unit.name().toLowerCase(Locale.ROOT));
        process.destroy();
        return process;
      });
  }
}
