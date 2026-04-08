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
package org.sonar.iac.common.api.checks;

import java.util.List;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.languages.IacLanguage;

public interface CheckContext {

  /**
   * Returns the language of the file currently being checked, or {@link IacLanguage#UNKNOWN} if unknown.
   */
  default IacLanguage language() {
    return IacLanguage.UNKNOWN;
  }

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
