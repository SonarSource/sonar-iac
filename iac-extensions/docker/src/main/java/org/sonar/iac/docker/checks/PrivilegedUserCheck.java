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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.TreeUtils;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.Body;
import org.sonar.iac.docker.tree.api.DockerImage;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.FromInstruction;
import org.sonar.iac.docker.tree.api.UserInstruction;

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
  private static final Set<String> UNSAFE_USERS = Set.of("root", "containerAdministrator");
  private static final Set<String> SAFE_IMAGES = Set.of("adminer", "api-firewall", "elasticsearch", "emqx", "flink", "fluentd", "geonetwork", "groovy", "haproxy",
    "ibm-semeru-runtimes", "irssi", "jetty", "jobber", "kibana", "kong", "lightstreamer", "logstash", "memcached", "neo4j", "odoo", "open-liberty", "percona",
    "rocket.chat", "solr", "swift", "varnish", "vault", "websphere-liberty", "znc", "nginxinc/nginx-unprivileged");

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

  private static final String MESSAGE_SCRATCH = "Scratch images run as root by default. Make sure it is safe here.";
  private static final String MESSAGE_UNSAFE_DEFAULT_ROOT = "The %s image runs with root as the default user. Make sure it is safe here.";
  private static final String MESSAGE_MICROSOFT_DEFAULT_ROOT = "This image runs with root or containerAdministrator as the default user. Make sure it is safe here.";
  private static final String MESSAGE_OTHER_IMAGE = "This image might run with root as the default user. Make sure it is safe here.";
  private static final String MESSAGE_ROOT_USER = "Setting the default user as %s might unnecessarily make the application unsafe. Make sure it is safe here.";

  @Override
  public void initialize(InitContext init) {
    init.register(DockerImage.class, this::handle);
  }

  private void handle(CheckContext ctx, DockerImage dockerImage) {
    if (!dockerImage.isLastDockerImageInFile()) {
      return;
    }

    getLastUser(dockerImage).ifPresentOrElse(
      lastUserInstruction -> checkLastUserInstruction(ctx, lastUserInstruction),
      () -> checkLastImageName(ctx, dockerImage.from()));
  }

  private static void checkLastUserInstruction(CheckContext ctx, UserInstruction userInstruction) {
    if (userInstruction.arguments().size() != 1) {
      return;
    }
    Argument user = userInstruction.arguments().get(0);
    String userName = ArgumentResolution.of(user).value();
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

  private static String getImageName(FromInstruction from) {
    ArgumentResolution resolvedImage = ArgumentResolution.of(from.image());
    String fullImageName = resolvedImage.value();
    if (resolvedImage.isUnresolved()) {
      return null;
    } else if (fullImageName.contains(":")) {
      return fullImageName.split(":")[0];
    } else if (fullImageName.contains("@")) {
      return fullImageName.split("@")[0];
    } else {
      return fullImageName;
    }
  }

  private static Optional<UserInstruction> getLastUser(DockerImage dockerImage) {
    return TreeUtils.lastDescendant(dockerImage, tree -> ((DockerTree) tree).is(DockerTree.Kind.USER)).map(UserInstruction.class::cast);
  }

  // All possible image use cases
  private static boolean isScratchImage(String imageName) {
    return imageName.equals("scratch");
  }

  private static boolean isUnsafeImage(String imageName) {
    return UNSAFE_IMAGES.contains(imageName);
  }

  private boolean isSafeImage(String imageName) {
    return SAFE_IMAGES.contains(imageName) || imageName.startsWith("bitnami/") || isUserSafeImage(imageName);
  }

  private boolean isUserSafeImage(String imageName) {
    return userSafeImages().contains(imageName);
  }

  private static boolean isMicrosoftUnsafeImage(String imageName) {
    return imageName.startsWith("mcr.microsoft.com/");
  }
}
