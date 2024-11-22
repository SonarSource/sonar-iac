/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.common.testing;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import javax.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.SyntaxHighlightingVisitor;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractHighlightingTest {

  protected final SyntaxHighlightingVisitor highlightingVisitor;
  private final TreeParser<? extends Tree> parser;
  protected SensorContextTester sensorContext;
  private DefaultInputFile inputFile;
  private String code;

  protected AbstractHighlightingTest(SyntaxHighlightingVisitor highlightingVisitor, TreeParser<? extends Tree> parser) {
    this.highlightingVisitor = highlightingVisitor;
    this.parser = parser;
  }

  @TempDir
  public Path tempFolder;

  @BeforeEach
  void setUp() {
    sensorContext = SensorContextTester.create(tempFolder);
  }

  protected void highlight(String code) {
    highlight(code, null, null);
  }

  protected void highlight(String code, @Nullable Path filePath, @Nullable String languageKey) {
    var relativePath = tempFolder;
    if (filePath != null) {
      relativePath = relativePath.resolve(filePath);
    }
    var inputFileBuilder = new TestInputFileBuilder("moduleKey", relativePath.getFileName().toString())
      .setCharset(StandardCharsets.UTF_8)
      .initMetadata(code)
      .setContents(code);
    if (languageKey != null) {
      inputFileBuilder.setLanguage(languageKey);
    }

    inputFile = inputFileBuilder.build();
    this.code = code;
    InputFileContext ctx = new InputFileContext(sensorContext, inputFile);
    highlightingVisitor.scan(ctx, parser.parse(code, ctx));
  }

  protected void highlight(Tree root) {
    inputFile = new TestInputFileBuilder("moduleKey", tempFolder.getFileName().toString())
      .setCharset(StandardCharsets.UTF_8)
      .initMetadata("dummy")
      .build();
    InputFileContext ctx = new InputFileContext(sensorContext, inputFile);
    highlightingVisitor.scan(ctx, root);
  }

  protected void assertHighlighting(int columnFirst, int columnLast, @Nullable TypeOfText type) {
    assertHighlighting(1, columnFirst, columnLast, type);
  }

  protected void assertHighlighting(int line, int columnFirst, int columnLast, @Nullable TypeOfText type) {
    for (int i = columnFirst; i <= columnLast; i++) {
      List<TypeOfText> typeOfTexts = sensorContext.highlightingTypeAt(inputFile.key(), line, i);
      if (type != null) {
        assertThat(typeOfTexts).as("Expect highlighting " + type + " at line " + line + " lineOffset " + i).containsExactly(type);
      } else {
        assertThat(typeOfTexts).as("Expect no highlighting at line " + line + " lineOffset " + i).containsExactly();
      }
    }
  }

  protected void assertHighlighting(String text, TypeOfText typeOfText) {
    assertHighlighting(1, text, typeOfText);
  }

  protected void assertHighlighting(int line, String text, TypeOfText typeOfText) {
    String[] lines = code.split("\\n");
    int startColumn = lines[line - 1].indexOf(text);
    if (startColumn == -1) {
      throw new RuntimeException(String.format("Not found `%s` in line:\n%s", text, lines[line - 1]));
    }
    int endColumn = startColumn + text.length();
    assertHighlighting(line, startColumn, endColumn - 1, typeOfText);
  }
}
