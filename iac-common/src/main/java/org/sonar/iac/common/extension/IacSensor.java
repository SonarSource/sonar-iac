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
package org.sonar.iac.common.extension;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.sonar.api.SonarProduct;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.resources.Language;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonarsource.analyzer.commons.ProgressReport;

public abstract class IacSensor implements Sensor {

  private static final Logger LOG = Loggers.get(IacSensor.class);
  private static final Pattern EMPTY_FILE_CONTENT_PATTERN = Pattern.compile("\\s*+");

  protected FileLinesContextFactory fileLinesContextFactory;
  protected final NoSonarFilter noSonarFilter;
  private final Language language;

  protected IacSensor(FileLinesContextFactory fileLinesContextFactory, NoSonarFilter noSonarFilter, Language language) {
    this.fileLinesContextFactory = fileLinesContextFactory;
    this.noSonarFilter = noSonarFilter;
    this.language = language;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .onlyOnLanguage(language.getKey())
      .name("IaC " + language.getName() + " Sensor");
  }

  protected abstract TreeParser<Tree> treeParser();

  protected abstract String repositoryKey();

  protected abstract List<TreeVisitor<InputFileContext>> visitors(SensorContext sensorContext, DurationStatistics statistics);

  protected abstract boolean isActive(SensorContext sensorContext);

  @Override
  public void execute(SensorContext sensorContext) {
    if (!isActive(sensorContext)) {
      return;
    }

    DurationStatistics statistics = new DurationStatistics(sensorContext.config());
    FileSystem fileSystem = sensorContext.fileSystem();
    FilePredicate mainFilePredicate = fileSystem.predicates().and(
      fileSystem.predicates().hasLanguage(language.getKey()),
      fileSystem.predicates().hasType(InputFile.Type.MAIN));
    Iterable<InputFile> inputFiles = fileSystem.inputFiles(mainFilePredicate);
    List<String> filenames = StreamSupport.stream(inputFiles.spliterator(), false).map(InputFile::toString).collect(Collectors.toList());
    ProgressReport progressReport = new ProgressReport("Progress of the " + language.getName() + " analysis", TimeUnit.SECONDS.toMillis(10));
    progressReport.start(filenames);
    boolean success = false;
    Analyzer analyzer = new Analyzer(treeParser(), visitors(sensorContext, statistics), statistics);
    try {
      success = analyzer.analyseFiles(sensorContext, inputFiles, progressReport);
    } finally {
      if (success) {
        progressReport.stop();
      } else {
        progressReport.cancel();
      }
    }
    statistics.log();
  }

  protected boolean isSonarLintContext(SensorContext sensorContext) {
    return sensorContext.runtime().getProduct() != SonarProduct.SONARLINT;
  }

  protected ParseException toParseException(String action, InputFile inputFile, Exception cause) {
    return new ParseException("Cannot " + action + " '" + inputFile + "': " + cause.getMessage(), null);
  }

  private class Analyzer {

    private final TreeParser<Tree> parser;
    private final List<TreeVisitor<InputFileContext>> visitors;
    private final DurationStatistics statistics;

    public Analyzer(TreeParser<Tree> parser, List<TreeVisitor<InputFileContext>> visitors, DurationStatistics statistics) {
      this.parser = parser;
      this.visitors = visitors;
      this.statistics = statistics;
    }

    boolean analyseFiles(SensorContext sensorContext, Iterable<InputFile> inputFiles, ProgressReport progressReport) {
      for (InputFile inputFile : inputFiles) {
        if (sensorContext.isCancelled()) {
          return false;
        }
        InputFileContext inputFileContext = new InputFileContext(sensorContext, inputFile);
        try {
          analyseFile(inputFileContext);
        } catch (ParseException e) {
          logParsingError(inputFile, e);
          inputFileContext.reportParseError(repositoryKey(), e.getPosition());
        }
        progressReport.nextFile();
      }
      return true;
    }

    private void analyseFile(InputFileContext inputFileContext) {
      InputFile inputFile = inputFileContext.inputFile;
      String content;
      try {
        content = inputFile.contents();
      } catch (IOException | RuntimeException e) {
        throw toParseException("read", inputFile, e);
      }

      if (EMPTY_FILE_CONTENT_PATTERN.matcher(content).matches()) {
        return;
      }

      Tree tree = statistics.time("Parse", () -> {
        try {
          return parser.parse(content, inputFileContext);
        } catch (RuntimeException e) {
          throw toParseException("parse", inputFile, e);
        }
      });

      for (TreeVisitor<InputFileContext> visitor : visitors) {
        try {
          String visitorId = visitor.getClass().getSimpleName();
          statistics.time(visitorId, () -> visitor.scan(inputFileContext, tree));
        } catch (RuntimeException e) {
          inputFileContext.reportAnalysisError(e.getMessage(), null);
          LOG.error("Cannot analyse '" + inputFile +"': " + e.getMessage(), e);
        }
      }
    }

    private void logParsingError(InputFile inputFile, ParseException e) {
      TextPointer position = e.getPosition();
      String positionMessage = "";
      if (position != null) {
        positionMessage = String.format("Parse error at position %s:%s", position.line(), position.lineOffset());
      }
      LOG.error(String.format("Unable to parse file: %s. %s", inputFile.uri(), positionMessage));
      LOG.error(e.getMessage());
    }
  }
}
