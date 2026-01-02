/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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

/**
 * Common interface for the specifics form used by some instruction.
 * It is currently implemented by {@link HereDocument}, {@link ExecForm} and {@link ShellForm}.
 * They all have in common that they provide a List of Argument, they just follow different format.
 * Examples :
 * <li/> {@code arg1 arg2 arg3}
 * <li/> {@code ["arg1", "arg2", "arg3"]}
 * <li/> {@code <<INPUT arg1\narg2\nINPUT\n }
 */
public interface ArgumentList extends HasArguments, DockerTree {
}
