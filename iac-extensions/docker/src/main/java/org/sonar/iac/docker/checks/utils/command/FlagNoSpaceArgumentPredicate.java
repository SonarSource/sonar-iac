/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.docker.checks.utils.command;

import java.util.List;
import java.util.function.Predicate;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.ExpandableStringLiteral;

/**
 * The predicate for the flag that it is glued to the argument, e.g.: {@code -pPASSWORD}
 * (there is no space between flag and argument).
 */
public class FlagNoSpaceArgumentPredicate implements Predicate<ArgumentResolution> {
  private final String flag;

  /**
   * Package private constructor, should be used via
   * {@link StandardCommandDetectors#flagNoSpaceArgument(String)} or
   * {@link StandardCommandDetectors#commandFlagNoSpace(String, String)} or
   * {@link StandardCommandDetectors#commandFlagNoSpace(List, String)}.
   */
  FlagNoSpaceArgumentPredicate(String flag) {
    this.flag = flag;
  }

  @Override
  public boolean test(ArgumentResolution resolution) {
    var resolvedValue = StringPredicate.stripQuotes(resolution.value());
    if (resolvedValue.startsWith(flag)) {
      if (resolvedValue.length() > flag.length()) {
        // for -p"PASSWORD" and -p'PASSWORD'
        return true;
      }
      if (resolution.argument().expressions().size() > 1) {
        // for -p"$PASSWORD", -p$PASSWORD etc
        return true;
      }
      if (resolution.argument().expressions().get(0).is(DockerTree.Kind.EXPANDABLE_STRING_LITERAL)) {
        ExpandableStringLiteral expression = (ExpandableStringLiteral) resolution.argument().expressions().get(0);
        // for "-p$PASSWORD"
        return expression.expressions().size() > 1;
      }
    }
    return false;
  }
}
