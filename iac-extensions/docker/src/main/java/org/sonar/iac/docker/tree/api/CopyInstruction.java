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
package org.sonar.iac.docker.tree.api;

/**
 * Interface to define the contract of the <a href="https://docs.docker.com/engine/reference/builder/#copy">COPY</> instruction.
 * It is a combination of the {@link TransferInstruction} and {@link CommandInstruction} interfaces, offering two ways to retrieve the arguments.
 * <pre>
 *   COPY {@link #options()} {@link #srcs()} {@link #dest()}
 *   COPY {@link #srcs()} {@link #dest()}
 *   COPY {@link #options()} {@link #arguments()}
 *   COPY {@link #arguments()}
 * </pre>
 */
public interface CopyInstruction extends TransferInstruction, CommandInstruction {
}
