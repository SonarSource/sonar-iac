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

  enum Kind implements GrammarRuleKey {
    FILE(FileTree.class),
    INSTRUCTION(InstructionTree.class),
    ONBUILD(OnBuildTree.class),
    FROM(FromTree.class),
    IMAGE(ImageTree.class),
    PARAM(ParamTree.class),
    ALIAS(AliasTree.class),
    MAINTAINER(MaintainerTree.class),
    STOPSIGNAL(StopSignalTree.class),
    WORKDIR(WorkdirTree.class),
    EXPOSE(ExposeTree.class),
    PORT(PortTree.class),
    LABEL(LabelTree.class),
    ENV(EnvTree.class),
    KEY_VALUE_PAIR(KeyValuePairTree.class),
    ARG(ArgTree.class),
    CMD(CmdTree.class),
    SHELL_FORM(ShellFormTree.class),
    EXEC_FORM(ExecFormTree.class),
    EXEC_FORM_LITERAL(ExecFormLiteralTree.class),
    ADD(AddTree.class),
    KEYVALUEPAIR(KeyValuePairTree.class),
    OPTION(OptionTree.class),

    TOKEN(SyntaxToken.class);


    private final Class<? extends DockerTree> associatedInterface;

    Kind(Class<? extends DockerTree> associatedInterface) {
      this.associatedInterface = associatedInterface;
    }
  }
}
