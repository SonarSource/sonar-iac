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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.checks.utils.ArgumentResolutionSplitter;
import org.sonar.iac.docker.checks.utils.CheckUtils;
import org.sonar.iac.docker.checks.utils.CommandDetector;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.Flag;
import org.sonar.iac.docker.tree.api.RunInstruction;

@Rule(key = "S6437")
public class SecretsGenerationCheck implements IacCheck {

  private static final String MESSAGE = "Change this code not to store a secret in the image.";

  private static final Set<String> SSH_KEYGEN_COMPLIANT_FLAGS = Set.of("-l", "-F", "-H", "-R", "-r", "-k", "-Q");

  private static final CommandDetector SSH_DETECTOR = CommandDetector.builder()
    .with("ssh-keygen")
    .withOptionalRepeatingExcept(SSH_KEYGEN_COMPLIANT_FLAGS)
    .notWith(SSH_KEYGEN_COMPLIANT_FLAGS::contains)
    .build();

  private static final Set<String> SENSITIVE_KEYTOOL_FLAGS = Set.of("-gencert", "-genkeypair", "-genseckey", "-genkey");

  private static final CommandDetector KEYTOOL_DETECTOR = CommandDetector.builder()
    .with("keytool")
    .withAnyOptionExcluding(SENSITIVE_KEYTOOL_FLAGS)
    .with(SENSITIVE_KEYTOOL_FLAGS)
    .withAnyOptionExcluding(SENSITIVE_KEYTOOL_FLAGS)
    .build();

  private static final Set<String> SENSITIVE_OPENSSL_SUBCOMMANDS = Set.of("req", "genrsa", "rsa", "gendsa", "ec", "ecparam", "x509", "genpkey", "pkey");

  private static final CommandDetector SENSITIVE_OPENSSL_COMMANDS = CommandDetector.builder()
    .with("openssl")
    .with(SENSITIVE_OPENSSL_SUBCOMMANDS)
    // every flag that comes after a MATCH of the sensitive subcommand should be flagged as well
    .withAnyOptionExcluding(Collections.emptyList())
    .build();

  // It detects: wget --password=MyPassword
  private static final CommandDetector WGET_PASSWORD_FLAG_EQUALS_PWD = wgetFlagEquals("--password");

  // It detects: wget --password MyPassword
  private static final CommandDetector WGET_PASSWORD_FLAG_SPACE_PWD = commandFlagSpace("wget", "--password");

  private static final CommandDetector WGET_FTP_PASSWORD_FLAG_EQUALS_PWD = wgetFlagEquals("--ftp-password");

  private static final CommandDetector WGET_FTP_PASSWORD_FLAG_SPACE_PWD = commandFlagSpace("wget", "--ftp-password");
  private static final CommandDetector WGET_HTTP_PASSWORD_FLAG_EQUALS_PWD = wgetFlagEquals("--http-password");

  private static final CommandDetector WGET_HTTP_PASSWORD_FLAG_SPACE_PWD = commandFlagSpace("wget", "--http-password");

  private static final CommandDetector WGET_PROXY_PASSWORD_FLAG_EQUALS_PWD = wgetFlagEquals("--proxy-password");

  private static final CommandDetector WGET_PROXY_PASSWORD_FLAG_SPACE_PWD = commandFlagSpace("wget", "--proxy-password");

  private static final CommandDetector CURL_USER_FLAG_SPACE_PWD = commandFlagSpace("curl", "--user");
  private static final CommandDetector CURL_USER_SHORT_FLAG_SPACE_PWD = commandFlagSpace("curl", "-u");

  private static final CommandDetector SSHPASS_P_FLAG_SPACE_PWD = commandFlagSpace("sshpass", "-p");

  private static final CommandDetector SSHPASS_P_FLAG_NO_SPACE_PWD = CommandDetector.builder()
    .with("sshpass")
    .withAnyExcludingIncludeUnresolved(arg -> !arg.startsWith("-p"))
    .withArgumentResolutionIncludeUnresolved(resolution -> resolution.value().startsWith("-p") &&
      (resolution.value().length() > 2 || resolution.argument().expressions().size() > 1))
    .build();

  private static CommandDetector wgetFlagEquals(String flag) {
    String flagAndEquals = flag + "=";
    return CommandDetector.builder()
      .with("wget")
      .withAnyExcludingIncludeUnresolved(arg -> !arg.startsWith(flagAndEquals))
      .withIncludeUnresolved(arg -> arg.startsWith(flagAndEquals))
      .build();
  }

  private static CommandDetector commandFlagSpace(String command, String flag) {
    return CommandDetector.builder()
      .with(command)
      .withOptionalRepeatingExcept(flag)
      .with(flag::equals)
      .withIncludeUnresolved(a -> true)
      .build();
  }

  private static final Set<CommandDetector> DETECTORS = Set.of(
    SSH_DETECTOR,
    KEYTOOL_DETECTOR,
    SENSITIVE_OPENSSL_COMMANDS);

  private static final Set<CommandDetector> DETECTORS_IGNORE_MOUNT_SECRET = Set.of(
    WGET_PASSWORD_FLAG_EQUALS_PWD,
    WGET_PASSWORD_FLAG_SPACE_PWD,
    WGET_FTP_PASSWORD_FLAG_EQUALS_PWD,
    WGET_FTP_PASSWORD_FLAG_SPACE_PWD,
    WGET_HTTP_PASSWORD_FLAG_EQUALS_PWD,
    WGET_HTTP_PASSWORD_FLAG_SPACE_PWD,
    WGET_PROXY_PASSWORD_FLAG_EQUALS_PWD,
    WGET_PROXY_PASSWORD_FLAG_SPACE_PWD,
    SSHPASS_P_FLAG_NO_SPACE_PWD,
    SSHPASS_P_FLAG_SPACE_PWD);

  private static final Set<CommandDetector> CURL_DETECTORS = Set.of(CURL_USER_FLAG_SPACE_PWD,
    CURL_USER_SHORT_FLAG_SPACE_PWD);

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, SecretsGenerationCheck::checkRunInstruction);
  }

  private static void checkRunInstruction(CheckContext ctx, RunInstruction runInstruction) {
    List<ArgumentResolution> resolvedArgument = CheckUtils.resolveInstructionArguments(runInstruction);

    DETECTORS.forEach(
      detector -> detector.search(resolvedArgument).forEach(command -> ctx.reportIssue(command, MESSAGE)));

    if (isMountSecret(runInstruction)) {
      return;
    }

    DETECTORS_IGNORE_MOUNT_SECRET.forEach(
      detector -> detector.search(resolvedArgument).forEach(command -> ctx.reportIssue(command, MESSAGE)));

    CURL_DETECTORS.forEach(
      detector -> detector.search(resolvedArgument).forEach(command -> {
        ArgumentResolution userAndPassword = getLastArgument(command);
        if (userAndPassword.value().contains(":")) {
          ctx.reportIssue(command, MESSAGE);
        }
      }));

    ArgumentResolutionSplitter.splitCommands(resolvedArgument).elements()
      .forEach(arguments -> checkHtpasswd(arguments, ctx));
  }

  // Check if it is: RUN --mount=type=secret ...
  private static boolean isMountSecret(RunInstruction runInstruction) {
    for (Flag option : runInstruction.options()) {
      if ("mount".equals(option.name()) && (ArgumentResolution.of(option.value()).value()).contains("type=secret")) {
        return true;
      }
    }
    return false;
  }

  private static void checkHtpasswd(List<ArgumentResolution> resolvedArgument, CheckContext ctx) {
    if (HtpasswdDetector.detect(resolvedArgument)) {
      ctx.reportIssue(new CommandDetector.Command(resolvedArgument), MESSAGE);
    }
  }

  private static ArgumentResolution getLastArgument(CommandDetector.Command command) {
    List<ArgumentResolution> arguments = command.getResolvedArguments();
    return arguments.get(arguments.size() - 1);
  }

  private static class HtpasswdDetector {

    public static boolean detect(List<ArgumentResolution> resolvedArgument) {
      boolean flagB = false;
      boolean flagN = false;
      int numberOfNonFlags = 0;
      for (int i = 0; i < resolvedArgument.size(); i++) {
        String current = resolvedArgument.get(i).value();
        if (i == 0 && !"htpasswd".equals(current)) {
          break;
        }
        if (current.startsWith("-")) {
          if (current.contains("b")) {
            flagB = true;
          }
          if (current.contains("n")) {
            flagN = true;
          }
        } else {
          numberOfNonFlags++;
        }
      }
      return detectedSensitiveCommand(flagB, flagN, numberOfNonFlags);
    }

    private static boolean detectedSensitiveCommand(boolean flagB, boolean flagN, int numberOfNonFlags) {
      return flagB && ((!flagN && numberOfNonFlags == 4) || (flagN && numberOfNonFlags == 3));
    }
  }
}
