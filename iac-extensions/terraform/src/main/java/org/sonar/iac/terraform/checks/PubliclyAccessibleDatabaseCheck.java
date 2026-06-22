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

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.iac.terraform.checks.utils.ExpressionPredicate;

@Rule(key = "S8793")
public class PubliclyAccessibleDatabaseCheck extends AbstractNewResourceCheck {

  private static final String MESSAGE = "Make sure allowing public network access is safe here.";

  private static final List<String> DATABASE_RESOURCE_TYPES = List.of(
    "aws_db_instance",
    "aws_rds_cluster_instance",
    "aws_redshift_cluster");

  @Override
  protected void registerResourceConsumer() {
    register(DATABASE_RESOURCE_TYPES,
      resource -> resource.attribute("publicly_accessible")
        .reportIf(ExpressionPredicate.isTrue(), MESSAGE));
  }
}
