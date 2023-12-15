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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExecutableHelperTest {
  @TempDir
  File tempDir;

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
  void shouldReturnNullOnIoError() throws IOException {
    var process = mock(Process.class);
    var is = mock(InputStream.class);
    when(process.getInputStream()).thenReturn(is);
    when(is.readAllBytes()).thenThrow(new IOException("Mocked IOException"));

    var bytes = ExecutableHelper.readProcessOutput(process);

    Assertions.assertThat(bytes).isNull();
  }
}
