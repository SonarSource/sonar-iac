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
package org.sonar.iac.helm.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.Locale;
import javax.annotation.CheckForNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.iac.helm.HelmEvaluator;

public final class ExecutableHelper {
  private static final int BUFFER_SIZE = 8192;
  private static final Logger LOG = LoggerFactory.getLogger(ExecutableHelper.class);

  private ExecutableHelper() {
  }

  /**
   * It extracts the binary executable from JAR file to {@code workDir}.
   *
   * @param workDir Working directory where executable needs to be extracted
   * @param executable The executable filename what needs to be extracted
   * @return The executable absolute path
   * @throws IOException If any IO Error
   */
  public static String extractFromClasspath(File workDir, String executable) throws IOException {
    byte[] executableData = getBytesFromResource(executable);
    var destination = new File(workDir, executable);
    if (!fileContentMatches(destination, executableData)) {
      Files.write(destination.toPath(), executableData);
      if (!System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")) {
        // The Zip compression doesn't preserve the executable flag on Windows, so it needs to be set after extraction
        var permissions = Files.getPosixFilePermissions(destination.toPath());
        permissions.add(PosixFilePermission.OWNER_EXECUTE);
        Files.setPosixFilePermissions(destination.toPath(), permissions);
      }
    }
    return destination.getAbsolutePath();
  }

  @CheckForNull
  public static byte[] readProcessOutput(Process process) {
    try (var output = process.getInputStream(); var errorOutput = process.getErrorStream()) {
      var rawEvaluationResult = output.readAllBytes();
      new BufferedReader(new InputStreamReader(errorOutput, StandardCharsets.UTF_8)).lines()
        .forEach(line -> LOG.debug("[{}] {}", HelmEvaluator.HELM_FOR_IAC_EXECUTABLE, line));
      return rawEvaluationResult;
    } catch (IOException e) {
      LOG.debug("Error reading process output", e);
      return null;
    }
  }

  /**
   * It can be true if the executable is not cleaned up properly from another scan
   */
  static boolean fileContentMatches(File destination, byte[] expectedContent) throws IOException {
    if (!destination.exists()) {
      return false;
    }
    byte[] actualContent = Files.readAllBytes(destination.toPath());
    return Arrays.equals(actualContent, expectedContent);
  }

  static byte[] getBytesFromResource(String executable) throws IOException {
    var out = new ByteArrayOutputStream();
    // For an unknown reason, accessing resource via `Thread.currentThread().getContextClassLoader()` does not find it
    try (InputStream in = ExecutableHelper.class.getClassLoader().getResourceAsStream(executable)) {
      if (in == null) {
        throw new IllegalStateException(executable + " binary not found on class path");
      }
      copy(in, out);
    }
    return out.toByteArray();
  }

  private static void copy(InputStream in, OutputStream out) throws IOException {
    var buffer = new byte[BUFFER_SIZE];
    int read;
    while ((read = in.read(buffer)) >= 0) {
      out.write(buffer, 0, read);
    }
  }
}
