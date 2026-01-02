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
package org.sonar.iac.common.predicates;

import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.extension.AbstractTimedFilePredicate;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.FileIdentificationPredicate;

public class CloudFormationFilePredicate extends AbstractTimedFilePredicate {
  public static final String CLOUDFORMATION_FILE_IDENTIFIER_KEY = "sonar.cloudformation.file.identifier";
  public static final String CLOUDFORMATION_FILE_IDENTIFIER_DEFAULT_VALUE = "AWSTemplateFormatVersion";
  private final FilePredicate delegate;

  public CloudFormationFilePredicate(SensorContext sensorContext, boolean shouldLogPredicateFailure, DurationStatistics.Timer timer) {
    super(timer);
    this.delegate = new FileIdentificationPredicate(sensorContext.config().get(CLOUDFORMATION_FILE_IDENTIFIER_KEY).orElse(""),
      shouldLogPredicateFailure);
  }

  @Override
  protected boolean accept(InputFile inputFile) {
    return delegate.apply(inputFile);
  }
}
