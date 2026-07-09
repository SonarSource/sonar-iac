/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.docker.checks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.checks.utils.MultiStageBuildInspector;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.TreeUtils;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.ArgumentList;
import org.sonar.iac.docker.tree.api.CmdInstruction;
import org.sonar.iac.docker.tree.api.CodeInstruction;
import org.sonar.iac.docker.tree.api.DockerImage;
import org.sonar.iac.docker.tree.api.EntrypointInstruction;
import org.sonar.iac.docker.tree.api.FromInstruction;
import org.sonar.iac.docker.tree.api.OnBuildInstruction;
import org.sonar.iac.docker.tree.api.RunInstruction;
import org.sonar.iac.docker.tree.api.ShellCode;
import org.sonar.iac.docker.tree.api.UserInstruction;

import static org.sonar.iac.docker.checks.utils.CheckUtils.isScratchImage;

@Rule(key = "S6471")
public class PrivilegedUserCheck implements IacCheck {

  private static final Set<String> UNSAFE_IMAGES = Set.of("aerospike", "almalinux", "alpine", "alt", "amazoncorretto",
    "amazonlinux", "arangodb", "archlinux", "backdrop", "bash", "buildpack-deps", "busybox", "caddy", "cirros", "clearlinux", "clefos",
    "clojure", "composer", "consul", "couchdb", "crate", "dart", "debian", "drupal", "eclipse-temurin", "elixir", "erlang", "express-gateway",
    "fedora", "friendica", "gazebo", "gcc", "golang", "haskell", "haxe", "hitch", "httpd", "hylang", "ibmjava", "influxdb", "joomla", "jruby",
    "julia", "kapacitor", "mageia", "matomo", "maven", "mediawiki", "monica", "mono", "nats", "nats-streaming", "neurodebian", "nextcloud",
    "nginx", "notary", "openjdk", "oraclelinux", "orientdb", "perl", "photon", "php", "phpmyadmin", "php-zendserver", "plone", "postfixadmin",
    "pypy", "python", "redmine", "registry", "rethinkdb", "rockylinux", "ros", "ruby", "rust", "r-base", "sapmachine", "satosa", "silverpeas",
    "sl", "spiped", "swipl", "telegraf", "tomcat", "tomee", "traefik", "ubuntu", "xwiki", "yourls", "bonita", "cassandra", "centos", "chronograf",
    "convertigo", "couchbase", "docker", "eclipse-mosquitto", "eggdrop", "ghost", "gradle", "mariadb", "mongo", "mongo-express", "mysql", "node",
    "postgres", "rabbitmq", "rakudo-star", "redis", "sonarqube", "storm", "swift", "teamspeak", "zookeeper");
  private static final Set<String> UNSAFE_USERS = Set.of("root", "containerAdministrator", "0");
  private static final Set<String> SAFE_IMAGES = Set.of("adminer", "api-firewall", "elasticsearch", "emqx", "flink", "fluentd", "geonetwork", "groovy", "haproxy",
    "ibm-semeru-runtimes", "irssi", "jetty", "jobber", "kibana", "kong", "lightstreamer", "logstash", "memcached", "neo4j", "odoo", "open-liberty", "percona",
    "rocket.chat", "solr", "swift", "varnish", "vault", "websphere-liberty", "znc", "nginxinc/nginx-unprivileged");
  // Registry/organization namespaces whose images run as a non-root user by default.
  private static final Set<String> SAFE_NAMESPACES = Set.of("bitnami/", "dhi.io/");

  // Commands whose invocation in a RUN signals the dockerfile drops privileges later (or sets up a separate user).
  private static final Set<String> PRIVILEGE_DROP_COMMANDS = Set.of("gosu", "su-exec", "useradd", "adduser", "setpriv");

  private static final Pattern SHELL_SEGMENT_SEPARATOR = Pattern.compile("[;&|\\n]");

  private static final Pattern SEGMENT_INVOKES_PRIVILEGE_DROP = Pattern.compile(
    "\\s*+(?:" + String.join("|", PRIVILEGE_DROP_COMMANDS) + ")\\b");

  // Bare interpreters/shells that, when launched without a script argument, run interactively.
  private static final Set<String> INTERACTIVE_REPL_COMMANDS = Set.of(
    "bash", "sh", "zsh", "pwsh", "ash", "node", "ruby", "irb", "R", "ipython", "php");

  // python, python2, python3, python3.11, python3.11.4 — all interactive when launched bare.
  private static final Pattern PYTHON_BINARY_PATTERN = Pattern.compile("python\\d*+(?:\\.\\d++)*+");

  // Shell metacharacters that mean a CMD shell-form isn't a bare REPL launch.
  private static final Pattern CMD_SHELL_METACHARACTERS = Pattern.compile("[;&|<>$`]");

  private static final String MESSAGE_SCRATCH = "Scratch images run as \"root\" by default. Make sure it is safe here.";
  private static final String MESSAGE_UNSAFE_DEFAULT_ROOT = "The \"%s\" image runs with \"root\" as the default user. Make sure it is safe here.";
  private static final String MESSAGE_MICROSOFT_DEFAULT_ROOT = "This image runs with \"root\" or \"containerAdministrator\" as the default user. Make sure it is safe here.";
  private static final String MESSAGE_OTHER_IMAGE = "This image might run with \"root\" as the default user. Make sure it is safe here.";
  private static final String MESSAGE_ROOT_USER = "Setting the default user as \"%s\" might unnecessarily make the application unsafe. Make sure it is safe here.";

  @RuleProperty(
    key = "safeImages",
    description = "Comma separated list of safe images (no default root user).",
    defaultValue = "")
  public String safeImages = "";

  private Set<String> safeImagesSet;

  private Set<String> userSafeImages() {
    if (safeImagesSet == null) {
      safeImagesSet = Stream.of(safeImages.split(","))
        .map(String::trim).collect(Collectors.toSet());
    }
    return safeImagesSet;
  }

  @Override
  public void initialize(InitContext init) {
    init.register(DockerImage.class, this::handle);
  }

  private void handle(CheckContext ctx, DockerImage dockerImage) {
    if (!MultiStageBuildInspector.isLastStage(dockerImage)) {
      return;
    }

    getLastUser(dockerImage).ifPresentOrElse(
      lastUserInstruction -> checkLastUserInstruction(ctx, lastUserInstruction),
      () -> {
        // Suppression is scoped to this last stage; builder stages don't affect the final image.
        if (!isRootByDesign(dockerImage)) {
          checkLastImageName(ctx, dockerImage.from());
        }
      });
  }

  private static void checkLastUserInstruction(CheckContext ctx, UserInstruction userInstruction) {
    if (userInstruction.arguments().size() != 1) {
      return;
    }
    Argument user = userInstruction.arguments().get(0);
    String resolvedValue = ArgumentResolution.of(user).value();
    // USER can be specified as user:group; extract only the user part for the privilege check
    int colonIndex = resolvedValue.indexOf(':');
    String userName = colonIndex >= 0 ? resolvedValue.substring(0, colonIndex) : resolvedValue;
    if (UNSAFE_USERS.contains(userName)) {
      ctx.reportIssue(userInstruction, String.format(MESSAGE_ROOT_USER, userName));
    }
  }

  private void checkLastImageName(CheckContext ctx, FromInstruction fromInstruction) {
    String imageName = getImageName(fromInstruction);
    if (imageName == null) {
      return;
    }

    if (isScratchImage(imageName)) {
      ctx.reportIssue(fromInstruction, MESSAGE_SCRATCH);
    } else if (isUnsafeImage(imageName) && !isUserSafeImage(imageName)) {
      ctx.reportIssue(fromInstruction, String.format(MESSAGE_UNSAFE_DEFAULT_ROOT, imageName));
    } else if (isMicrosoftUnsafeImage(imageName)) {
      ctx.reportIssue(fromInstruction, MESSAGE_MICROSOFT_DEFAULT_ROOT);
    } else if (!isSafeImage(imageName)) {
      ctx.reportIssue(fromInstruction, MESSAGE_OTHER_IMAGE);
    }
  }

  private static boolean isRootByDesign(DockerImage image) {
    return TreeUtils.firstDescendant(image, OnBuildInstruction.class).isPresent()
      || image.instructions().stream()
        .filter(RunInstruction.class::isInstance)
        .map(RunInstruction.class::cast)
        .anyMatch(PrivilegedUserCheck::runHasPrivilegeDropCommand)
      || lastLaunchIsInteractiveShell(image);
  }

  private static boolean runHasPrivilegeDropCommand(RunInstruction run) {
    var code = run.code();
    if (code instanceof ArgumentList argumentList) {
      return resolveArguments(argumentList)
        .filter(tokens -> !tokens.isEmpty())
        .map(tokens -> PRIVILEGE_DROP_COMMANDS.contains(basename(tokens.get(0))))
        .orElse(false);
    }
    if (code instanceof ShellCode<?> shellCode) {
      // originalSourceCode is null in Community edition; without it we can't classify the body.
      var source = shellCode.originalSourceCode();
      return source != null && shellSourceInvokesPrivilegeDrop(source);
    }
    return false;
  }

  private static boolean shellSourceInvokesPrivilegeDrop(String source) {
    if (segmentInvokesPrivilegeDrop(source, 0)) {
      return true;
    }
    var matcher = SHELL_SEGMENT_SEPARATOR.matcher(source);
    while (matcher.find()) {
      if (segmentInvokesPrivilegeDrop(source, matcher.end())) {
        return true;
      }
    }
    return false;
  }

  private static boolean segmentInvokesPrivilegeDrop(String source, int from) {
    return SEGMENT_INVOKES_PRIVILEGE_DROP.matcher(source).region(from, source.length()).lookingAt();
  }

  private static boolean lastLaunchIsInteractiveShell(DockerImage image) {
    // Docker semantics: when both ENTRYPOINT and CMD are set, the image runs ENTRYPOINT
    // with CMD as default args. Otherwise the lone instruction is the launch.
    // Top-level iteration avoids picking a CMD nested in HEALTHCHECK.
    CmdInstruction lastCmd = null;
    EntrypointInstruction lastEntrypoint = null;
    for (var instruction : image.instructions()) {
      if (instruction instanceof CmdInstruction cmd) {
        lastCmd = cmd;
      } else if (instruction instanceof EntrypointInstruction ep) {
        lastEntrypoint = ep;
      }
    }
    if (lastEntrypoint == null && lastCmd == null) {
      return false;
    }
    if (tokensInteractive(resolveLaunchTokens(lastEntrypoint, lastCmd).orElse(null))) {
      return true;
    }
    // Wrapper-script ENTRYPOINTs (e.g. docker-entrypoint.sh) commonly end with `exec "$@"`, so the effective process is the CMD.
    // When the entrypoint binary isn't a shell we recognize, fall back to inspecting CMD alone.
    if (lastEntrypoint != null && lastCmd != null) {
      return tokensInteractive(resolveCodeTokens(lastCmd).orElse(null));
    }
    return false;
  }

  private static boolean tokensInteractive(@Nullable List<String> tokens) {
    return tokens != null && !tokens.isEmpty() && tokensLaunchInteractiveShell(tokens);
  }

  private static Optional<List<String>> resolveLaunchTokens(@Nullable EntrypointInstruction entrypoint, @Nullable CmdInstruction cmd) {
    if (entrypoint == null) {
      return resolveCodeTokens(cmd);
    }
    var entrypointTokens = resolveCodeTokens(entrypoint);
    if (entrypointTokens.isEmpty() || cmd == null) {
      return entrypointTokens;
    }
    var cmdTokens = resolveCodeTokens(cmd);
    if (cmdTokens.isEmpty()) {
      return Optional.empty();
    }
    var combined = new ArrayList<String>(entrypointTokens.get().size() + cmdTokens.get().size());
    combined.addAll(entrypointTokens.get());
    combined.addAll(cmdTokens.get());
    return Optional.of(combined);
  }

  private static boolean tokensLaunchInteractiveShell(List<String> tokens) {
    var binary = basename(tokens.get(0));
    if (!isInteractiveBinary(binary)) {
      return false;
    }
    var tail = tokens.subList(1, tokens.size());
    // A non-flag positional means CMD is running a script/expression, not a bare REPL.
    if (!tail.stream().allMatch(t -> t.startsWith("-"))) {
      return false;
    }
    // php only runs interactively with -a / --interactive.
    return !"php".equals(binary) || tail.contains("-a") || tail.contains("--interactive");
  }

  private static Optional<List<String>> resolveCodeTokens(@Nullable CodeInstruction instruction) {
    if (instruction == null) {
      return Optional.empty();
    }
    var code = instruction.code();
    if (code instanceof ArgumentList argumentList) {
      return resolveArguments(argumentList);
    }
    if (code instanceof ShellCode<?> shellCode) {
      var source = shellCode.originalSourceCode();
      if (source == null) {
        return Optional.empty();
      }
      var trimmed = source.trim();
      if (trimmed.isEmpty() || CMD_SHELL_METACHARACTERS.matcher(trimmed).find()) {
        return Optional.empty();
      }
      return Optional.of(List.of(trimmed.split("\\s+")));
    }
    return Optional.empty();
  }

  private static Optional<List<String>> resolveArguments(ArgumentList argumentList) {
    var values = new ArrayList<String>(argumentList.arguments().size());
    for (Argument arg : argumentList.arguments()) {
      var resolution = ArgumentResolution.of(arg);
      if (resolution.isUnresolved()) {
        return Optional.empty();
      }
      values.add(resolution.value());
    }
    return Optional.of(values);
  }

  private static boolean isInteractiveBinary(String name) {
    return INTERACTIVE_REPL_COMMANDS.contains(name) || PYTHON_BINARY_PATTERN.matcher(name).matches();
  }

  private static String basename(String path) {
    // Only strip absolute paths; a relative `./bash` typically means a local script, not the shell.
    if (!path.startsWith("/")) {
      return path;
    }
    return path.substring(path.lastIndexOf('/') + 1);
  }

  private static String getImageName(FromInstruction from) {
    ArgumentResolution resolvedImage = ArgumentResolution.of(from.image());
    String fullImageName = resolvedImage.value();
    if (resolvedImage.isUnresolved()) {
      return null;
    } else if (fullImageName.contains(":")) {
      var lastColonIndex = fullImageName.lastIndexOf(":");
      var afterColonPart = fullImageName.substring(lastColonIndex);
      if (afterColonPart.contains("/")) {
        // Here it means that the last colon is just a port of the docker repo,
        // example: customHost:8080/custom/dotnet
        return fullImageName;
      }
      return fullImageName.substring(0, lastColonIndex);
    } else if (fullImageName.contains("@")) {
      return fullImageName.split("@")[0];
    } else {
      return fullImageName;
    }
  }

  private static Optional<UserInstruction> getLastUser(DockerImage dockerImage) {
    return TreeUtils.lastDescendant(dockerImage, UserInstruction.class::isInstance)
      .map(UserInstruction.class::cast);
  }

  // All possible image use cases
  private static boolean isUnsafeImage(String imageName) {
    return UNSAFE_IMAGES.contains(imageName);
  }

  private boolean isSafeImage(String imageName) {
    return SAFE_IMAGES.contains(imageName) || isSafeNamespace(imageName) || isUserSafeImage(imageName);
  }

  private static boolean isSafeNamespace(String imageName) {
    return SAFE_NAMESPACES.stream().anyMatch(imageName::startsWith);
  }

  private boolean isUserSafeImage(String imageName) {
    return userSafeImages().contains(imageName);
  }

  private static boolean isMicrosoftUnsafeImage(String imageName) {
    return imageName.startsWith("mcr.microsoft.com/");
  }
}
