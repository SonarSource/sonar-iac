/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.ArgInstruction;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.EnvInstruction;
import org.sonar.iac.docker.tree.api.Expression;
import org.sonar.iac.docker.tree.api.KeyValuePair;
import org.sonar.iac.docker.tree.api.Variable;

@Rule(key = "S6472")
public class SecretsHandlingCheck implements IacCheck {

  private static final String MESSAGE = "Make sure that using %s to handle a secret is safe here.";

  private static final Set<String> ENTITIES = Set.of("ACCESS", "AMPLITUDE", "ANSIBLE", "ADMIN", "API",
    "APP", "AUTH", "CLIENT", "CONFIG", "DATABASE", "DB", "ENCRYPTION", "ENV", "FACEBOOK", "FIREBASE", "FTP", "GIT",
    "GITHUB", "GITLAB", "HONEYCOMB", "JWT", "KEYCLOAK", "KEYRING", "LDAP", "MAIL", "MASTER", "MARIADB", "MSSQL",
    "MYSQL", "NPM", "OAUTH", "OAUTH2", "PG", "POSTGRES", "REDIS", "REFRESH", "REPLICATION", "ROOT", "RPC", "SA",
    "SECRET", "SERVER", "SIGN", "SIGNING", "SLACK", "SVN", "USER", "VNC", "WEBHOOK", "JDBC");

  private static final Set<String> SECRETS = Set.of("CREDENTIALS", "KEY", "PASS", "PASSPHRASE", "PASSWD", "PASSWORD",
    "SECRET", "TOKEN");

  private static final Set<String> EXCLUSIONS = Set.of("ALLOW", "DIR", "EXPIRE", "EXPIRY", "FILE", "ID",
    "LOCATION", "NAME", "OWNER", "PATH", "URL", "SIZE");

  // Patterns to split the identifier of a variable into separate words
  private static final Pattern UNDERSCORE_NAME_PATTERN = Pattern.compile("^\\w+$");
  private static final Pattern DASH_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9-]+$");
  private static final Pattern CAMELCASE_NAME_PATTERN = Pattern.compile("^[A-Za-z]+$");
  private static final Pattern CAMELCASE_SPLIT_PATTERN = Pattern.compile("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");

  // Pattern to identify a URL
  private static final Pattern URL_PATTERN = Pattern.compile("^[a-zA-Z][-a-zA-Z0-9+.]*://");

  // Pattern to identify a path
  private static final String ROOT_PATH_PATTERN = "^/[a-z]*+($|/)";
  private static final String RELATIVE_PATH_PATTERN = "^./[a-zA-Z_-]*+($|/)";
  private static final String EXPANSION_PATH_PATTERN = "^\\$\\{[^}]+}/";
  // FP URIs should not be hardcoded
  @SuppressWarnings("java:S1075")
  private static final String PATH_WITH_EXPANSION_PATTERN = "/.*+\\.[a-z0-9]{2,4}$";
  private static final Pattern PATH_PATTERN = Pattern.compile("(" + ROOT_PATH_PATTERN + "|" + RELATIVE_PATH_PATTERN
    + "|" + EXPANSION_PATH_PATTERN + "|" + PATH_WITH_EXPANSION_PATTERN + ")");

  enum AssignmentType {
    ARG,
    ENV
  }

  @Override
  public void initialize(InitContext init) {
    init.register(EnvInstruction.class, (ctx, envInstruction) -> checkAssignments(ctx, envInstruction.environmentVariables(), AssignmentType.ENV));
    init.register(ArgInstruction.class, (ctx, argInstruction) -> checkAssignments(ctx, argInstruction.keyValuePairs(), AssignmentType.ARG));
  }

  private static void checkAssignments(CheckContext ctx, List<KeyValuePair> assignments, AssignmentType type) {
    for (KeyValuePair assignment : assignments) {
      if (isSensitiveName(assignment.key()) && isSensitiveValue(assignment.value(), type)) {
        ctx.reportIssue(assignment.key(), String.format(MESSAGE, type.name()));
      }
    }
  }

  /**
   * Check if the left hand of the assignment is a sensitive
   */
  private static boolean isSensitiveName(Argument nameArgument) {
    ArgumentResolution nameResolution = ArgumentResolution.of(nameArgument);
    return nameResolution.isResolved() && isSensitiveVariableName(nameResolution.value());
  }

  /**
   * Check if the right hand of the assignment is sensitive
   */
  private static boolean isSensitiveValue(@Nullable Argument secret, AssignmentType type) {
    if (secret == null) {
      return type.equals(AssignmentType.ARG);
    }

    ArgumentResolution valueResolution = ArgumentResolution.of(secret);
    if (valueResolution.isUnresolved()) {
      return type.equals(AssignmentType.ARG) || isSensitiveVariableName(secret);
    }

    String value = valueResolution.value();
    if (value.isBlank()) {
      return type.equals(AssignmentType.ARG);
    }

    return !isUrl(value) && !isPath(value);
  }

  /**
   * Check if the argument contains of a single variable expression and check if its name is sensitive
   */
  private static boolean isSensitiveVariableName(Argument secret) {
    List<Expression> expressions = secret.expressions();
    if (expressions.size() == 1 && expressions.get(0) instanceof Variable variable) {
      String identifier = variable.identifier();
      return isSensitiveVariableName(identifier);
    }
    return false;
  }

  /**
   * Check if the identifier of a variable is sensitive
   */
  private static boolean isSensitiveVariableName(String identifier) {
    List<String> words = VarNameSplitter.split(identifier);
    return isSecretWordOnly(words) || containsSecretEntityWordCombination(words);
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

  private static boolean isUrl(String value) {
    return URL_PATTERN.matcher(value).find();
  }

  private static boolean isPath(String value) {
    return PATH_PATTERN.matcher(value).find();
  }

  private static class VarNameSplitter {
    private static List<String> split(String name) {
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
      return Stream.of(strings).map(s -> s.toUpperCase(Locale.ROOT)).toList();
    }
  }

}
