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

/**
 * Interface to define the contract of the <a href="https://docs.docker.com/engine/reference/builder/#entrypoint">ENTRYPOINT</> instruction.
 * It is a strict implementation of the {@link CodeInstruction} interface.
 * <pre>
 *   ENTRYPOINT {@link #code()}
 * </pre>
 */
public interface EntrypointInstruction extends CodeInstruction {
}
