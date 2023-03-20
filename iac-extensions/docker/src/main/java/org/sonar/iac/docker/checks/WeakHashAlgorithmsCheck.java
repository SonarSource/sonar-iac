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
package org.sonar.iac.docker.checks;

import java.util.List;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.checks.utils.CheckUtils;
import org.sonar.iac.docker.checks.utils.CommandDetector;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.RunInstruction;

@Rule(key = "S4790")
public class WeakHashAlgorithmsCheck implements IacCheck {

  private static final String MESSAGE = "Using weak hashing algorithms is security-sensitive.";

  private static final Set<String> OPENSSL_SENSITIVE_SUBCOMMAND = Set.of("md5", "sha1", "rmd160", "ripemd160");
  private static final Set<String> OPENSSL_SENSITIVE_DGST_OPTION = Set.of("-md2", "-md4", "-md5", "-sha1", "-ripemd160", "-ripemd", "-rmd160");
  private static final Set<String> SHASUM_SENSITIVE_COMMAND = Set.of("md5sum", "sha1sum");

  private static final CommandDetector SENSITIVE_OPENSSL_SUBCOMMAND = CommandDetector.builder()
    .with("openssl"::equals)
    .with(OPENSSL_SENSITIVE_SUBCOMMAND::contains)
    .build();
  private static final CommandDetector SENSITIVE_OPENSSL_DGST = CommandDetector.builder()
    .with("openssl"::equals)
    .with("dgst"::equals)
    .withOptional(s -> !OPENSSL_SENSITIVE_DGST_OPTION.contains(s) && s.startsWith("-"))
    .with(OPENSSL_SENSITIVE_DGST_OPTION::contains)
    .build();
  private static final CommandDetector SENSITIVE_SHASUM_COMMAND = CommandDetector.builder()
    .with(SHASUM_SENSITIVE_COMMAND::contains)
    .build();
  private static final CommandDetector SENSITIVE_SHASUN_COMMAND_WITHOUT_OPTION = CommandDetector.builder()
    .with("shasum"::equals)
    .notWith("-a"::equals)
    .build();
  private static final CommandDetector SENSITIVE_SHASUM_COMMAND_WITH_OPTION_A_TO_1 = CommandDetector.builder()
    .with("shasum"::equals)
    .with("-a"::equals)
    .with("1"::equals)
    .build();

  private static final List<CommandDetector> COMMANDS = List.of(SENSITIVE_OPENSSL_SUBCOMMAND, SENSITIVE_OPENSSL_DGST, SENSITIVE_SHASUM_COMMAND,
    SENSITIVE_SHASUN_COMMAND_WITHOUT_OPTION, SENSITIVE_SHASUM_COMMAND_WITH_OPTION_A_TO_1);

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, WeakHashAlgorithmsCheck::checkRun);
  }

  private static void checkRun(CheckContext ctx, RunInstruction runInstruction) {
    List<ArgumentResolution> resolvedArgument = CheckUtils.resolveInstructionArguments(runInstruction);
    COMMANDS.forEach(detector -> detector.search(resolvedArgument).forEach(command -> ctx.reportIssue(command, MESSAGE)));
  }
}
