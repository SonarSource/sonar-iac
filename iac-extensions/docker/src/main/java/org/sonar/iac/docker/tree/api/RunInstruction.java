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

import java.util.List;

/**
 * Interface to define the contract of the <a href="https://docs.docker.com/engine/reference/builder/#run">RUN</> instruction.
 * <pre>
 *   RUN {@link #options()} {@link #arguments()}
 * </pre>
 */
public interface RunInstruction extends CommandInstruction {

  /**
   * @return the options as a list of {@link Flag}s.
   */
  List<Flag> options();
}
