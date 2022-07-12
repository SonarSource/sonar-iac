/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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

import java.util.Optional;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.exceptions.MarkedYamlEngineException;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.resources.Language;
import org.sonar.iac.common.extension.IacSensor;
import org.sonar.iac.common.extension.ParseException;

public abstract class YamlIacSensor extends IacSensor {

  protected static final String JSON_LANGUAGE_KEY = "json";
  protected static final String YAML_LANGUAGE_KEY = "yaml";

  protected YamlIacSensor(SonarRuntime sonarRuntime, FileLinesContextFactory fileLinesContextFactory, NoSonarFilter noSonarFilter, Language language) {
    super(sonarRuntime, fileLinesContextFactory, noSonarFilter, language);
  }
  @Override
  protected ParseException toParseException(String action, InputFile inputFile, Exception cause) {
    if (!(cause instanceof MarkedYamlEngineException)) {
      return super.toParseException(action, inputFile, cause);
    }

    Optional<Mark> problemMark = ((MarkedYamlEngineException) cause).getProblemMark();
    TextPointer position = null;
    if (problemMark.isPresent()) {
      position = inputFile.newPointer(problemMark.get().getLine() + 1, 0);
    }
    return new ParseException("Cannot " + action + " '" + inputFile + "': " + cause.getMessage(), position);
  }
}
