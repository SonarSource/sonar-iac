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
package org.sonar.iac.common.predicates;

import java.util.Arrays;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.config.Configuration;
import org.sonar.iac.common.extension.FileIdentificationPredicate;

import static org.sonar.iac.common.yaml.AbstractYamlLanguageSensor.JSON_LANGUAGE_KEY;

public class ArmJsonFilePredicate extends AbstractTimedFilePredicate implements YamlFileTypePredicate {
  public static final String ARM_JSON_FILE_IDENTIFIER_KEY = "sonar.azureresourcemanager.file.identifier";
  public static final String ARM_JSON_FILE_IDENTIFIER_DEFAULT_VALUE = "https://schema.management.azure.com/schemas/,http://schema.management.azure.com/schemas/";
  private final FilePredicate delegate;

  public ArmJsonFilePredicate(FilePredicates predicates, Configuration config, boolean enablePredicateDebugLogs) {
    var identifiers = Arrays.stream(config.getStringArray(ARM_JSON_FILE_IDENTIFIER_KEY))
      .filter(s -> !s.isBlank()).toList();
    // Azure Resource Manager templates are JSON only (Bicep is matched by its own language by the sensor), so the
    // language is checked here to keep this predicate inert for the other (YAML) file types handled by the resolver.
    this.delegate = predicates.and(
      predicates.hasLanguage(JSON_LANGUAGE_KEY),
      new FileIdentificationPredicate(identifiers, enablePredicateDebugLogs));
  }

  @Override
  protected boolean accept(InputFile inputFile) {
    return delegate.apply(inputFile);
  }

  @Override
  public FileType fileType() {
    return FileType.AZURE_RESOURCE_MANAGER;
  }
}
