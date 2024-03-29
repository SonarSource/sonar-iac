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
package org.sonar.iac.common.extension;

import com.sonar.sslr.api.RecognitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.SonarProduct;
import org.sonar.api.SonarRuntime;
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
import org.sonar.api.utils.Version;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonarsource.analyzer.commons.ProgressReport;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import static org.sonar.iac.common.extension.ParseException.createGeneralParseException;

public abstract class IacSensor implements Sensor {

  public static final String FAIL_FAST_PROPERTY_NAME = "sonar.internal.analysis.failFast";
  private static final Logger LOG = LoggerFactory.getLogger(IacSensor.class);
  private static final Pattern EMPTY_FILE_CONTENT_PATTERN = Pattern.compile("\\s*+");

  protected final SonarRuntime sonarRuntime;
  protected final FileLinesContextFactory fileLinesContextFactory;
  protected final NoSonarFilter noSonarFilter;
  protected final Language language;

  protected IacSensor(SonarRuntime sonarRuntime, FileLinesContextFactory fileLinesContextFactory, NoSonarFilter noSonarFilter, Language language) {
    this.sonarRuntime = sonarRuntime;
    this.fileLinesContextFactory = fileLinesContextFactory;
    this.noSonarFilter = noSonarFilter;
    this.language = language;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .onlyOnLanguage(language.getKey())
      .name("IaC " + language.getName() + " Sensor");

    if (sonarRuntime.getApiVersion().isGreaterThanOrEqual(Version.create(9, 3))) {
      descriptor.processesFilesIndependently();
    }
  }

  protected abstract TreeParser<Tree> treeParser();

  protected abstract String repositoryKey();

  protected abstract List<TreeVisitor<InputFileContext>> visitors(SensorContext sensorContext, DurationStatistics statistics);

  protected abstract String getActivationSettingKey();

  @Override
  public void execute(SensorContext sensorContext) {
    if (!isActive(sensorContext)) {
      return;
    }

    if (isNotSonarLintContext(sensorContext)) {
      importExternalReports(sensorContext);
    }

    initContext(sensorContext);

    DurationStatistics statistics = new DurationStatistics(sensorContext.config());
    List<InputFile> inputFiles = inputFiles(sensorContext);
    List<String> filenames = inputFiles.stream().map(InputFile::toString).toList();

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
    afterExecute();
  }

  protected void initContext(SensorContext sensorContext) {
    // do nothing by default
  }

  protected List<InputFile> inputFiles(SensorContext sensorContext) {
    FileSystem fileSystem = sensorContext.fileSystem();
    FilePredicate predicate = mainFilePredicate(sensorContext);
    return StreamSupport.stream(fileSystem.inputFiles(predicate).spliterator(), false)
      .toList();
  }

  protected FilePredicate mainFilePredicate(SensorContext sensorContext) {
    FileSystem fileSystem = sensorContext.fileSystem();
    return fileSystem.predicates().and(
      fileSystem.predicates().hasLanguage(language.getKey()),
      fileSystem.predicates().hasType(InputFile.Type.MAIN));
  }

  protected void importExternalReports(SensorContext sensorContext) {
    // Default is to do nothing. A child-sensor that does require importing external reports should override this.
  }

  protected boolean isNotSonarLintContext(SensorContext sensorContext) {
    return sensorContext.runtime().getProduct() != SonarProduct.SONARLINT;
  }

  protected ParseException toParseException(String action, InputFileContext inputFileContext, Exception cause) {
    TextPointer position = null;
    if (cause instanceof RecognitionException recognitionException) {
      position = inputFileContext.newPointer(recognitionException.getLine(), 0);
    }
    return createGeneralParseException(action, inputFileContext.inputFile, cause, position);
  }

  private boolean isActive(SensorContext sensorContext) {
    return sensorContext.config().getBoolean(getActivationSettingKey()).orElse(false);
  }

  protected void afterExecute() {
    // do nothing by default
  }

  protected InputFileContext createInputFileContext(SensorContext sensorContext, InputFile inputFile) {
    return new InputFileContext(sensorContext, inputFile);
  }

  public static boolean isFailFast(SensorContext context) {
    return context.config().getBoolean(FAIL_FAST_PROPERTY_NAME).orElse(false);
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

    boolean analyseFiles(SensorContext sensorContext, List<InputFile> inputFiles, ProgressReport progressReport) {
      for (InputFile inputFile : inputFiles) {
        if (sensorContext.isCancelled()) {
          return false;
        }
        var inputFileContext = createInputFileContext(sensorContext, inputFile);
        try {
          analyseFile(inputFileContext);
        } catch (ParseException e) {
          logParsingError(e);
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
        throw toParseException("read", inputFileContext, e);
      }

      if (EMPTY_FILE_CONTENT_PATTERN.matcher(content).matches()) {
        return;
      }

      Tree tree = statistics.time("Parse", () -> {
        try {
          return parser.parse(content, inputFileContext);
        } catch (ParseException e) {
          throw e;
        } catch (RuntimeException e) {
          throw toParseException("parse", inputFileContext, e);
        }
      });

      for (TreeVisitor<InputFileContext> visitor : visitors) {
        try {
          String visitorId = visitor.getClass().getSimpleName();
          statistics.time(visitorId, () -> visitor.scan(inputFileContext, tree));
        } catch (RuntimeException e) {
          inputFileContext.reportAnalysisError(e.getMessage(), null);
          LOG.error("Cannot analyse '" + inputFile + "': " + e.getMessage(), e);

          interruptOnFailFast(inputFileContext.sensorContext, inputFile, e);
        }
      }
    }

    private void interruptOnFailFast(SensorContext context, InputFile inputFile, Exception e) {
      if (isFailFast(context)) {
        throw new IllegalStateException("Exception when analyzing '" + inputFile + "'", e);
      }
    }

    private void logParsingError(ParseException e) {
      LOG.error(e.getMessage());
      String detailedMessage = e.getDetails();
      if (detailedMessage != null) {
        LOG.debug(detailedMessage);
      }
      String stackTrace = getStackTrace(e);
      LOG.debug(stackTrace);
    }

    private String getStackTrace(ParseException e) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw, true);
      e.printStackTrace(pw);
      return sw.getBuffer().toString();
    }
  }
}
