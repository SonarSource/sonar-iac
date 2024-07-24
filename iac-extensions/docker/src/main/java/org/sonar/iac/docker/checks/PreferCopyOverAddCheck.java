/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.docker.checks;

import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.AddInstruction;
import org.sonar.iac.docker.tree.api.Argument;

@Rule(key = "S7029")
public class PreferCopyOverAddCheck implements IacCheck {
  private static final String MESSAGE = "Replace this ADD instruction with a COPY instruction.";
  private static final Set<String> ARCHIVE_EXTENSIONS = Set.of(".tar", ".gz", ".tgz", ".xz", ".txz", ".bz2", ".tbz2", ".tbz", ".lz", ".tlz",
    ".lzma", ".tlzma", ".lzo", ".tlzo", ".7z", ".zip");

  @Override
  public void initialize(InitContext init) {
    init.register(AddInstruction.class, PreferCopyOverAddCheck::checkAddInstruction);
  }

  private static void checkAddInstruction(CheckContext ctx, AddInstruction tree) {
    if (tree.srcs().stream().noneMatch(PreferCopyOverAddCheck::isAllowedAddSource)) {
      ctx.reportIssue(tree, MESSAGE);
    }
  }

  private static boolean isAllowedAddSource(Argument argument) {
    var argumentResolution = ArgumentResolution.of(argument);

    if (argumentResolution.isUnresolved()) {
      // we can't determine if the source is a local file or not, so we allow it as a source of ADD instruction
      return true;
    }

    var src = argumentResolution.value();
    return src.startsWith("http") || src.startsWith("git") || ARCHIVE_EXTENSIONS.stream().anyMatch(src::endsWith);
  }
}
