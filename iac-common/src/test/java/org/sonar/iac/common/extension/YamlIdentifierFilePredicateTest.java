/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.Mockito.when;

class YamlIdentifierFilePredicateTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  @ParameterizedTest
  @MethodSource("provideTestCases")
  void shouldVerifyFileIdentifier(String content, Set<String> identifiers, boolean expectedResult) throws IOException {
    var inputFile = Mockito.mock(InputFile.class);
    when(inputFile.inputStream()).thenReturn(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    when(inputFile.charset()).thenReturn(StandardCharsets.UTF_8);
    when(inputFile.toString()).thenReturn("filename.txt");

    var yamlFileIdentifier = new YamlIdentifierFilePredicate(identifiers);
    assertThat(yamlFileIdentifier.apply(inputFile)).isEqualTo(expectedResult);
  }

  private static Stream<Arguments> provideTestCases() {
    return Stream.of(
      of("foo:\nbar:", Set.of("^foo:", "^bar"), true),
      of("foo:\nbar:", Set.of("^foo:", "^baz"), false),
      of("foo:\n---\nbar:\n", Set.of("^foo:", "^bar"), false),
      of("foo:\n  bar:", Set.of("^foo:", "^bar"), false),
      of("foo:\n  bar:", Set.of("foo:", "bar"), true),
      of("foo:\n  bar:", Set.of("^foo:"), true),
      of("- foo:\n  bar:", Set.of("^foo:"), false),
      of("", Set.of("^foo:", "^bar"), false));
  }

  @Test
  void shouldVerifyIOException() throws IOException {
    InputFile inputFile = Mockito.mock(InputFile.class);
    when(inputFile.inputStream()).thenThrow(new IOException("boom"));
    when(inputFile.toString()).thenReturn("filename.txt");

    var yamlFileIdentifier = new YamlIdentifierFilePredicate(Set.of("foo"));
    assertThat(yamlFileIdentifier.apply(inputFile)).isFalse();
    assertThat(logTester.logs(Level.ERROR)).contains("Unable to read file: filename.txt.");
  }
}
