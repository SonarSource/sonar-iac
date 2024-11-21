/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
