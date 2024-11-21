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

import java.util.List;
import org.sonar.check.Rule;

import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isFalse;

@Rule(key = "S6275")
public class UnencryptedEbsVolumeCheck extends AbstractNewResourceCheck {

  private static final List<String> PROPERTIES = List.of("root_block_device", "ebs_block_device");
  private static final String MESSAGE = "Make sure that using unencrypted volumes is safe here.";
  private static final String OMITTING_MESSAGE = "Omitting \"%s\" disables volumes encryption. Make sure it is safe here.";

  @Override
  protected void registerResourceConsumer() {
    register("aws_ebs_encryption_by_default",
      resource -> resource.attribute("enabled")
        .reportIf(isFalse(), MESSAGE));

    register("aws_ebs_volume",
      resource -> resource.attribute("encrypted")
        .reportIfAbsent(OMITTING_MESSAGE)
        .reportIf(isFalse(), MESSAGE));

    register("aws_launch_configuration",
      resource -> PROPERTIES.forEach(property -> resource.block(property)
        .attribute("encrypted")
        .reportIfAbsent(OMITTING_MESSAGE)
        .reportIf(isFalse(), MESSAGE)));
  }

}
