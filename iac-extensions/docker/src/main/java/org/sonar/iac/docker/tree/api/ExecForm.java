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
package org.sonar.iac.docker.tree.api;

import org.sonar.iac.common.api.tree.SeparatedList;

/**
 * Interface to define the contract of ExecForm.
 * It is a way to structure and provide {@link Argument} to compatible instruction.
 * It extends from {@code ArgumentList}, it is a common interface from which extends any form that provide a list of argument, they are interchangeable.
 * This form use brackets, each argument much be separated by comma and be surrounded by double-quotes.
 * Examples :
 * {@code ["val"]}
 * {@code ["val1", "val2"]}
 */
public interface ExecForm extends ArgumentList {
  SyntaxToken leftBracket();

  SeparatedList<Argument, SyntaxToken> argumentsWithSeparators();

  SyntaxToken rightBracket();
}
