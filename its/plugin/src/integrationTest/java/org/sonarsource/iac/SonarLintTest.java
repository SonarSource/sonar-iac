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
package org.sonarsource.iac;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonarsource.sonarlint.core.StandaloneSonarLintEngineImpl;
import org.sonarsource.sonarlint.core.analysis.api.AnalysisResults;
import org.sonarsource.sonarlint.core.analysis.api.ClientInputFile;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneAnalysisConfiguration;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneGlobalConfiguration;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneSonarLintEngine;
import org.sonarsource.sonarlint.core.commons.Language;

import static org.assertj.core.api.Assertions.assertThat;

public class SonarLintTest {

  public static final Path BASE_DIR = Paths.get("projects/sonarlint");

  @TempDir
  static Path sonarLintUserHome;

  @TempDir
  static File tmp;

  private static StandaloneSonarLintEngine sonarlintEngine;
  public static final Language[] ENABLED_LANGUAGES = {Language.TERRAFORM, Language.CLOUDFORMATION, Language.KUBERNETES, Language.DOCKER};

  @BeforeAll
  public static void prepare() {
    StandaloneGlobalConfiguration config = StandaloneGlobalConfiguration.builder()
      .addPlugin(TestBase.IAC_PLUGIN_LOCATION.getFile().toPath())
      .addEnabledLanguages(ENABLED_LANGUAGES)
      .setSonarLintUserHome(sonarLintUserHome)
      .setLogOutput((formattedMessage, level) -> {
        /* Don't pollute logs */ })
      .build();
    sonarlintEngine = new StandaloneSonarLintEngineImpl(config);
  }

  @ParameterizedTest
  @MethodSource
  void parseErrorShouldReportAnalysisError(Path inputFile) {
    ClientInputFile clientInputFile = createInputFile(inputFile, false);
    final List<Issue> issues = new ArrayList<>();
    StandaloneAnalysisConfiguration standaloneAnalysisConfiguration = StandaloneAnalysisConfiguration.builder()
      .setBaseDir(tmp.toPath())
      .addInputFile(clientInputFile)
      .build();
    AnalysisResults analysisResults = sonarlintEngine.analyze(standaloneAnalysisConfiguration, issues::add, null, null);
    assertThat(issues).isEmpty();
    assertThat(analysisResults.failedAnalysisFiles()).hasSize(1);
  }

  static List<Path> parseErrorShouldReportAnalysisError() {
    return provideTestFiles("error");
  }

  @ParameterizedTest
  @MethodSource
  void shouldRaiseIssue(Path inputFile) {
    ClientInputFile clientInputFile = createInputFile(inputFile, false);
    final List<Issue> issues = new ArrayList<>();
    StandaloneAnalysisConfiguration standaloneAnalysisConfiguration = StandaloneAnalysisConfiguration.builder()
      .setBaseDir(tmp.toPath())
      .addInputFile(clientInputFile)
      .build();
    sonarlintEngine.analyze(standaloneAnalysisConfiguration, issues::add, null, null);
    assertThat(issues).hasSize(1);
  }

  static List<Path> shouldRaiseIssue() {
    return provideTestFiles("issue");
  }

  private static List<Path> provideTestFiles(String testFileDir) {
    List<Path> testFiles = new ArrayList<>();

    for (Language language : ENABLED_LANGUAGES) {
      try (Stream<Path> pathStream = Files.list(BASE_DIR.resolve(language.getLanguageKey() + "/" + testFileDir))) {
        pathStream
          .map(Path::getFileName)
          .map(fileName -> BASE_DIR.resolve(language.getLanguageKey() + "/" + testFileDir + "/" + fileName))
          .forEach(testFiles::add);
      } catch (IOException e) {
        throw new AssertionError("Can not load test files from " + testFileDir, e);
      }
    }

    if (testFiles.isEmpty()) {
      throw new AssertionError("There are no test files provided");
    }

    return testFiles;
  }

  private ClientInputFile createInputFile(final Path path, final boolean isTest) {
    return new ClientInputFile() {

      @Override
      public String getPath() {
        return path.toString();
      }

      @Override
      public String relativePath() {
        return path.toString();
      }

      @Override
      public URI uri() {
        return path.toUri();
      }

      @Override
      public boolean isTest() {
        return isTest;
      }

      @Override
      public Charset getCharset() {
        return StandardCharsets.UTF_8;
      }

      @Override
      public <G> G getClientObject() {
        return null;
      }

      @Override
      public InputStream inputStream() throws IOException {
        return new FileInputStream(path.toFile());
      }

      @Override
      public String contents() throws IOException {
        return FileUtils.readFileToString(path.toFile(), getCharset());
      }
    };
  }
}
