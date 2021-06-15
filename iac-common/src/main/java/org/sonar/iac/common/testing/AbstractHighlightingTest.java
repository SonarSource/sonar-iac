/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.SyntaxHighlightingVisitor;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractHighlightingTest {

  private final SyntaxHighlightingVisitor highlightingVisitor;
  private final TreeParser parser;
  private SensorContextTester sensorContext;
  private DefaultInputFile inputFile;

  protected AbstractHighlightingTest(SyntaxHighlightingVisitor highlightingVisitor, TreeParser parser) {
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
