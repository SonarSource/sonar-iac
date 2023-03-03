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
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.EnvInstruction;
import org.sonar.iac.docker.tree.api.KeyValuePair;
import org.sonar.iac.docker.utils.ArgumentUtils;

@Rule(key = "S6472")
public class EnvSecretCheck implements IacCheck {

  private static final String MESSAGE = "Make sure that using ENV to handle a secret is safe here.";

  private static final Set<String> ENTITIES = Set.of("ACCESS", "AMPLITUDE", "ANSIBLE", "ADMIN", "API",
    "APP", "AUTH", "CLIENT", "CONFIG", "DATABASE", "DB", "ENCRYPTION", "ENV", "FACEBOOK", "FIREBASE", "FTP", "GIT",
    "GITHUB", "GITLAB", "HONEYCOMB", "JWT", "KEYCLOAK", "KEYRING", "LDAP", "MAIL", "MASTER", "MARIADB", "MSSQL",
    "MYSQL", "NPM", "OAUTH", "OAUTH2", "PG", "POSTGRES", "REDIS", "REFRESH", "REPLICATION", "ROOT", "RPC", "SA",
    "SECRET", "SERVER", "SIGN", "SIGNING", "SLACK", "SVN", "USER", "VNC", "WEBHOOK");

  private static final Set<String> SECRETS = Set.of("CREDENTIALS", "KEY", "PASS", "PASSPHRASE", "PASSWD", "PASSWORD",
    "SECRET", "TOKEN");

  private static final Set<String> EXCLUSIONS = Set.of("ALLOW", "DIR", "EXPIRE", "EXPIRY", "FILE", "ID",
    "LOCATION", "NAME", "OWNER", "PATH", "URL");

  private static final Pattern UNDERSCORE_NAME_PATTERN = Pattern.compile("^\\w+$");
  private static final Pattern DASH_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9-]+$");

  private static final Pattern CAMELCASE_NAME_PATTERN = Pattern.compile("^[A-Za-z]+$");

  private static final Pattern CAMELCASE_SPLIT_PATTERN = Pattern.compile("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");

  private static final Pattern URL_PATTERN = Pattern.compile("^(http|ftp)s?://");

  private static final String ROOT_PATH_PATTERN = "^/[a-z]*+($|/)";
  private static final String RELATIVE_PATH_PATTERN = "^./[a-zA-Z_-]*+($|/)";
  private static final String EXPANSION_PATH_PATTERN = "^\\$\\{[^}]+}/";
  // FP URIs should not be hardcoded
  @SuppressWarnings("java:S1075")
  private static final String PATH_WITH_EXPANSION_PATTERN = "/.*+\\.[a-z0-9]{2,4}$";
  private static final Pattern PATH_PATTERN = Pattern.compile("(" + ROOT_PATH_PATTERN + "|" + RELATIVE_PATH_PATTERN
    + "|" + EXPANSION_PATH_PATTERN + "|" + PATH_WITH_EXPANSION_PATTERN + ")");

  @Override
  public void initialize(InitContext init) {
    init.register(EnvInstruction.class, (ctx, instruction) -> instruction
      .environmentVariables().forEach(envVarAssignment -> checkEnvVariableAssignment(ctx, envVarAssignment)));
  }

  private static void checkEnvVariableAssignment(CheckContext ctx, KeyValuePair envVarAssignment) {
    if (isSensitiveName(ArgumentUtils.resolve(envVarAssignment.key()).value()) && isSensitiveSecret(envVarAssignment.value())) {
      ctx.reportIssue(envVarAssignment.key(), MESSAGE);
    }
  }

  private static boolean isSensitiveName(@Nullable String name) {
    if (name == null) return false;
    List<String> words = splitEnvVarName(name);
    return isSecretWordOnly(words) || containsSecretEntityWordCombination(words);
  }

  private static List<String> splitEnvVarName(String name) {
    if (UNDERSCORE_NAME_PATTERN.matcher(name).matches() && name.contains("_")) {
      return toUpperCase(name.split("_"));
    }
    if (DASH_NAME_PATTERN.matcher(name).matches() && name.contains("-")) {
      return toUpperCase(name.split("-"));
    }
    if (CAMELCASE_NAME_PATTERN.matcher(name).matches()) {
      return toUpperCase(CAMELCASE_SPLIT_PATTERN.split(name));
    }
    return Collections.emptyList();
  }

  private static List<String> toUpperCase(String[] strings) {
    return Stream.of(strings).map(s -> s.toUpperCase(Locale.ROOT)).collect(Collectors.toList());
  }

  private static boolean isSecretWordOnly(List<String> words) {
    return words.size() == 1 && SECRETS.contains(words.get(0));
  }

  private static boolean containsSecretEntityWordCombination(List<String> words) {
    if (words.stream().anyMatch(EXCLUSIONS::contains)) {
      return false;
    }

    for (int i = 0; i < words.size(); i++) {
      if (ENTITIES.contains(words.get(i)) && i < words.size() - 1 && SECRETS.contains(words.get(i + 1))) {
        return true;
      }
    }
    return false;
  }

  private static boolean isSensitiveSecret(@Nullable Argument secret) {
    return secret != null && isSensitiveValue(ArgumentUtils.resolve(secret).value());
  }

  private static boolean isSensitiveValue(@Nullable String value) {
    if(value == null) return false;

    return !value.isBlank() && !isUrl(value) && !isPath(value);
  }

  private static boolean isUrl(String value) {
    return URL_PATTERN.matcher(value).find();
  }

  private static boolean isPath(String value) {
    return PATH_PATTERN.matcher(value).find();
  }
}
