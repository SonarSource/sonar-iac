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
import org.sonar.iac.docker.tree.impl.StringLiteralImpl;
import org.sonar.sslr.grammar.GrammarRuleKey;

public interface DockerTree extends Tree {

  boolean is(Kind... kind);
  Kind getKind();
  DockerTree parent();
  void setParent(DockerTree parent);

  enum Kind implements GrammarRuleKey {
    FILE(File.class),
    DOCKERIMAGE(DockerImage.class),
    INSTRUCTION(Instruction.class),
    ONBUILD(OnBuildInstruction.class),
    FROM(FromInstruction.class),
    IMAGE(Image.class),
    PARAM(Param.class),
    ALIAS(Alias.class),
    ARGUMENT(Argument.class),
    STRING_LITERAL(StringLiteralImpl.class),
    DOUBLE_QUOTED_STRING(DoubleQuotedString.class),
    QUOTED_STRING(QuotedString.class),
    MAINTAINER(MaintainerInstruction.class),
    STOPSIGNAL(StopSignalInstruction.class),
    WORKDIR(WorkdirInstruction.class),
    EXPOSE(ExposeInstruction.class),
    PORT(Port.class),
    LABEL(LabelInstruction.class),
    ENV(EnvInstruction.class),
    KEY_VALUE_PAIR(KeyValuePair.class),
    ARG(ArgInstruction.class),
    CMD(CmdInstruction.class),
    ENTRYPOINT(EntrypointInstruction.class),
    RUN(RunInstruction.class),
    SHELL_FORM(ShellForm.class),
    EXEC_FORM(ExecForm.class),
    EXEC_FORM_LITERAL(ExecFormLiteral.class),
    ADD(AddInstruction.class),
    COPY(CopyInstruction.class),
    VOLUME(VolumeInstruction.class),
    USER(UserInstruction.class),
    SHELL(ShellInstruction.class),
    HEALTHCHECK(HealthCheckInstruction.class),
    NONE(NoneInstruction.class),
    HEREDOCUMENT(HereDocument.class),

    TOKEN(SyntaxToken.class);


    private final Class<? extends DockerTree> associatedInterface;

    Kind(Class<? extends DockerTree> associatedInterface) {
      this.associatedInterface = associatedInterface;
    }
  }
}
