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
package org.sonar.iac.docker.checks.utils;

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
  private final boolean ignoreQuotes;

  public FlagNoSpaceArgumentPredicate(String flag, boolean ignoreQuotes) {
    this.flag = flag;
    this.ignoreQuotes = ignoreQuotes;
  }

  @Override
  public boolean test(ArgumentResolution resolution) {
    String resolvedValue = ignoreQuotes ? StringPredicate.stripQuotes(resolution.value()) : resolution.value();
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
