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

import java.util.List;
import org.sonar.iac.docker.symbols.ArgumentResolution;

/**
 * Main element used to represent most parameters provided to Docker instruction.
 * An Argument consist of a list of {@link Expression}, which can be a mix of {@link Literal}, {@link Variable}, {@link ExpandableStringCharacters}
 * and {@link ExpandableStringLiteral}.
 * To resolve an Argument, please refer to the class {@link ArgumentResolution}.
 * Examples :
 * {@code value}
 * {@code "value with spaces"}
 * {@code $var}
 * {@code ${var}}
 * {@code value$var"with quoted string"}
 */
public interface Argument extends DockerTree {
  List<Expression> expressions();
}
