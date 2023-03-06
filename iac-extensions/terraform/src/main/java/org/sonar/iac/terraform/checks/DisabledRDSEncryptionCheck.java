/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.terraform.checks;

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.iac.terraform.symbols.AttributeSymbol;

import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.equalTo;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isFalse;


@Rule(key = "S6303")
public class DisabledRDSEncryptionCheck extends AbstractNewResourceCheck {

  private static final String MESSAGE = "Make sure that using unencrypted RDS DB Instances is safe here.";
  private static final String OMITTING_MESSAGE = "Omitting \"storage_encrypted\" disables databases encryption. Make sure it is safe here.";
  private static final String SECONDARY_MESSAGE = "Related RDS DBInstance";
  private static final List<String> EXCLUDE_AURORA_ATTRIBUTE = List.of("aurora", "aurora-mysql", "aurora-postgresql");

  @Override
  protected void registerResourceConsumer() {
    register("aws_db_instance",
      resource -> {
        AttributeSymbol engine = resource.attribute("engine");
        if (EXCLUDE_AURORA_ATTRIBUTE.stream().anyMatch(auroraAttribute -> engine.is(equalTo(auroraAttribute)))) {
          return;
        }
        resource.attribute("storage_encrypted")
          .reportIf(isFalse(), MESSAGE, resource.toSecondary(SECONDARY_MESSAGE))
          .reportIfAbsent(OMITTING_MESSAGE);
      });
  }
}
