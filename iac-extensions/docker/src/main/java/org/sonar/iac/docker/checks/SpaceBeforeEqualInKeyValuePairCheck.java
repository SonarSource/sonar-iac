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
package org.sonar.iac.docker.checks;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.EnvInstruction;
import org.sonar.iac.docker.tree.api.KeyValuePair;
import org.sonar.iac.docker.tree.api.LabelInstruction;
import org.sonar.iac.docker.tree.api.Literal;

@Rule(key = "S6574")
public class SpaceBeforeEqualInKeyValuePairCheck implements IacCheck {

  private static final String MESSAGE = "Remove space before the equal sign in the key-value pair, as it can lead to unexpected behavior.";

  @Override
  public void initialize(InitContext init) {
    init.register(LabelInstruction.class, (ctx, label) -> checkKeyValurPairs(ctx, label.labels()));
    init.register(EnvInstruction.class, (ctx, env) -> checkKeyValurPairs(ctx, env.environmentVariables()));
  }

  private static void checkKeyValurPairs(CheckContext ctx, List<KeyValuePair> keyValuePairs) {
    if (isSensitiveKeyValuePair(keyValuePairs)) {
      ctx.reportIssue(keyValuePairs.get(0), MESSAGE);
    }
  }

  private static boolean isSensitiveKeyValuePair(List<KeyValuePair> keyValuePairs) {
    return keyValuePairs.size() == 1 && keyValuePairs.get(0).equalSign() == null && isStartingWithLiteralStringWithEqual(keyValuePairs.get(0).value());
  }

  private static boolean isStartingWithLiteralStringWithEqual(@Nullable Argument argument) {
    return Optional.ofNullable(argument).stream()
      .map(arg -> arg.expressions().get(0))
      .filter(expr -> expr.is(DockerTree.Kind.STRING_LITERAL))
      .map(Literal.class::cast)
      .anyMatch(literal -> literal.originalValue().startsWith("="));
  }
}
