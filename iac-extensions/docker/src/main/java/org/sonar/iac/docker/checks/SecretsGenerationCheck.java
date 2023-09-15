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
import org.sonar.iac.docker.checks.utils.StringPredicate;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.DockerImage;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.ExpandableStringLiteral;
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

  private static final String PASSWORD_FLAG = "--password";
  // It detects: wget --password=MyPassword
  private static final CommandDetector WGET_PASSWORD_FLAG_EQUALS_PWD = commandsFlagEquals(List.of("wget"), PASSWORD_FLAG);

  // It detects: wget --password MyPassword
  private static final CommandDetector WGET_PASSWORD_FLAG_SPACE_PWD = commandFlagSpace("wget", PASSWORD_FLAG);

  private static final CommandDetector WGET_FTP_PASSWORD_FLAG_EQUALS_PWD = commandsFlagEquals(List.of("wget"), "--ftp-password");

  private static final CommandDetector WGET_FTP_PASSWORD_FLAG_SPACE_PWD = commandFlagSpace("wget", "--ftp-password");
  private static final CommandDetector WGET_HTTP_PASSWORD_FLAG_EQUALS_PWD = commandsFlagEquals(List.of("wget"), "--http-password");

  private static final CommandDetector WGET_HTTP_PASSWORD_FLAG_SPACE_PWD = commandFlagSpace("wget", "--http-password");

  private static final CommandDetector WGET_PROXY_PASSWORD_FLAG_EQUALS_PWD = commandsFlagEquals(List.of("wget"), "--proxy-password");

  private static final CommandDetector WGET_PROXY_PASSWORD_FLAG_SPACE_PWD = commandFlagSpace("wget", "--proxy-password");

  private static final CommandDetector CURL_USER_FLAG_SPACE_PWD = commandFlagSpace("curl", "--user");
  private static final CommandDetector CURL_USER_SHORT_FLAG_SPACE_PWD = commandFlagSpace("curl", "-u");

  private static final CommandDetector SSHPASS_P_FLAG_SPACE_PWD = commandFlagSpace("sshpass", "-p");

  private static final CommandDetector SSHPASS_P_FLAG_NO_SPACE_PWD = commandsFlagNoSpacePwd(List.of("sshpass"), "-p");

  private static final List<String> MYSQL_COMMANDS = List.of("mysql", "mysqladmin", "mysqldump");
  private static final CommandDetector MYSQL_PASSWORD_EQUALS_PWD = commandsFlagEquals(MYSQL_COMMANDS, PASSWORD_FLAG);

  private static final CommandDetector MYSQL_P_FLAG_NO_SPACE_PWD = commandsFlagNoSpacePwd(MYSQL_COMMANDS, "-p");

  private static CommandDetector commandsFlagNoSpacePwd(List<String> commands, String flag) {
    return CommandDetector.builder()
      .with(commands)
      .withAnyIncludingUnresolvedExcluding(arg -> !arg.startsWith(flag))
      .withArgumentResolutionIncludeUnresolved(resolution -> {
        if (resolution.value().startsWith(flag)) {
          if (resolution.value().length() > 2) {
            // for -p"PASSWORD" and -p'PASSWORD'
            return true;
          }
          if (resolution.argument().expressions().size() > 1) {
            // for -p"$PASSWORD", -p$PASSWORD etc
            return true;
          }
          if (resolution.argument().expressions().get(0).is(DockerTree.Kind.EXPANDABLE_STRING_LITERAL)) {
            ExpandableStringLiteral expression = (ExpandableStringLiteral) resolution.argument().expressions().get(0);
            // for "-p$PASSWORD"
            return expression.expressions().size() > 1;
          }
        }
        return false;
      })
      .build();
  }

  private static CommandDetector commandsFlagEquals(List<String> commands, String flag) {
    String flagAndEquals = flag + "=";
    return CommandDetector.builder()
      .with(commands)
      .withAnyIncludingUnresolvedExcluding(arg -> !arg.startsWith(flagAndEquals))
      .withIncludeUnresolved(arg -> arg.startsWith(flagAndEquals))
      .build();
  }

  private static CommandDetector commandFlagSpace(String command, String flag) {
    return CommandDetector.builder()
      .with(command)
      .withOptionalRepeatingExcept(StringPredicate.equalsIgnoreQuotes(flag))
      .with(StringPredicate.equalsIgnoreQuotes(flag))
      .withIncludeUnresolved(a -> true)
      .build();
  }

  private static final Set<CommandDetector> DETECTORS_THAT_STORE_SECRETS = Set.of(
    SSH_DETECTOR,
    KEYTOOL_DETECTOR,
    SENSITIVE_OPENSSL_COMMANDS);

  private static final Set<CommandDetector> DETECTORS_THAT_HAVE_SECRETS_IN_CMD = Set.of(
    WGET_PASSWORD_FLAG_EQUALS_PWD,
    WGET_PASSWORD_FLAG_SPACE_PWD,
    WGET_FTP_PASSWORD_FLAG_EQUALS_PWD,
    WGET_FTP_PASSWORD_FLAG_SPACE_PWD,
    WGET_HTTP_PASSWORD_FLAG_EQUALS_PWD,
    WGET_HTTP_PASSWORD_FLAG_SPACE_PWD,
    WGET_PROXY_PASSWORD_FLAG_EQUALS_PWD,
    WGET_PROXY_PASSWORD_FLAG_SPACE_PWD,
    SSHPASS_P_FLAG_NO_SPACE_PWD,
    SSHPASS_P_FLAG_SPACE_PWD,
    MYSQL_PASSWORD_EQUALS_PWD,
    MYSQL_P_FLAG_NO_SPACE_PWD);

  private static final Set<CommandDetector> CURL_DETECTORS = Set.of(CURL_USER_FLAG_SPACE_PWD,
    CURL_USER_SHORT_FLAG_SPACE_PWD);

  @Override
  public void initialize(InitContext init) {
    init.register(DockerImage.class, SecretsGenerationCheck::checkDockerImage);
  }

  private static void checkDockerImage(CheckContext ctx, DockerImage dockerImage) {
    if (!dockerImage.isLastDockerImageInFile()) {
      return;
    }

    dockerImage.instructions().stream()
      .filter(RunInstruction.class::isInstance)
      .map(RunInstruction.class::cast)
      .forEach(runInstruction -> checkRunInstruction(ctx, runInstruction));
  }

  private static void checkRunInstruction(CheckContext ctx, RunInstruction runInstruction) {
    List<ArgumentResolution> resolvedArgument = CheckUtils.resolveInstructionArguments(runInstruction);

    DETECTORS_THAT_STORE_SECRETS.forEach(
      detector -> detector.search(resolvedArgument).forEach(command -> ctx.reportIssue(command, MESSAGE)));

    if (isMountSecret(runInstruction)) {
      // Let's ignore the following detectors. Tha assumptions is that, if user use RUN --mount=type=secret
      // then user knows how to do it in safe way. Still FN are possible, e.g.:
      // `RUN --mount=type=secret wget --password ${PASSWORD}`
      // There is docker variable substitution used that will occur before the execution of RUN instruction
      return;
    }

    DETECTORS_THAT_HAVE_SECRETS_IN_CMD.forEach(
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
