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

import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.config.Configuration;
import org.sonar.iac.common.extension.FileIdentificationPredicate;

public class CloudFormationFilePredicate extends AbstractTimedFilePredicate implements YamlFileTypePredicate {
  public static final String CLOUDFORMATION_FILE_IDENTIFIER_KEY = "sonar.cloudformation.file.identifier";
  public static final String CLOUDFORMATION_FILE_IDENTIFIER_DEFAULT_VALUE = "AWSTemplateFormatVersion";
  private final FilePredicate delegate;

  public CloudFormationFilePredicate(Configuration config, boolean shouldLogPredicateFailure) {
    // Fall back to the default identifier when the property is absent (e.g. tests or programmatic setups where the
    // PropertyDefinition default is not applied), so the predicate does not degrade to matching every file. An
    // explicitly cleared identifier is returned by Configuration.get as a present empty string, so it still bypasses
    // this fallback and keeps the documented "no identifier means analyze all files" behavior.
    this.delegate = new FileIdentificationPredicate(
      config.get(CLOUDFORMATION_FILE_IDENTIFIER_KEY).orElse(CLOUDFORMATION_FILE_IDENTIFIER_DEFAULT_VALUE),
      shouldLogPredicateFailure);
  }

  @Override
  protected boolean accept(InputFile inputFile) {
    return delegate.apply(inputFile);
  }

  @Override
  public FileType fileType() {
    return FileType.CLOUDFORMATION;
  }
}
