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
package org.sonar.iac.common.extension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SharedFileHeadReaderTest {

  private final SharedFileHeadReader sharedFileHeadReader = new SharedFileHeadReader();

  @Test
  void shouldReadFileOnlyOnceForRepeatedCallsOnTheSameFile() throws IOException {
    var inputFile = mockInputFile("line1\nline2\nline3");

    var firstRead = sharedFileHeadReader.readLines(inputFile);
    var secondRead = sharedFileHeadReader.readLines(inputFile);

    assertThat(firstRead).containsExactly("line1", "line2", "line3");
    assertThat(secondRead).isSameAs(firstRead);
    verify(inputFile, times(1)).inputStream();
  }

  @Test
  void shouldReadAgainWhenTheFileDiffersFromTheMemoizedOne() throws IOException {
    var fileA = mockInputFile("a-line1\na-line2");
    var fileB = mockInputFile("b-line1");

    var readA1 = sharedFileHeadReader.readLines(fileA);
    var readB = sharedFileHeadReader.readLines(fileB);
    var readA2 = sharedFileHeadReader.readLines(fileA);

    assertThat(readA1).containsExactly("a-line1", "a-line2");
    assertThat(readB).containsExactly("b-line1");
    assertThat(readA2).containsExactly("a-line1", "a-line2");
    // Alternating A, B, A: the single slot only ever remembers the most recent file, so all three calls miss.
    verify(fileA, times(2)).inputStream();
    verify(fileB, times(1)).inputStream();
  }

  @Test
  void shouldSplitOnAllSupportedLineTerminators() throws IOException {
    // Line separator (U+2028) and paragraph separator (U+2029) are built from their code points rather than
    // embedded as literal characters, since the raw characters in source confuse Spotless' toggleOffOn() lint.
    var content = "a\nb\rc" + Character.toString(0x2028) + "d" + Character.toString(0x2029) + "e";
    var inputFile = mockInputFile(content);

    var lines = sharedFileHeadReader.readLines(inputFile);

    assertThat(lines).containsExactly("a", "b", "c", "d", "e");
  }

  @Test
  void shouldTruncateAtDefaultBufferSize() throws IOException {
    var oneLine = "x".repeat(8192 + 100);
    var inputFile = mockInputFile(oneLine);

    var lines = sharedFileHeadReader.readLines(inputFile);

    assertThat(lines).hasSize(1);
    assertThat(lines[0]).hasSize(8192);
  }

  @Test
  void shouldDecodeUsingTheInputFilesCharset() throws IOException {
    var text = "café";
    var bytes = text.getBytes(StandardCharsets.ISO_8859_1);
    var inputFile = mock(InputFile.class);
    when(inputFile.inputStream()).thenAnswer(invocation -> new ByteArrayInputStream(bytes));
    when(inputFile.charset()).thenReturn(StandardCharsets.ISO_8859_1);

    var lines = sharedFileHeadReader.readLines(inputFile);

    assertThat(lines).containsExactly(text);
  }

  @Test
  void shouldPropagateIOExceptionWithoutMemoizingTheFailure() throws IOException {
    var inputFile = mock(InputFile.class);
    when(inputFile.inputStream()).thenThrow(new IOException("boom"));

    assertThatThrownBy(() -> sharedFileHeadReader.readLines(inputFile))
      .isInstanceOf(IOException.class)
      .hasMessage("boom");

    var retryFile = mockInputFile("line1");
    assertThat(sharedFileHeadReader.readLines(retryFile)).containsExactly("line1");
  }

  private static InputFile mockInputFile(String content) throws IOException {
    var inputFile = mock(InputFile.class);
    var bytes = content.getBytes(StandardCharsets.UTF_8);
    when(inputFile.inputStream()).thenAnswer(invocation -> new ByteArrayInputStream(bytes));
    when(inputFile.charset()).thenReturn(StandardCharsets.UTF_8);
    return inputFile;
  }
}
