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

import java.util.List;

/**
 * Represents the content of a Dockerfile.
 * A dockerfile currently contain a list of {@link ArgInstruction} followed by a list of {@link DockerImage}.
 * The ArgInstruction are considered to be global.
 * <pre>
 *   {@link #dockerImages()}
 *   {@link #globalArgs()} {@link #dockerImages()}
 * <pre/>
 */
public interface Body extends DockerTree, HasScope {
  List<ArgInstruction> globalArgs();

  List<DockerImage> dockerImages();
}
