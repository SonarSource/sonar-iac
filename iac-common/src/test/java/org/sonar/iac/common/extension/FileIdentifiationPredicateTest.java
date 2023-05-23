package org.sonar.iac.common.extension;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.utils.log.LogTesterJUnit5;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.testing.IacTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileIdentifiationPredicateTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();

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
    assertThat(logTester.logs(LoggerLevel.ERROR)).hasSize(2);
    assertThat(logTester.logs(LoggerLevel.ERROR).get(0)).isEqualTo("Unable to read file: nofile.txt.");
    assertThat(logTester.logs(LoggerLevel.ERROR).get(1)).startsWith("File not found mock");
    assertThat(logTester.logs(LoggerLevel.DEBUG)).hasSize(1);
    assertThat(logTester.logs(LoggerLevel.DEBUG).get(0)).startsWith("File without provided identifier: nofile.txt");
  }

}
