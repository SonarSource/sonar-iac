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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.event.Level;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExecutableHelperTest {
  @TempDir
  static File tempDir;

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  @AfterAll
  static void cleanup() throws IOException {
    // workaround for Windows due to https://github.com/junit-team/junit5/issues/2811
    FileUtils.deleteDirectory(tempDir);
  }

  @Test
  void shouldThrowIfResourceNotFound() {
    assertThatThrownBy(() -> ExecutableHelper.getBytesFromResource("non-existing.file"))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("non-existing.file binary not found on class path");
  }

  @Test
  void shouldNotUnpackIfAlreadyExists() throws IOException {
    var executable = "sonar-helm-for-iac-" + OperatingSystemUtils.getCurrentPlatform();
    var dest = new File(tempDir, executable);
    dest.delete();
    assertThat(dest).doesNotExist();

    var binary = ExecutableHelper.extractFromClasspath(tempDir, executable);
    assertThat(binary).isEqualTo(dest.getAbsolutePath());
    assertThat(dest).exists();
    var lastModified = dest.lastModified();

    var binary2 = ExecutableHelper.extractFromClasspath(tempDir, executable);
    assertThat(binary2).isEqualTo(dest.getAbsolutePath());
    assertThat(dest).exists();
    assertThat(dest.lastModified()).isEqualTo(lastModified);
  }

  @Test
  void shouldReturnNullOnIoErrorProcessOutput() throws IOException {
    var process = mock(Process.class);
    var is = mock(InputStream.class);
    when(process.getInputStream()).thenReturn(is);
    when(is.readAllBytes()).thenThrow(new IOException("Mocked IOException"));

    var bytes = ExecutableHelper.readProcessOutput(process);

    Assertions.assertThat(bytes).isNull();
  }

  @Test
  void shouldReturnNullOnIoErrorForProcessErrorOutput() throws IOException {
    var process = mock(Process.class);
    var is = mock(InputStream.class);
    when(process.getErrorStream()).thenReturn(is);

    ExecutableHelper.readProcessErrorOutput(process);

    assertThat(logTester.logs()).contains("Error reading process error output for sonar-helm-for-iac");
  }
}
