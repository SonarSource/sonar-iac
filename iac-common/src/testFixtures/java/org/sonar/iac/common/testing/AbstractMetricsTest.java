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
package org.sonar.iac.common.testing;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.MetricsVisitor;
import org.sonar.iac.common.extension.visitors.SensorTelemetry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class AbstractMetricsTest {

  protected NoSonarFilter noSonarFilter = mock(NoSonarFilter.class);
  protected TreeParser<? extends Tree> parser;
  protected String language;
  protected MetricsVisitor visitor;
  protected SensorContextTester sensorContext;
  protected InputFile inputFile;
  protected FileLinesContext fileLinesContext;
  protected SensorTelemetry sensorTelemetry;
  protected FileLinesContextFactory fileLinesContextFactory;

  @TempDir
  public File tempFolder;

  @BeforeEach
  void setUp() {
    sensorContext = spy(SensorContextTester.create(tempFolder));
    fileLinesContext = mock(FileLinesContext.class);
    fileLinesContextFactory = mock(FileLinesContextFactory.class);
    when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(fileLinesContext);
    sensorTelemetry = spy(new SensorTelemetry());

    parser = treeParser();
    visitor = metricsVisitor(fileLinesContextFactory);
    language = languageKey();
  }

  protected abstract String languageKey();

  protected abstract TreeParser<? extends Tree> treeParser();

  protected abstract MetricsVisitor metricsVisitor(FileLinesContextFactory fileLinesContextFactory);

  protected MetricsVisitor scan(String code) {
    return scan(code, "file");
  }

  protected MetricsVisitor scan(String code, String filename) {
    return scan(new TestInputFileBuilder("moduleKey", new File(tempFolder, filename).getName())
      .setCharset(StandardCharsets.UTF_8)
      .setLanguage(language)
      .setContents(code)
      .build());
  }

  protected MetricsVisitor scan(InputFile inputFile) {
    this.inputFile = inputFile;
    var inputFileContext = new InputFileContext(sensorContext, inputFile);
    try {
      visitor.scan(inputFileContext, parser.parse(inputFile.contents(), inputFileContext));
    } catch (IOException e) {
      throw new RuntimeException("Failed to retrieve content of inputFile: " + inputFile.filename(), e);
    }
    return visitor;
  }

  protected void verifyLinesOfCodeMetricsAndTelemetry(Integer... linesOfCode) {
    verifyNCLOCDataMetric(linesOfCode);
    verify(sensorContext, times(2)).newMeasure();
    verifyTelemetry(linesOfCode.length);
  }

  protected void verifyTelemetry(Integer linesOfCode) {
    verify(sensorTelemetry).addLinesOfCode(linesOfCode);
  }

  protected void verifyNCLOCDataMetric(Integer... linesOfCode) {
    var linesOfCodeSet = Arrays.stream(linesOfCode).collect(Collectors.toSet());
    for (var lineNumber = 1; lineNumber <= inputFile.lines(); lineNumber++) {
      if (linesOfCodeSet.contains(lineNumber)) {
        verify(fileLinesContext).setIntValue(CoreMetrics.NCLOC_DATA_KEY, lineNumber, 1);
      } else {
        verify(fileLinesContext, never()).setIntValue(CoreMetrics.NCLOC_DATA_KEY, lineNumber, 1);
      }
    }
  }
}
