/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
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

public abstract class AbstractMetricsTest {

  protected NoSonarFilter noSonarFilter = mock(NoSonarFilter.class);
  protected TreeParser<Tree> parser;
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
  }

  protected abstract TreeParser<Tree> treeParser();

  protected abstract MetricsVisitor metricsVisitor(FileLinesContextFactory fileLinesContextFactory);

  protected void scan(String code) {
    inputFile = new TestInputFileBuilder("moduleKey", new File(tempFolder, "file").getName())
      .setCharset(StandardCharsets.UTF_8)
      .initMetadata(code).build();
    InputFileContext ctx = new InputFileContext(sensorContext, inputFile);
    visitor.scan(ctx, parser.parse(code, null));
  }
}
