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
