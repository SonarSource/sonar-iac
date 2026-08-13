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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import org.sonar.api.batch.fs.InputFile;

/**
 * Reads and splits the first {@value #DEFAULT_BUFFER_SIZE} bytes of an {@link InputFile} into lines, memoizing the
 * result for the single most recently read file - {@code YamlFileTypeResolver.classify} runs several content-based
 * predicates over the same candidate in a row, so this collapses their reads into one per file. Read failures are not
 * memoized.
 */
public class SharedFileHeadReader {

  private static final int DEFAULT_BUFFER_SIZE = 8192;
  private static final Pattern LINE_TERMINATOR = Pattern.compile("[\\n\\r\\u2028\\u2029]");

  @Nullable
  private InputFile lastFile;
  private String[] lastLines = new String[0];

  /**
   * The returned array is shared internal state, not a copy - callers must not mutate it.
   */
  public String[] readLines(InputFile inputFile) throws IOException {
    if (inputFile == lastFile) {
      return lastLines;
    }
    try (var bufferedInputStream = new BufferedInputStream(inputFile.inputStream())) {
      // Only first 8k bytes is read to avoid slow execution for big one-line files
      byte[] bytes = bufferedInputStream.readNBytes(DEFAULT_BUFFER_SIZE);
      var text = new String(bytes, inputFile.charset());
      var lines = LINE_TERMINATOR.split(text);
      lastFile = inputFile;
      lastLines = lines;
      return lines;
    }
  }
}
