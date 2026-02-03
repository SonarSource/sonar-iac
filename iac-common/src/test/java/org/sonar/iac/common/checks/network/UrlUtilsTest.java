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
package org.sonar.iac.common.checks.network;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.iac.common.api.tree.impl.TextPointer;
import org.sonar.iac.common.testing.TextRangeAssert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.checks.network.UrlUtils.findUnencryptedUrlsOffsets;
import static org.sonar.iac.common.checks.network.UrlUtils.isSensitiveUnencryptedUrl;
import static org.sonar.iac.common.checks.network.UrlUtils.isUnencryptedUrl;

class UrlUtilsTest {

  static Stream<Arguments> urlTestCases() {
    return Stream.of(
      Arguments.of("http://example.com", true, true),
      Arguments.of("http://example.com:8080", true, true),
      Arguments.of("https://example.com", false, false),
      Arguments.of("https://example.com:8080", false, false),
      Arguments.of("ftp://example.com/path/to/file.txt", true, true),
      Arguments.of("ftps://example.com/path/to/file.txt", false, false),
      Arguments.of("http://127.0.0.1:8080", true, false),
      Arguments.of("ftp://127.0.0.1:8080/path/to/file.txt", true, false),
      Arguments.of("http://0:0:0:0:0:0:0:01", true, false),
      Arguments.of("http://localhost", true, false),
      Arguments.of("http://localhost:8080", true, false),
      Arguments.of("http://169.254.169.254", true, false),
      Arguments.of("ftp://localhost/path/to/file.txt", true, false));
  }

  @ParameterizedTest
  @MethodSource("urlTestCases")
  void shouldMatchSensitiveUnencryptedUrls(String url, boolean isUnencrypted, boolean isSensitive) {
    assertThat(isSensitiveUnencryptedUrl(url)).isEqualTo(isSensitive);
  }

  @ParameterizedTest
  @MethodSource("urlTestCases")
  void shouldMatchUnencryptedUrls(String url, boolean isUnencrypted, boolean isSensitive) {
    assertThat(isUnencryptedUrl(url)).isEqualTo(isUnencrypted);
  }

  @Test
  void shouldFindOffsetOfUnencryptedUrls() {
    var code = "line of text http://example.com and the rest of text";
    var initialShift = 10;
    var textRanges = findUnencryptedUrlsOffsets(new TextPointer(1, initialShift), code);
    assertThat(textRanges)
      .hasSize(1)
      .element(0)
      .satisfies(textRange -> TextRangeAssert.assertThat(textRange).hasRange(1, initialShift + 13, 1, initialShift + 31));
  }
}
