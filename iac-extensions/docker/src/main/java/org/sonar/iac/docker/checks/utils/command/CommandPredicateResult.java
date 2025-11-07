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
