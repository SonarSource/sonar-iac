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
package org.sonar.iac.common.testing;

import java.io.File;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.MetricsVisitor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.testing.IacTestUtils.code;

public abstract class AbstractMetricsTest {

  protected NoSonarFilter noSonarFilter = mock(NoSonarFilter.class);
  protected TreeParser<? extends Tree> parser;
  protected String language;
  protected MetricsVisitor visitor;
  protected SensorContextTester sensorContext;
  protected DefaultInputFile inputFile;

  @TempDir
  public File tempFolder;

  @BeforeEach
  void setUp() {
    sensorContext = SensorContextTester.create(tempFolder);
    FileLinesContext fileLinesContext = mock(FileLinesContext.class);
    FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
    when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(fileLinesContext);

    parser = treeParser();
    visitor = metricsVisitor(fileLinesContextFactory);
    language = languageKey();
  }

  protected abstract String languageKey();

  protected abstract TreeParser<? extends Tree> treeParser();

  protected abstract MetricsVisitor metricsVisitor(FileLinesContextFactory fileLinesContextFactory);

  protected MetricsVisitor scan(String... codeLines) {
    return scan(code(codeLines));
  }

  protected MetricsVisitor scan(String code) {
    return scan(code, "file");
  }

  protected MetricsVisitor scan(String code, String filename) {
    inputFile = new TestInputFileBuilder("moduleKey", new File(tempFolder, filename).getName())
      .setCharset(StandardCharsets.UTF_8)
      .setLanguage(language)
      .setContents(code)
      .build();
    var inputFileContext = new InputFileContext(sensorContext, inputFile);
    visitor.scan(inputFileContext, parser.parse(code, inputFileContext));
    return visitor;
  }
}
