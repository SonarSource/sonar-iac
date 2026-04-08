/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import java.util.List;

/**
 * Interface to define the contract of the DockerImage, with its {@link FromInstruction} and the list of {@link Instruction}.
 * <pre>
 *   {@link #from()}
 *   {@link #from()} {@link #instructions()}
 * </pre>
 */
public interface DockerImage extends DockerTree, HasScope {

  FromInstruction from();

  List<Instruction> instructions();
}
