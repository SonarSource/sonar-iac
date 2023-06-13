package org.sonar.iac.common.extension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ParseExceptionTest {

  private final RuntimeException cause = new RuntimeException("cause");
  private final TextPointer position = new BasicTextPointer(1, 2);
  private InputFile inputFile;

  @BeforeEach
  public void init() {
    inputFile = mock(InputFile.class);
    when(inputFile.filename()).thenReturn("TestFile.abc");
  }

  @Test
  void shouldCreateException() {
    ParseException actual = ParseException.throwParseException("action", inputFile, cause, position);

    assertThat(actual)
      .hasMessage("Cannot action 'TestFile.abc:1:3'")
      .extracting(ParseException::getPosition)
      .isEqualTo(position);
    assertThat(actual)
      .extracting(ParseException::getDetails)
      .isEqualTo("cause");
  }

  @Test
  void shouldCreateExceptionNullInputFile() {
    ParseException actual = ParseException.throwParseException("action", null, cause, position);

    assertThat(actual)
      .hasMessage("Cannot action 'null:1:3'")
      .extracting(ParseException::getPosition)
      .isEqualTo(position);
    assertThat(actual)
      .extracting(ParseException::getDetails)
      .isEqualTo("cause");
  }

  @Test
  void shouldCreateNullPosition() {
    ParseException actual = ParseException.throwParseException("action", inputFile, cause, null);

    assertThat(actual)
      .hasMessage("Cannot action 'TestFile.abc'")
      .extracting(ParseException::getPosition)
      .isNull();
    assertThat(actual)
      .extracting(ParseException::getDetails)
      .isEqualTo("cause");
  }
}
