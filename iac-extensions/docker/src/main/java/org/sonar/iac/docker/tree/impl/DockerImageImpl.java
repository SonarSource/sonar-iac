/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.docker.tree.impl;

import java.util.ArrayList;
import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.DockerImage;
import org.sonar.iac.docker.tree.api.FromInstruction;
import org.sonar.iac.docker.tree.api.Instruction;

/**
 * Represent a Docker image and it's related instructions.
 * A docker image is constituted first of a FROM instruction.
 * Every following instructions until the next FROM instruction are associated to this image.
 * A Dockerfile can contain zero (empty file) to any amount of images.
 * Example of a Dockerfile with two DockerImage defined in it (one instruction for each) :
 *   FROM ubuntu:latest
 *   MAINTAINER bob
 *   FROM ubuntu:14.04
 *   EXPOSE 80/tcp
 */
public class DockerImageImpl extends AbstractDockerTreeImpl implements DockerImage {

  private final FromInstruction from;
  private final List<Instruction> instructions;

  public DockerImageImpl(FromInstruction from, List<Instruction> instructions) {
    this.from = from;
    this.instructions = instructions;
  }

  @Override
  public FromInstruction from() {
    return from;
  }

  @Override
  public List<Instruction> instructions() {
    return instructions;
  }

  @Override
  public List<Tree> children() {
    List<Tree> result = new ArrayList<>();
    result.add(from);
    result.addAll(instructions);
    return result;
  }

  @Override
  public Kind getKind() {
    return Kind.DOCKERIMAGE;
  }
}
