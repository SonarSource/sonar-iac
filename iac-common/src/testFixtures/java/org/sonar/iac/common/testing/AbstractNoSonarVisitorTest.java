/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import com.sonarsource.scanner.engine.sensor.test.fixtures.SensorContextTester;
import com.sonarsource.scanner.engine.sensor.test.fixtures.TestInputFileBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.NoSonarVisitor;
import org.sonar.iac.common.languages.IacLanguage;

import static org.mockito.Mockito.mock;

public abstract class AbstractNoSonarVisitorTest {

  protected NoSonarFilter noSonarFilter = mock(NoSonarFilter.class);
  protected TreeParser<? extends Tree> parser;
  protected String language;
  protected NoSonarVisitor visitor;
  protected SensorContextTester sensorContext;
  protected InputFile inputFile;

  @TempDir
  public File tempFolder;

  @BeforeEach
  void setUp() {
    sensorContext = SensorContextTester.create(tempFolder);
    parser = treeParser();
    visitor = noSonarVisitor(noSonarFilter);
    language = languageKey();
  }

  protected abstract String languageKey();

  protected abstract TreeParser<? extends Tree> treeParser();

  protected abstract NoSonarVisitor noSonarVisitor(NoSonarFilter noSonarFilter);

  protected NoSonarVisitor scan(String code) {
    return scan(code, "file");
  }

  protected NoSonarVisitor scan(String code, String filename) {
    return scan(new TestInputFileBuilder("moduleKey", new File(tempFolder, filename).getName())
      .setCharset(StandardCharsets.UTF_8)
      .setLanguage(language)
      .setContents(code)
      .build());
  }

  protected NoSonarVisitor scan(InputFile inputFile) {
    this.inputFile = inputFile;
    var inputFileContext = new InputFileContext(sensorContext, inputFile, IacLanguage.UNKNOWN);
    try {
      visitor.scan(inputFileContext, parser.parse(inputFile.contents(), inputFileContext));
    } catch (IOException e) {
      throw new RuntimeException("Failed to retrieve content of inputFile: " + inputFile.filename(), e);
    }
    return visitor;
  }
}
