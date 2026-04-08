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
package org.sonar.iac.terraform.checks;

import org.sonar.check.Rule;

@Rule(key = "S6327")
public class DisabledSNSTopicEncryptionCheck extends AbstractNewResourceCheck {

  @Override
  protected void registerResourceConsumer() {
    register("aws_sns_topic",
      resource -> resource.attribute("kms_master_key_id")
        .reportIfAbsent("Omitting \"kms_master_key_id\" disables SNS topics encryption. Make sure it is safe here."));
  }
}
