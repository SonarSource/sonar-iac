/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.extension;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.testing.IacTestUtils.inputFile;

class FileIdentificationPredicateTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  private static final String IDENTIFIER = "myidentifier";

  static Stream<List<String>> shouldReturnTrueForEmptyIdentifier() {
    return Stream.of(
      List.of(),
      List.of(""),
      null);
  }

  @ParameterizedTest
  @MethodSource
  void shouldReturnTrueForEmptyIdentifier(List<String> identifiers) {
    var filePredicate = new FileIdentificationPredicate(identifiers, true);
    assertThat(filePredicate.apply(inputFile("small_file.txt", "text"))).isTrue();
    assertThat(filePredicate.apply(inputFile("big_file_identifier_in_buffer.txt", "text"))).isTrue();
    assertThat(filePredicate.apply(inputFile("big_file_identifier_after_buffer.txt", "text"))).isTrue();
  }

  @Test
  void shouldFindIdentifierInSmallFile() {
    var filePredicate = new FileIdentificationPredicate(IDENTIFIER, true);
    assertThat(filePredicate.apply(inputFile("small_file.txt", "text"))).isTrue();
  }

  @Test
  void shouldFindIdentifierInBigFileIdentifierInBuffer() {
    var filePredicate = new FileIdentificationPredicate(IDENTIFIER, true);
    assertThat(filePredicate.apply(inputFile("big_file_identifier_in_buffer.txt", "text"))).isTrue();
  }

  @Test
  void shouldNotFindIdentifierInBigFileIdentifierAfterBuffer() {
    var filePredicate = new FileIdentificationPredicate(IDENTIFIER, true);
    assertThat(filePredicate.apply(inputFile("big_file_identifier_after_buffer.txt", "text"))).isFalse();
  }

  @Test
  void shouldErrorMessageWhenFileNotFound() throws IOException {
    InputFile noFile = mock(InputFile.class);
    when(noFile.inputStream()).thenThrow(new IOException("File not found mock"));
    when(noFile.toString()).thenReturn("nofile.txt");

    var filePredicate = new FileIdentificationPredicate(IDENTIFIER, true);
    assertThat(filePredicate.apply(noFile)).isFalse();
    assertThat(logTester.logs(Level.WARN)).hasSize(2);
    assertThat(logTester.logs(Level.WARN).get(0)).isEqualTo("Unable to read file: nofile.txt.");
    assertThat(logTester.logs(Level.WARN).get(1)).startsWith("File not found mock");
    assertThat(logTester.logs(Level.DEBUG)).hasSize(1);
    assertThat(logTester.logs(Level.DEBUG).get(0)).startsWith("File without identifier '" + IDENTIFIER + "': nofile.txt");
  }

  @Test
  void shouldErrorMessageWhenFileNotFoundWithMultipleIdentifier() throws IOException {
    InputFile noFile = mock(InputFile.class);
    when(noFile.inputStream()).thenThrow(new IOException("File not found mock"));
    when(noFile.toString()).thenReturn("nofile.txt");

    var filePredicate = new FileIdentificationPredicate(List.of(IDENTIFIER, IDENTIFIER), true);
    assertThat(filePredicate.apply(noFile)).isFalse();
    assertThat(logTester.logs(Level.WARN)).hasSize(2);
    assertThat(logTester.logs(Level.WARN).get(0)).isEqualTo("Unable to read file: nofile.txt.");
    assertThat(logTester.logs(Level.WARN).get(1)).startsWith("File not found mock");
    assertThat(logTester.logs(Level.DEBUG)).hasSize(1);
    assertThat(logTester.logs(Level.DEBUG).get(0))
      .startsWith("File without any identifiers '[%s, %s]': nofile.txt".formatted(IDENTIFIER, IDENTIFIER));
  }

  @Test
  void shouldNotErrorMessageWhenFileNotFoundAndLogDisabled() throws IOException {
    InputFile noFile = mock(InputFile.class);
    when(noFile.inputStream()).thenThrow(new IOException("File not found mock"));
    when(noFile.toString()).thenReturn("nofile.txt");

    var filePredicate = new FileIdentificationPredicate(IDENTIFIER, false);
    assertThat(filePredicate.apply(noFile)).isFalse();
    assertThat(logTester.logs(Level.WARN)).hasSize(2);
    assertThat(logTester.logs(Level.WARN).get(0)).isEqualTo("Unable to read file: nofile.txt.");
    assertThat(logTester.logs(Level.WARN).get(1)).startsWith("File not found mock");
    assertThat(logTester.logs(Level.DEBUG)).isEmpty();
  }

}
