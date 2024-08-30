/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.jvmframeworkconfig.checks.micronaut;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.jvmframeworkconfig.tree.api.Tuple;

import static org.sonar.iac.jvmframeworkconfig.tree.utils.JvmFrameworkConfigUtils.getStringValue;

@Rule(key = "S4830")
public class UnsecureConnectionCheck implements IacCheck {

  private static final String MESSAGE = "Trusting any certificate is security-sensitive.";
  private static final Predicate<String> SENSITIVE_KEY_PREDICATE = Pattern.compile("micronaut.http.services.[^.]++.ssl.insecure-trust-all-certificates").asMatchPredicate()
    .or("micronaut.http.client.ssl.insecure-trust-all-certificates"::equals);

  @Override
  public void initialize(InitContext init) {
    init.register(Tuple.class, UnsecureConnectionCheck::checkTuple);
  }

  private static void checkTuple(CheckContext ctx, Tuple tuple) {
    var key = tuple.key().value().value();
    if (SENSITIVE_KEY_PREDICATE.test(key)) {
      var valueString = getStringValue(tuple);
      if ("true".equalsIgnoreCase(valueString)) {
        ctx.reportIssue(tuple.value(), MESSAGE);
      }
    }
  }
}
