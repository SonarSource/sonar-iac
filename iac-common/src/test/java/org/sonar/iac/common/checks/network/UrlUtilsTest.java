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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.common.api.tree.impl.TextPointer;
import org.sonar.iac.common.testing.TextRangeAssert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.checks.network.UrlUtils.findUnencryptedUrlsOffsets;
import static org.sonar.iac.common.checks.network.UrlUtils.isUnencryptedUrl;

class UrlUtilsTest {
  @ParameterizedTest
  @CsvSource(textBlock = """
    http://example.com,true
    http://example.com:8080,true
    https://example.com,false
    https://example.com:8080,false
    ftp://example.com/path/to/file.txt,true
    ftps://example.com/path/to/file.txt,false
    http://127.0.0.1:8080,false
    ftp://127.0.0.1:8080/path/to/file.txt,false
    http://0:0:0:0:0:0:0:01,false
    http://localhost,false
    http://localhost:8080,false
    ftp://localhost/path/to/file.txt,false
    """)
  void shouldMatchUnencryptedUrls(String url, boolean shouldMatch) {
    assertThat(isUnencryptedUrl(url)).isEqualTo(shouldMatch);
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
