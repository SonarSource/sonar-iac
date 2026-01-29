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
package org.sonar.iac.common.reports;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONArray;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.ParseException;

/**
 * Abstract importer for external linter reports that use a JSON array as the root element.
 * Each element in the array represents an issue.
 * <p>
 * For reports using SARIF format (JSON object with runs/results structure),
 * use {@link AbstractSarifReportImporter} instead.
 */
public abstract class AbstractJsonArrayReportImporter extends AbstractReportImporter {

  protected AbstractJsonArrayReportImporter(SensorContext context,
    AbstractExternalRulesDefinition externalRulesDefinition,
    AnalysisWarningsWrapper analysisWarnings,
    String warningPrefix) {
    super(context, externalRulesDefinition, analysisWarnings, warningPrefix);
  }

  @Override
  protected String getExpectedFileFormat() {
    return "JSON array";
  }

  @Override
  protected JSONArray extractIssues(File reportFile) throws IOException, ParseException {
    try (var reader = Files.newBufferedReader(reportFile.toPath())) {
      return (JSONArray) jsonParser.parse(reader);
    }
  }
}
