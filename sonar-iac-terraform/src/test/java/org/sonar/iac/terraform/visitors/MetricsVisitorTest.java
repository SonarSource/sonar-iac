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
package org.sonar.iac.terraform.visitors;

import com.sonar.sslr.api.typed.ActionParser;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.parser.HclParser;
import org.sonar.iac.terraform.plugin.InputFileContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MetricsVisitorTest {

  private NoSonarFilter mockNoSonarFilter;
  private ActionParser<TerraformTree> parser = new HclParser();
  private MetricsVisitor visitor;
  private SensorContextTester sensorContext;
  private DefaultInputFile inputFile;

  @TempDir
  public File tempFolder;

  @BeforeEach
  void setUp() {
    sensorContext = SensorContextTester.create(tempFolder);
    FileLinesContext mockFileLinesContext = mock(FileLinesContext.class);
    FileLinesContextFactory mockFileLinesContextFactory = mock(FileLinesContextFactory.class);
    mockNoSonarFilter = mock(NoSonarFilter.class);
    when(mockFileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(mockFileLinesContext);
    visitor = new MetricsVisitor(mockFileLinesContextFactory, mockNoSonarFilter);
  }

  @Test
  void emptySource() throws Exception {
    scan("");
    assertThat(visitor.linesOfCode).isEmpty();
    assertThat(visitor.commentLines).isEmpty();
    verify(mockNoSonarFilter).noSonarInFile(inputFile, new HashSet<>());
  }

  @Test
  void linesOfCode() throws Exception {
    scan("" +
      "a {\n" +
      "   // comment\n" +
      "   b = {}\n" +
      "}");
    assertThat(visitor.linesOfCode).containsExactly(1, 3, 4);
  }

  @Test
  void commentLines() throws Exception {
    scan("" +
      "/* comment */ a {\n" +
      "   // comment\n" +
      "   b = {} // comment\n" +
      "}");
    assertThat(visitor.commentLines).containsExactly(1, 2, 3);
  }

  @Test
  void multiLineComment() throws Exception {
    scan("" +
      "/*start\n" +
      " a = {}\n" +
      " end\n" +
      "*/");
    assertThat(visitor.commentLines).containsExactly(1, 2, 3);
    assertThat(visitor.linesOfCode).isEmpty();
  }

  @Test
  void noSonarLines() throws Exception {
    scan("" +
      "a {\n" +
      "// NOSONAR comment\n" +
      "b = {}\n" +
      "}");
    assertThat(visitor.noSonarLines).containsExactly(2);
    Set<Integer> nosonarLines = new HashSet<>();
    nosonarLines.add(2);
    verify(mockNoSonarFilter).noSonarInFile(inputFile, nosonarLines);
  }


  private void scan(String code) throws IOException {
    inputFile = new TestInputFileBuilder("moduleKey", new File(tempFolder, "file").getName())
      .setCharset(StandardCharsets.UTF_8)
      .initMetadata(code).build();
    InputFileContext ctx = new InputFileContext(sensorContext, inputFile);
    visitor.scan(ctx, parser.parse(code));
  }

}
