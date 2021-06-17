/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.common.testing;

import java.io.File;
import java.nio.charset.StandardCharsets;
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

  private final SyntaxHighlightingVisitor highlightingVisitor;
  private final TreeParser<Tree> parser;
  private SensorContextTester sensorContext;
  private DefaultInputFile inputFile;

  protected AbstractHighlightingTest(SyntaxHighlightingVisitor highlightingVisitor, TreeParser<Tree> parser) {
    this.highlightingVisitor = highlightingVisitor;
    this.parser = parser;
  }

  @TempDir
  public File tempFolder;

  @BeforeEach
  void setUp() {
    sensorContext = SensorContextTester.create(tempFolder);
  }

  protected void highlight(String code) {
    inputFile = new TestInputFileBuilder("moduleKey", tempFolder.getName())
      .setCharset(StandardCharsets.UTF_8)
      .initMetadata(code).build();
    InputFileContext ctx = new InputFileContext(sensorContext, inputFile);
    highlightingVisitor.scan(ctx, parser.parse(code, null));
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
}
