/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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

@Rule(key = "S6319")
public class UnencryptedSageMakerNotebookCheck extends AbstractNewResourceCheck {

  @Override
  protected void registerResourceConsumer() {
    register("aws_sagemaker_notebook_instance",
      resource -> resource.attribute("kms_key_id")
        .reportIfAbsent("Omitting \"kms_key_id\" disable encryption of SageMaker notebook instances. Make sure it is safe here."));
  }
}
