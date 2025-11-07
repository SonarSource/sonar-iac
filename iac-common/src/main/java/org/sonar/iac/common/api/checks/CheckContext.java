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
package org.sonar.iac.common.api.checks;

import java.util.List;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.impl.TextRange;

public interface CheckContext {

  /**
   * Report an issue with a primary location.
   *
   * @param textRange the primary location of the issue
   * @param message the message to report
   */
  void reportIssue(TextRange textRange, String message);

  /**
   * Report an issue with a primary location and secondary locations.
   *
   * @param textRange the primary location of the issue
   * @param message the message to report
   * @param secondaryLocations the list of secondary locations
   */
  void reportIssue(TextRange textRange, String message, List<SecondaryLocation> secondaryLocations);

  /**
   * Report an issue with a component as primary location.
   *
   * @param toHighlight the component to highlight
   * @param message the message to report
   */
  void reportIssue(HasTextRange toHighlight, String message);

  /**
   * Report an issue with a component as primary location and a secondary location.
   *
   * @param toHighlight the component to highlight
   * @param message the message to report
   * @param secondaryLocation the secondary location
   */
  void reportIssue(HasTextRange toHighlight, String message, SecondaryLocation secondaryLocation);

  /**
   * Report an issue with a component as primary location and a secondary locations.
   *
   * @param toHighlight the component to highlight
   * @param message the message to report
   * @param secondaryLocations the list of secondary locations
   */
  void reportIssue(HasTextRange toHighlight, String message, List<SecondaryLocation> secondaryLocations);

  /**
   * Report an issue with a components as primary location.
   *
   * @param toHighlight the list of components to highlight
   * @param message the message to report
   */
  <T extends HasTextRange> void reportIssue(List<T> toHighlight, String message);

}
