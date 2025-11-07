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

/**
 * Interface to define the contract of <a href="https://docs.docker.com/engine/reference/builder/#here-documents">Here-Documents</a> form.
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
