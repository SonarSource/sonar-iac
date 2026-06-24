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
package org.sonar.iac.terraform.checks.gcp;

import org.sonar.check.Rule;
import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;

import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isFalse;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isTrue;

@Rule(key = "S8857")
public class UniformBucketLevelAccessCheck extends AbstractNewResourceCheck {

  private static final String MESSAGE = "Make sure enabling object ACLs without enforcing uniform bucket-level access is safe here.";
  private static final String OMITTING_MESSAGE = "Omitting \"uniform_bucket_level_access\" allows object ACLs to bypass IAM. Make sure it is safe here.";

  @Override
  protected void registerResourceConsumer() {
    register("google_storage_bucket",
      resource -> {
        if (resource.attribute("bucket_policy_only").is(isTrue())) {
          return;
        }
        resource.attribute("uniform_bucket_level_access")
          .reportIf(isFalse(), MESSAGE)
          .reportIfAbsent(OMITTING_MESSAGE);
      });
  }
}
