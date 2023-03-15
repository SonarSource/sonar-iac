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
 * Common interface for the specifics form used by some instruction.
 * It is currently implemented by {@link HereDocument}, {@link ExecForm} and {@link ShellForm}.
 * They all have in common that they provide a List of Argument, they just follow different format.
 * Examples :
 * {@code arg1 arg2 arg3}
 * {@code ["arg1", "arg2", "arg3"]}
 * {@code <<INPUT arg1\narg2\nINPUT\n }
 */
public interface ArgumentList extends HasArguments, DockerTree {
}
