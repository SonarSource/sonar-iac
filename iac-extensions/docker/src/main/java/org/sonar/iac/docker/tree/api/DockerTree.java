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
package org.sonar.iac.docker.tree.api;

import org.sonar.iac.common.api.tree.Tree;
import org.sonar.sslr.grammar.GrammarRuleKey;

public interface DockerTree extends Tree {

  boolean is(Kind... kind);
  Kind getKind();
  DockerTree parent();
  void setParent(DockerTree parent);

  enum Kind implements GrammarRuleKey {
    FILE(File.class),
    BODY(Body.class),
    DOCKERIMAGE(DockerImage.class),
    INSTRUCTION(Instruction.class),
    ONBUILD(OnBuildInstruction.class),
    FROM(FromInstruction.class),
    IMAGE(Image.class),
    PARAM(Flag.class),
    ALIAS(Alias.class),
    MAINTAINER(MaintainerInstruction.class),
    STOPSIGNAL(StopSignalInstruction.class),
    WORKDIR(WorkdirInstruction.class),
    EXPOSE(ExposeInstruction.class),
    LABEL(LabelInstruction.class),
    ENV(EnvInstruction.class),
    KEY_VALUE_PAIR(KeyValuePair.class),
    ARG(ArgInstruction.class),
    CMD(CmdInstruction.class),
    ENTRYPOINT(EntrypointInstruction.class),
    RUN(RunInstruction.class),
    SHELL_FORM(ShellForm.class),
    EXEC_FORM(ExecForm.class),
    ADD(AddInstruction.class),
    COPY(CopyInstruction.class),
    VOLUME(VolumeInstruction.class),
    USER(UserInstruction.class),
    SHELL(ShellInstruction.class),
    HEALTHCHECK(HealthCheckInstruction.class),
    NONE(NoneInstruction.class),
    HEREDOCUMENT(HereDocument.class),

    TOKEN(SyntaxToken.class),

    STRING_LITERAL(Literal.class),

    EXPANDABLE_STRING_LITERAL(ExpandableStringLiteral.class),
    EXPANDABLE_STRING_CHARACTERS(ExpandableStringCharacters.class),
    REGULAR_VARIABLE(RegularVariable.class),
    ENCAPSULATED_VARIABLE(EncapsulatedVariable.class),
    ARGUMENT(Argument.class);


    private final Class<? extends DockerTree> associatedInterface;

    Kind(Class<? extends DockerTree> associatedInterface) {
      this.associatedInterface = associatedInterface;
    }
  }
}
