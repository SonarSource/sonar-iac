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
package org.sonar.iac.common.extension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.InputFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Stream;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.Mockito.when;

class AbstractYamlFileIdentifierTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  private static class TestYamlFileIdentifier extends AbstractYamlFileIdentifier {
    private static final Logger LOG = LoggerFactory.getLogger(TestYamlFileIdentifier.class);

    private final String debugMessage;

    TestYamlFileIdentifier(Set<String> identifiers, boolean isDebugEnabled, String debugMessage) {
      super(identifiers, isDebugEnabled);
      this.debugMessage = debugMessage;
    }

    @Override
    protected void logDebugMessage(InputFile inputFile) {
      LOG.debug(debugMessage, inputFile);
    }
  }

  @ParameterizedTest
  @MethodSource("provideTestCases")
  void shouldVerifyFileIdentifier(String content, Set<String> identifiers, boolean expectedResult) throws IOException {
    var inputFile = Mockito.mock(InputFile.class);
    when(inputFile.inputStream()).thenReturn(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    when(inputFile.charset()).thenReturn(StandardCharsets.UTF_8);
    when(inputFile.toString()).thenReturn("filename.txt");

    var yamlFileIdentifier = new TestYamlFileIdentifier(identifiers, true, "File without some identifier: {}");
    assertThat(yamlFileIdentifier.apply(inputFile)).isEqualTo(expectedResult);
    if (expectedResult) {
      assertThat(logTester.logs(Level.DEBUG)).isEmpty();
    } else {
      assertThat(logTester.logs(Level.DEBUG)).contains("File without some identifier: filename.txt");
    }
  }

  private static Stream<Arguments> provideTestCases() {
    return Stream.of(
      of("foo:\nbar:", Set.of("foo:", "bar"), true),
      of("foo:\nbar:", Set.of("foo:", "baz"), false),
      of("foo:\n---\nbar:\n", Set.of("foo:", "bar"), false),
      of("foo:\n  bar:", Set.of("foo:", "bar"), false),
      of("foo:\n  bar:", Set.of("foo:"), true),
      of("- foo:\n  bar:", Set.of("foo:"), false),
      of("", Set.of("foo:", "bar"), false));
  }

  @Test
  void shouldVerifyIOException() throws IOException {
    InputFile inputFile = Mockito.mock(InputFile.class);
    when(inputFile.inputStream()).thenThrow(new IOException("boom"));
    when(inputFile.toString()).thenReturn("filename.txt");

    var yamlFileIdentifier = new TestYamlFileIdentifier(Set.of("foo"), true, "foo");
    assertThat(yamlFileIdentifier.apply(inputFile)).isFalse();
    assertThat(logTester.logs(Level.ERROR)).contains("Unable to read file: filename.txt.");
  }

  @Test
  void shouldNotLogWhenDebugDisabled() throws IOException {
    var inputFile = Mockito.mock(InputFile.class);
    when(inputFile.inputStream()).thenReturn(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));
    when(inputFile.charset()).thenReturn(StandardCharsets.UTF_8);

    var yamlFileIdentifier = new TestYamlFileIdentifier(Set.of("foo"), false, "File without some identifier: {}");
    assertThat(yamlFileIdentifier.apply(inputFile)).isFalse();
    assertThat(logTester.logs(Level.DEBUG)).isEmpty();
  }
}
