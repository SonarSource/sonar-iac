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
package org.sonar.iac.docker.checks.utils.command;

public class CommandPredicateResult {

  private final boolean match;
  private final PredicateContext.Status status;

  private final boolean detectCurrentPredicateAgain;

  private final boolean shouldBeMatchedAgain;

  public CommandPredicateResult(boolean match,
    PredicateContext.Status status,
    boolean detectCurrentPredicateAgain,
    boolean shouldBeMatchedAgain) {
    this.match = match;
    this.status = status;
    this.detectCurrentPredicateAgain = detectCurrentPredicateAgain;
    this.shouldBeMatchedAgain = shouldBeMatchedAgain;
  }

  public boolean isMatch() {
    return match;
  }

  public PredicateContext.Status getStatus() {
    return status;
  }

  public boolean isDetectCurrentPredicateAgain() {
    return detectCurrentPredicateAgain;
  }

  public boolean isShouldBeMatchedAgain() {
    return shouldBeMatchedAgain;
  }
}
