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
package org.sonar.iac.springconfig.checks;

import java.util.Optional;
import java.util.Set;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.springconfig.tree.api.Scalar;
import org.sonar.iac.springconfig.tree.api.SyntaxToken;
import org.sonar.iac.springconfig.tree.api.Tuple;

public abstract class AbstractSensitiveKeyCheck implements IacCheck {

  @Override
  public void initialize(InitContext init) {
    init.register(Tuple.class, this::checkTuple);
  }

  protected abstract Set<String> sensitiveKeys();

  protected abstract void reportOnSensitiveValue(CheckContext ctx, Tuple tuple, String value);

  private void checkTuple(CheckContext ctx, Tuple tuple) {
    var key = tuple.key().value().value();
    if (sensitiveKeys().contains(key)) {
      var valueString = Optional.ofNullable(tuple.value())
        .map(Scalar::value)
        .map(SyntaxToken::value)
        .orElse(null);

      if (valueString == null) {
        return;
      }

      reportOnSensitiveValue(ctx, tuple, valueString);
    }
  }
}
