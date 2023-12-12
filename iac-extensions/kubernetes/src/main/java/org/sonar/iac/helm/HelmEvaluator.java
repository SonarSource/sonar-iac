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
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.iac.helm.utils.ExecutableHelper;
import org.sonar.iac.helm.utils.NativeUtils;
import org.sonarsource.iac.helm.TemplateEvaluationResult;

public class HelmEvaluator {
  private static final Logger LOG = LoggerFactory.getLogger(HelmEvaluator.class);

  private final File workingDir;
  private final NativeUtils nativeUtils = new NativeUtils();

  public HelmEvaluator(File workingDir) {
    this.workingDir = workingDir;
  }

  public TemplateEvaluationResult evaluateTemplate(String path, String content, String valuesFileContent) throws IOException {
    var pb = prepareProcessBuilder(path, content.lines().count());

    LOG.debug("Executing: {}", pb.command());
    var process = startProcess(pb, content, valuesFileContent);

    byte[] rawEvaluationResult = ExecutableHelper.readProcessOutput(process);
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

  ProcessBuilder prepareProcessBuilder(String path, long nl) throws IOException {
    var suffix = nativeUtils.getSuffixForCurrentPlatform();
    var executable = ExecutableHelper.extractFromClasspath(workingDir, "sonar-helm-for-iac-" + suffix);
    return new ProcessBuilder(
      executable,
      "--path=" + path,
      "--nl=" + nl);
  }

  Process startProcess(ProcessBuilder pb, String content, String valuesFileContent) throws IOException {
    var process = pb.start();
    try (var os = process.getOutputStream()) {
      os.write(content.getBytes(StandardCharsets.UTF_8));
      // In case content doesn't have a trailing newline, add it. If there are empty lines, Go will ignore them anyway.
      os.write("\n".getBytes(StandardCharsets.UTF_8));
      os.write(valuesFileContent.getBytes(StandardCharsets.UTF_8));
    }
    return process;
  }
}
