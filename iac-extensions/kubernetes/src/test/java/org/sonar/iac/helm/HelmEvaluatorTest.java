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
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.io.FileUtils;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junitpioneer.jupiter.RetryingTest;
import org.slf4j.event.Level;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.helm.protobuf.TemplateEvaluationResult;
import org.sonar.iac.helm.utils.ExecutableHelper;
import org.sonar.scanner.plugin.api.impl.utils.DefaultTempFolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HelmEvaluatorTest {
  @TempDir
  static File tempDir;

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);
  private HelmEvaluator helmEvaluator;
  private final List<CompletableFuture<Process>> pendingProcessExits = new CopyOnWriteArrayList<>();

  @BeforeEach
  void setUp() throws IOException {
    this.helmEvaluator = new HelmEvaluator(new DefaultTempFolder(tempDir, false));
    this.helmEvaluator.start();
    this.helmEvaluator.initialize();
  }

  @AfterEach
  void release() {
    helmEvaluator.stop();
    // Completing the exit futures cancels the orTimeout() timers that destroyAfterTimeout() scheduled on them.
    // A pending timer would otherwise fire seconds later, while another test is running, and log its DEBUG line
    // against whichever LogTester is active by then.
    pendingProcessExits.forEach(exit -> exit.complete(null));
  }

  @Test
  void stopShouldAwaitProcessMonitorTermination() throws Exception {
    var blockedProcess = mockProcessBlockedOnItsPipes();
    var helmEvaluatorSpy = spyEvaluatorStarting(blockedProcess.process());
    var evaluation = startEvaluationOn(helmEvaluatorSpy);
    assertThat(blockedProcess.errorOutput().awaitReadingStarted(5, TimeUnit.SECONDS)).isTrue();
    assertThat(blockedProcess.errorOutput().blockedThreadName()).startsWith("helm-evaluator-");
    assertThat(blockedProcess.errorOutput().isFinished()).isFalse();

    helmEvaluatorSpy.stop();

    assertThat(blockedProcess.errorOutput().isFinished()).isTrue();
    evaluation.thread().join(5000);
  }

  @Test
  void stopShouldDestroyLiveProcessAndAbortInFlightEvaluation() throws Exception {
    var blockedProcess = mockProcessBlockedOnItsPipes();
    var helmEvaluatorSpy = spyEvaluatorStarting(blockedProcess.process());
    var evaluation = startEvaluationOn(helmEvaluatorSpy);
    assertThat(blockedProcess.output().awaitReadingStarted(5, TimeUnit.SECONDS)).isTrue();

    helmEvaluatorSpy.stop();

    verify(blockedProcess.process()).destroy();
    evaluation.thread().join(5000);
    assertThat(evaluation.thread().isAlive()).isFalse();
    assertThat(evaluation.thrown().get())
      .isInstanceOf(HelmEvaluationAbortedException.class)
      .hasMessage("sonar-helm-for-iac evaluation was aborted: the analyzer is shutting down");
  }

  @Test
  void evaluateTemplateShouldBeAbortedAfterStopWithoutSpawningAProcess() throws IOException {
    var helmEvaluatorSpy = spy(this.helmEvaluator);
    helmEvaluatorSpy.stop();

    var templateDependencies = Map.<String, String>of();
    assertThatThrownBy(() -> helmEvaluatorSpy.evaluateTemplate("/foo/bar/baz.yaml", "", templateDependencies))
      .isInstanceOf(HelmEvaluationAbortedException.class)
      .hasMessage("sonar-helm-for-iac evaluation was aborted: the analyzer is shutting down");
    verify(helmEvaluatorSpy, never()).startProcess();
  }

  @Test
  void evaluateTemplateShouldDestroyProcessWhenWritingTheTemplateFails() throws Exception {
    var process = mockRunningProcess();
    when(process.getErrorStream()).thenReturn(InputStream.nullInputStream());
    var helmEvaluatorSpy = spy(this.helmEvaluator);
    doReturn(process).when(helmEvaluatorSpy).startProcess();
    doThrow(new IOException("Broken pipe")).when(helmEvaluatorSpy).writeTemplateAndDependencies(any(), any(), any(), any());

    var templateDependencies = Map.<String, String>of();
    assertThatThrownBy(() -> helmEvaluatorSpy.evaluateTemplate("/foo/bar/baz.yaml", "", templateDependencies))
      .isInstanceOf(IOException.class);

    // the process never got its complete input, so it must not be left running until the timeout
    verify(process).destroy();
  }

  @Test
  void startShouldResetTheAbortedState() throws IOException {
    helmEvaluator.stop();
    helmEvaluator.start();

    try (var ignored = mockStatic(ExecutableHelper.class)) {
      when(ExecutableHelper.readProcessOutput(any())).thenReturn(new byte[0]);
      var helmEvaluatorSpy = spy(this.helmEvaluator);
      var process = mockRunningProcess();
      doReturn(process).when(helmEvaluatorSpy).startProcess();
      doNothing().when(helmEvaluatorSpy).writeTemplateAndDependencies(any(), any(), any(), any());

      var templateDependencies = Map.<String, String>of();
      assertThatThrownBy(() -> helmEvaluatorSpy.evaluateTemplate("/foo/bar/baz.yaml", "", templateDependencies))
        .isInstanceOf(IllegalStateException.class)
        .isNotInstanceOf(HelmEvaluationAbortedException.class)
        .hasMessage("Empty evaluation result returned from sonar-helm-for-iac");
    }
  }

  @Test
  void stopShouldDestroyProcessStillRunningAfterItsEvaluation() throws IOException {
    var process = mockRunningProcess();
    evaluateWithEmptyResult(process);

    helmEvaluator.stop();

    verify(process).destroy();
  }

  @Test
  void stopShouldNotDestroyProcessThatAlreadyExited() throws IOException {
    var process = mockRunningProcess();
    when(process.onExit()).thenReturn(CompletableFuture.completedFuture(process));
    evaluateWithEmptyResult(process);

    helmEvaluator.stop();

    verify(process, never()).destroy();
  }

  @Test
  void destroyAfterTimeoutShouldDestroyProcessStillRunningAfterTheTimeout() {
    var process = mockRunningProcess();

    HelmEvaluator.destroyAfterTimeout(process, 10, TimeUnit.MILLISECONDS);

    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> verify(process).destroy());
    assertThat(logTester.logs(Level.DEBUG)).contains("sonar-helm-for-iac is taking longer than 10 milliseconds to finish");
  }

  @Test
  void destroyAfterTimeoutShouldNotDestroyProcessThatExitedInTime() {
    var process = mockRunningProcess();
    when(process.onExit()).thenReturn(CompletableFuture.completedFuture(process));

    HelmEvaluator.destroyAfterTimeout(process, 10, TimeUnit.MILLISECONDS);

    verify(process, never()).destroy();
  }

  private void evaluateWithEmptyResult(Process process) throws IOException {
    try (var ignored = mockStatic(ExecutableHelper.class)) {
      when(ExecutableHelper.readProcessOutput(any())).thenReturn(new byte[0]);
      var helmEvaluatorSpy = spyEvaluatorStarting(process);

      var templateDependencies = Map.<String, String>of();
      assertThatThrownBy(() -> helmEvaluatorSpy.evaluateTemplate("/foo/bar/baz.yaml", "", templateDependencies))
        .hasMessage("Empty evaluation result returned from sonar-helm-for-iac");
    }
  }

  @AfterAll
  static void cleanup() throws IOException {
    // workaround for Windows due to https://github.com/junit-team/junit5/issues/2811
    FileUtils.deleteDirectory(tempDir);
  }

  @RetryingTest(maxAttempts = 3)
  void shouldThrowIfGoBinaryNotFoundChartYaml() {
    var templateDependencies = Map.<String, String>of();
    assertThatThrownBy(() -> helmEvaluator.evaluateTemplate("/foo/bar/baz.yaml", "", templateDependencies))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Evaluation error in Go library: source file Chart.yaml not found");

    var logs = logTester.logs(Level.DEBUG).stream().filter(log -> !log.startsWith("Preparing Helm analysis for platform")).toList();
    assertThat(logs)
      .contains("[sonar-helm-for-iac] Exception encountered, printing recorded logs");
  }

  @Test
  void shouldThrowIfGoBinaryReturnsNonZero() throws IOException {
    try (var ignored = mockStatic(ExecutableHelper.class)) {
      when(ExecutableHelper.readProcessOutput(any())).thenReturn(new byte[0]);
      var helmEvaluatorSpy = spy(this.helmEvaluator);
      var process = mockRunningProcess();
      when(process.isAlive()).thenReturn(false);
      when(process.exitValue()).thenReturn(1);
      doReturn(process).when(helmEvaluatorSpy).startProcess();
      doNothing().when(helmEvaluatorSpy).writeTemplateAndDependencies(any(), any(), any(), any());

      var templateDependencies = Map.<String, String>of();
      assertThatThrownBy(() -> helmEvaluatorSpy.evaluateTemplate("/foo/bar/baz.yaml", "", templateDependencies))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("sonar-helm-for-iac exited with non-zero exit code: 1, possible serialization failure");
    }
  }

  @Test
  void shouldThrowIfRawEvaluationResultIsEmpty() throws IOException {
    try (var ignored = mockStatic(ExecutableHelper.class)) {
      when(ExecutableHelper.readProcessOutput(any())).thenReturn(new byte[0]);
      var helmEvaluatorSpy = spy(this.helmEvaluator);
      var process = mockRunningProcess();
      when(process.isAlive()).thenReturn(false);
      when(process.exitValue()).thenReturn(0);
      doReturn(process).when(helmEvaluatorSpy).startProcess();
      doNothing().when(helmEvaluatorSpy).writeTemplateAndDependencies(any(), any(), any(), any());

      var templateDependencies = Map.of("values.yaml", "");
      assertThatThrownBy(() -> helmEvaluatorSpy.evaluateTemplate("/foo/bar/baz.yaml", "", templateDependencies))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Empty evaluation result returned from sonar-helm-for-iac");
    }
  }

  @RetryingTest(maxAttempts = 3)
  void shouldThrowIfGoReturnsError() {
    var templateDependencies = Map.of("values.yaml", "container:\n  port: 8080");
    assertThatThrownBy(() -> helmEvaluator.evaluateTemplate("/foo/bar/baz.yaml", "containerPort: {{ .Values.", templateDependencies))
      .isInstanceOf(IllegalStateException.class);
    assertThat(logTester.logs(Level.DEBUG))
      .contains("[sonar-helm-for-iac]   Reading 26 bytes of file /foo/bar/baz.yaml from stdin");
  }

  @Test
  void shouldThrowOnDeserializationError() throws IOException {
    try (var ignored = mockStatic(TemplateEvaluationResult.class); var ignored2 = mockStatic(ExecutableHelper.class)) {
      when(TemplateEvaluationResult.parseFrom(any(byte[].class))).thenThrow(new InvalidProtocolBufferException("Invalid input"));
      var helmEvaluatorSpy = spy(this.helmEvaluator);
      when(ExecutableHelper.readProcessOutput(any())).thenReturn(new byte[1]);
      var pb = mock(ProcessBuilder.class);
      when(pb.command()).thenReturn(Collections.emptyList());
      doReturn(pb).when(helmEvaluatorSpy).prepareProcessBuilder();
      doReturn(mockRunningProcess()).when(helmEvaluatorSpy).startProcess();
      doNothing().when(helmEvaluatorSpy).writeTemplateAndDependencies(any(), any(), any(), any());

      var templateDependencies = Map.<String, String>of();
      assertThatThrownBy(() -> helmEvaluatorSpy.evaluateTemplate("/foo/bar/baz.yaml", "", templateDependencies))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Deserialization error");
    }
  }

  @Test
  void shouldEvaluateTemplate() throws IOException {
    var templateDependencies = Map.of("values.yaml", "container:\n  port: 8080", "Chart.yaml", "name: foo");
    var evaluationResult = helmEvaluator.evaluateTemplate("templates/baz.yaml", "containerPort: {{ .Values.container.port }}", templateDependencies);

    assertThat(evaluationResult.getTemplate()).contains("containerPort: 8080");
    assertThat(logTester.logs()).noneMatch(log -> log.startsWith("[sonar-helm-for-iac]"));
  }

  @Test
  void shouldEvaluateInputsWithTrailingNewline() throws IOException {
    var evaluationResult = helmEvaluator.evaluateTemplate("templates/baz.yaml", "containerPort: {{ .Values.container.port }}\n   \n",
      Map.of("values.yaml", "container:\n  port: 8080\n\n", "Chart.yaml", "name: foo\n\n"));

    assertThat(evaluationResult.getTemplate()).contains("containerPort: 8080");
    assertThat(logTester.logs())
      .anyMatch(log -> log.startsWith("Preparing Helm analysis for platform:"))
      .noneMatch(log -> log.startsWith("[sonar-helm-for-iac]"));
  }

  @Test
  void shouldEvaluateTemplateWithoutValuesYaml() throws IOException {
    var templateDependencies = Map.of("Chart.yaml", "name: foo");
    var evaluationResult = helmEvaluator.evaluateTemplate("templates/baz.yaml", "value: {{ print \"true\" }}", templateDependencies);

    assertThat(evaluationResult.getTemplate()).contains("value: true");
    assertThat(logTester.logs()).noneMatch(log -> log.startsWith("[sonar-helm-for-iac]"));
  }

  @Test
  void shouldFailOnTemplateWithoutValuesYamlThatUsesValues() {
    var templateDependencies = Map.of("Chart.yaml", "name: foo");

    assertThatThrownBy(() -> helmEvaluator.evaluateTemplate("templates/baz.yaml", "value: {{ .Values.container.port }}", templateDependencies))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Evaluation error in Go library: template: foo/templates/baz.yaml:1:10: " +
        "executing \"foo/templates/baz.yaml\" at <.Values.container.port>: nil pointer evaluating interface {}.port");
  }

  private record BlockedProcess(Process process, BlockingInputStream output, BlockingInputStream errorOutput) {
  }

  private record Evaluation(Thread thread, AtomicReference<Throwable> thrown) {
  }

  /**
   * A mock process whose stdout and stderr both block their reader, and whose {@code destroy()} releases them -
   * the way destroying a real process closes its pipes. A Mockito static mock of {@link ExecutableHelper} cannot
   * be used instead: static mocks are thread-local, so they would not apply to the evaluation thread.
   */
  private BlockedProcess mockProcessBlockedOnItsPipes() {
    var output = new BlockingInputStream();
    var errorOutput = new BlockingInputStream();
    var process = mockRunningProcess();
    when(process.getInputStream()).thenReturn(output);
    when(process.getErrorStream()).thenReturn(errorOutput);
    doAnswer(invocation -> {
      output.unblock();
      errorOutput.unblock();
      return null;
    }).when(process).destroy();
    return new BlockedProcess(process, output, errorOutput);
  }

  /**
   * A mock process that never exits on its own. {@code onExit()} must be stubbed, as Mockito would otherwise return
   * {@code null} from it. The future is registered so that {@link #release()} can complete it: an evaluation arms a
   * {@code PROCESS_TIMEOUT_SECONDS} timer on it, which outlives the test unless the future completes.
   */
  private Process mockRunningProcess() {
    var process = mock(Process.class);
    var exit = new CompletableFuture<Process>();
    pendingProcessExits.add(exit);
    when(process.onExit()).thenReturn(exit);
    return process;
  }

  private HelmEvaluator spyEvaluatorStarting(Process process) throws IOException {
    var helmEvaluatorSpy = spy(this.helmEvaluator);
    doReturn(process).when(helmEvaluatorSpy).startProcess();
    doNothing().when(helmEvaluatorSpy).writeTemplateAndDependencies(any(), any(), any(), any());
    return helmEvaluatorSpy;
  }

  private static Evaluation startEvaluationOn(HelmEvaluator evaluator) {
    var thrown = new AtomicReference<Throwable>();
    var thread = new Thread(() -> {
      try {
        evaluator.evaluateTemplate("/foo/bar/baz.yaml", "", Map.of());
      } catch (Exception e) {
        thrown.set(e);
      }
    }, "test-helm-evaluation");
    thread.setDaemon(true);
    thread.start();
    return new Evaluation(thread, thrown);
  }

  /**
   * Blocks in {@code read()} until {@link #unblock()} is called and then reports EOF, the way a process' pipe
   * behaves when the process is destroyed while a reader is blocked on it. The wait deliberately ignores
   * interruption, because plain blocking I/O on a process pipe cannot be aborted by {@code Thread.interrupt()}.
   */
  private static final class BlockingInputStream extends InputStream {
    private final CountDownLatch readingStarted = new CountDownLatch(1);
    private final CountDownLatch released = new CountDownLatch(1);
    private final CountDownLatch closed = new CountDownLatch(1);
    private final AtomicReference<String> blockedThreadName = new AtomicReference<>();

    @Override
    public int read() {
      blockedThreadName.compareAndSet(null, Thread.currentThread().getName());
      readingStarted.countDown();
      var wasInterrupted = false;
      var isReleased = false;
      while (!isReleased) {
        try {
          released.await();
          isReleased = true;
        } catch (InterruptedException e) {
          wasInterrupted = true;
        }
      }
      if (wasInterrupted) {
        Thread.currentThread().interrupt();
      }
      return -1;
    }

    @Override
    public void close() {
      closed.countDown();
    }

    boolean awaitReadingStarted(long timeout, TimeUnit unit) throws InterruptedException {
      return readingStarted.await(timeout, unit);
    }

    @Nullable
    String blockedThreadName() {
      return blockedThreadName.get();
    }

    /**
     * @return whether the reader left this stream, which the try-with-resources in {@link ExecutableHelper}
     *   only does once its task is done
     */
    boolean isFinished() {
      return closed.getCount() == 0;
    }

    void unblock() {
      released.countDown();
    }
  }

  @ParameterizedTest
  @CsvSource({
    "0,00 00 00 00",
    "1,00 00 00 01",
    "10,00 00 00 0A",
    "255,00 00 00 FF",
    "256,00 00 01 00",
    "21812,00 00 55 34",
    "65535,00 00 FF FF",
    "65536,00 01 00 00",
    Integer.MAX_VALUE + ",7F FF FF FF"})
  void shouldConvertIntToBytes(int number, String expected) {
    var bytes = HelmEvaluator.intTo4Bytes(number);
    var asText = "%02X %02X %02X %02X".formatted(bytes[0], bytes[1], bytes[2], bytes[3]);
    assertThat(asText).isEqualTo(expected);
  }
}
