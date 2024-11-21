/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.terraform.checks;

import org.sonar.check.Rule;
import org.sonar.iac.terraform.checks.utils.ExpressionPredicate;

@Rule(key = "S6332")
public class DisabledEFSEncryptionCheck extends AbstractNewResourceCheck {

  private static final String MESSAGE = "Make sure that using unencrypted EFS file systems is safe here.";
  private static final String SECONDARY_MESSAGE = "Related file system";
  private static final String OMITTING_MESSAGE = "Omitting \"encrypted\" disables EFS file systems encryption. Make sure it is safe here.";

  @Override
  protected void registerResourceConsumer() {
    register("aws_efs_file_system",
      resource -> resource.attribute("encrypted")
        .reportIfAbsent(OMITTING_MESSAGE)
        .reportIf(ExpressionPredicate.isFalse(), MESSAGE, resource.toSecondary(SECONDARY_MESSAGE)));
  }
}
