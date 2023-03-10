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
 * This interface define the contract of <a href="https://docs.docker.com/engine/reference/builder/#here-documents">Here-Documents</a> form.
 * It is a way to structure and provide {@link Argument} to compatible instruction.
 * It extends from {@code ArgumentList}, it is a common interface from which extends any form that provide a list of argument, they are interchangeable.
 * This form is the most complex, as it expect keys with <<-? as prefix and can last over several lines, until we find a line with that key only.
 * Examples :
 * <pre>
 *   <<KEY val1
 *   val2
 *   KEY
 * </pre>
 * <pre>
 *   <<KEY1 val1 KEY2 val2
 *   val3
 *   KEY1
 *   val4
 *   KEY2
 * <pre/>
 */
public interface HereDocument extends ArgumentList {
}
