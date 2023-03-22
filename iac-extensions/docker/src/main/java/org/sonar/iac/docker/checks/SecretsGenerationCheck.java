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

@Rule(key = "S6437")
public class SecretsGenerationCheck implements IacCheck {

  private static final String MESSAGE = "Revoke and change this secret, as it might be compromised.";

  private static final Set<String> SENSITIVE_FLAGS = Set.of("-N", "-t", "-b", "-f");

  // detects 'RUN ssh-keygen -N "" -t dsa -b 1024 -f rsync-key'
  private static final CommandDetector SSH_DETECTOR = CommandDetector.builder()
    .with("ssh-keygen")
    .withOptionAndSurroundingAnyOptionsExcluding("-N", "", SENSITIVE_FLAGS)
    .withOptionAndSurroundingAnyOptionsExcluding("-t", "dsa", SENSITIVE_FLAGS)
    .withOptionAndSurroundingAnyOptionsExcluding("-b", "1024", SENSITIVE_FLAGS)
    .withOptionAndSurroundingAnyOptionsExcluding("-f", "rsync-key", SENSITIVE_FLAGS)
    .build();

  private static final Set<String> SENSITIVE_KEYTOOL_FLAGS = Set.of("-gencert", "-genkeypair", "-genseckey", "-genkey");

  private static final CommandDetector KEYTOOL_DETECTOR = CommandDetector.builder()
    .with("keytool")
    .withOptionalRepeatingExcept(SENSITIVE_KEYTOOL_FLAGS)
    .with(SENSITIVE_KEYTOOL_FLAGS)
    .withOptionalRepeatingExcept(SENSITIVE_KEYTOOL_FLAGS)
    .build();

  private static final Set<String> SENSITIVE_OPENSSL_SUBCOMMANDS = Set.of("req", "genrsa", "rsa", "gendsa", "ec", "ecparam", "x509", "genpkey", "pkey");

  private static final CommandDetector SENSITIVE_OPENSSL_COMMANDS = CommandDetector.builder()
    .with("openssl")
    .with(SENSITIVE_OPENSSL_SUBCOMMANDS)
    // every flag that comes after a MATCH of the sensitive subcommand should be flagged as well
    .withOptionalRepeating(s -> s.startsWith("-"))
    .build();

  private static final Set<CommandDetector> DETECTORS = Set.of(
    SSH_DETECTOR, KEYTOOL_DETECTOR, SENSITIVE_OPENSSL_COMMANDS);

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, SecretsGenerationCheck::checkRunInstruction);
  }

  private static void checkRunInstruction(CheckContext ctx, RunInstruction runInstruction) {
    List<ArgumentResolution> resolvedArgument = CheckUtils.resolveInstructionArguments(runInstruction);

    DETECTORS.forEach(
      detector -> detector.search(resolvedArgument).forEach(command -> ctx.reportIssue(command, MESSAGE)));
  }
}
