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
package org.sonar.iac.common.extension;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.testing.IacTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileIdentificationPredicateTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  private static final String IDENTIFIER = "myidentifier";

  @Test
  void emptyIdentifierAlwaysTrue() {
    FileIdentificationPredicate filePredicate = new FileIdentificationPredicate("");
    assertThat(filePredicate.apply(IacTestUtils.inputFile("small_file.txt", "text"))).isTrue();
    assertThat(filePredicate.apply(IacTestUtils.inputFile("big_file_identifier_in_buffer.txt", "text"))).isTrue();
    assertThat(filePredicate.apply(IacTestUtils.inputFile("big_file_identifier_after_buffer.txt", "text"))).isTrue();
  }

  @Test
  void shouldFindIdentifierInSmallFile() {
    FileIdentificationPredicate filePredicate = new FileIdentificationPredicate(IDENTIFIER);
    assertThat(filePredicate.apply(IacTestUtils.inputFile("small_file.txt", "text"))).isTrue();
  }

  @Test
  void shouldFindIdentifierInBigFileIdentifierInBuffer() {
    FileIdentificationPredicate filePredicate = new FileIdentificationPredicate(IDENTIFIER);
    assertThat(filePredicate.apply(IacTestUtils.inputFile("big_file_identifier_in_buffer.txt", "text"))).isTrue();
  }

  @Test
  void shouldNotFindIdentifierInBigFileIdentifierAfterBuffer() {
    FileIdentificationPredicate filePredicate = new FileIdentificationPredicate(IDENTIFIER);
    assertThat(filePredicate.apply(IacTestUtils.inputFile("big_file_identifier_after_buffer.txt", "text"))).isFalse();
  }

  @Test
  void shouldErrorMessageWhenFileNotFound() throws IOException, URISyntaxException {
    InputFile noFile = mock(InputFile.class);
    when(noFile.inputStream()).thenThrow(new IOException("File not found mock"));
    when(noFile.uri()).thenReturn(new URI("nofile.txt"));

    FileIdentificationPredicate filePredicate = new FileIdentificationPredicate(IDENTIFIER);
    assertThat(filePredicate.apply(noFile)).isFalse();
    assertThat(logTester.logs(Level.ERROR)).hasSize(2);
    assertThat(logTester.logs(Level.ERROR).get(0)).isEqualTo("Unable to read file: nofile.txt.");
    assertThat(logTester.logs(Level.ERROR).get(1)).startsWith("File not found mock");
    assertThat(logTester.logs(Level.DEBUG)).hasSize(1);
    assertThat(logTester.logs(Level.DEBUG).get(0)).startsWith("File without identifier '" + IDENTIFIER + "': nofile.txt");
  }

}
