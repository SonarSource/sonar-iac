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
package org.sonar.iac.docker.utils;

import java.util.List;
import java.util.Optional;
import org.sonar.iac.docker.tree.api.Flag;

/**
 * Class to define global methods than can be used to help writing checks.
 * Any generalized method that can be used in multiple checks should be put there.
 */
public class CheckUtils {

  private CheckUtils() {
  }

  public static Optional<Flag> getParamByName(List<Flag> params, String name) {
    return params.stream().filter(param -> name.equals(param.name())).findFirst();
  }
}
