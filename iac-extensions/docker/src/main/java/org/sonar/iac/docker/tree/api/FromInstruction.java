/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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

import javax.annotation.CheckForNull;

/**
 * Interface to define the contract of the <a href="https://docs.docker.com/engine/reference/builder/#from">FROM</> instruction.
 * <pre>
 *   FROM {@link #image()}
 *   FROM {@link #platform()} {@link #image()}
 *   FROM {@link #image()} {@link #alias()}
 *   FROM {@link #platform()} {@link #image()} {@link #alias()}
 * </pre>
 */
public interface FromInstruction extends Instruction {

  @CheckForNull
  Flag platform();

  Argument image();

  @CheckForNull
  Alias alias();
}
