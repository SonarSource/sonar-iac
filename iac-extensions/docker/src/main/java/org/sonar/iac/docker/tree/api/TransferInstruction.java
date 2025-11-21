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
 * Interface to define the contract for transfer instruction, such as {@link AddInstruction} or {@link CopyInstruction}.
 * <pre>
 *   {@link #keyword()} {@link #options()} {@link #srcs()} {@link #dest()}
 * </pre>
 */
public interface TransferInstruction extends Instruction {

  /**
   * @return The list of {@link Flag} for this transfer instruction.
   */
  List<Flag> options();

  ArgumentList srcsAndDest();

  /**
   * @return The list of sources of type {@link Argument} to transfer.
   */
  List<Argument> srcs();

  /**
   * @return The destination of type {@link Argument} to transfer to.
   */
  Argument dest();
}
