/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
package org.sonar.iac.common.yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.exceptions.MarkedYamlEngineException;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.resources.Language;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.IacSensor;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.ChecksVisitor;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.yaml.visitors.YamlHighlightingVisitor;
import org.sonar.iac.common.yaml.visitors.YamlMetricsVisitor;

public abstract class YamlSensor extends IacSensor {

  protected static final String JSON_LANGUAGE_KEY = "json";
  protected static final String YAML_LANGUAGE_KEY = "yaml";
  protected static final String FILE_SEPERATOR = "---";

  protected final Checks<IacCheck> checks;

  protected YamlSensor(SonarRuntime sonarRuntime, FileLinesContextFactory fileLinesContextFactory, CheckFactory checkFactory,
    NoSonarFilter noSonarFilter, Language language, List<Class<?>> checks) {
    super(sonarRuntime, fileLinesContextFactory, noSonarFilter, language);
    this.checks = checkFactory.create(repositoryKey());
    this.checks.addAnnotatedChecks(checks);
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .onlyOnLanguages(JSON_LANGUAGE_KEY, YAML_LANGUAGE_KEY)
      .name("IaC " + language.getName() + " Sensor");
  }

  @Override
  protected TreeParser<Tree> treeParser() {
    return new YamlParser();
  }

  @Override
  protected List<TreeVisitor<InputFileContext>> visitors(SensorContext sensorContext, DurationStatistics statistics) {
    List<TreeVisitor<InputFileContext>> visitors = new ArrayList<>();
    visitors.add(new YamlHighlightingVisitor());
    visitors.add(new YamlMetricsVisitor(fileLinesContextFactory, noSonarFilter));
    visitors.add(new ChecksVisitor(checks, statistics));
    return visitors;
  }

  @Override
  protected ParseException toParseException(String action, InputFile inputFile, Exception cause) {
    if (cause instanceof MarkedYamlEngineException) {
      Optional<Mark> problemMark = ((MarkedYamlEngineException) cause).getProblemMark();
      TextPointer position = null;
      if (problemMark.isPresent()) {
        position = inputFile.newPointer(problemMark.get().getLine() + 1, 0);
      }
      return ParseException.throwParseException(action, inputFile, cause, position);
    }
    return super.toParseException(action, inputFile, cause);
  }

  @Override
  protected FilePredicate mainFilePredicate(SensorContext sensorContext) {
    FileSystem fileSystem = sensorContext.fileSystem();
    return fileSystem.predicates().and(fileSystem.predicates().and(
      fileSystem.predicates().or(fileSystem.predicates().hasLanguage(JSON_LANGUAGE_KEY), fileSystem.predicates().hasLanguage(YAML_LANGUAGE_KEY)),
      fileSystem.predicates().hasType(InputFile.Type.MAIN)),
      customFilePredicate(sensorContext));
  }

  protected abstract FilePredicate customFilePredicate(SensorContext sensorContext);

}
