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

  public static String extractFromClasspath(File workDir, String executable) throws IOException {
    byte[] executableData = getBytesFromResource(executable);
    var dest = new File(workDir, executable);
    if (!fileMatch(dest, executableData)) {
      Files.write(dest.toPath(), executableData);
      if (!System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")) {
        var perms = Files.getPosixFilePermissions(dest.toPath());
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        Files.setPosixFilePermissions(dest.toPath(), perms);
      }
    }
    return dest.getAbsolutePath();
  }

  @CheckForNull
  public static byte[] readProcessOutput(Process process) {
    try (var is = process.getInputStream(); var es = process.getErrorStream()) {
      var rawEvaluationResult = is.readAllBytes();
      new BufferedReader(new InputStreamReader(es, StandardCharsets.UTF_8)).lines()
        .forEach(line -> LOG.debug("[exec] {}", line));
      return rawEvaluationResult;
    } catch (IOException e) {
      LOG.debug("Error reading process output", e);
      return null;
    }
  }

  static boolean fileMatch(File dest, byte[] expectedContent) throws IOException {
    if (!dest.exists()) {
      return false;
    }
    byte[] actualContent = Files.readAllBytes(dest.toPath());
    return Arrays.equals(actualContent, expectedContent);
  }

  public static byte[] getBytesFromResource(String executable) throws IOException {
    var out = new ByteArrayOutputStream();
    try (InputStream in = HelmEvaluator.class.getClassLoader().getResourceAsStream(executable)) {
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
