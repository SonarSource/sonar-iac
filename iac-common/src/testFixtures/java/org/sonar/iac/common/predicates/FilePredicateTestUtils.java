/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
package org.sonar.iac.common.predicates;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.sonar.api.batch.fs.InputFile;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FilePredicateTestUtils {

  private FilePredicateTestUtils() {
    // util class
  }

  public static InputFile newInputFileMock(String path, String validContent) throws IOException {
    InputFile inputFile = mock(InputFile.class);
    String filename = path.substring(path.lastIndexOf("/") + 1);
    // the file is sometimes read twice by 2 predicates
    when(inputFile.inputStream()).thenAnswer(invocationOnMock -> new ByteArrayInputStream(validContent.getBytes(StandardCharsets.UTF_8)));
    when(inputFile.charset()).thenReturn(StandardCharsets.UTF_8);
    when(inputFile.toString()).thenReturn(filename);
    when(inputFile.path()).thenReturn(Path.of(path));
    when(inputFile.filename()).thenReturn(filename);
    when(inputFile.relativePath()).thenReturn(path);
    return inputFile;
  }
}
