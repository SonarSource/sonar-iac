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
package org.sonar.iac.docker.checks.utils;

import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.Argument;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * Represent chmod call instruction in RUN Arguments, with parsed permissions ready to be checked
 */
public class Chmod {
  private static final Pattern CHMOD_OPTIONS_PATTERN = Pattern.compile("-[a-zA-Z]|--[a-zA-Z-]++");
  private static final String NUMERIC = "(?<numeric>[0-7]{1,4})";
  private static final String ALPHANUMERIC = "[ugoa]*+[=+-][rwxXstugo]++";
  private static final String ALPHANUMERICS = "(?<alphanumeric>(?:" + ALPHANUMERIC + ",?+)++)";
  private static final Pattern PERMISSIONS_PATTERN = Pattern.compile(NUMERIC + "|" + ALPHANUMERICS);

  public final Argument chmodArg;
  public final Argument permissionsArg;
  public final Permission permissions;

  public Chmod(@Nullable Argument chmodArg, @Nullable Argument permissionsArg, String permissions) {
    this.chmodArg = chmodArg;
    this.permissionsArg = permissionsArg;
    this.permissions = parsePermissions(permissions);
  }

  private static Permission parsePermissions(String permissions) {
    Matcher matcher = PERMISSIONS_PATTERN.matcher(permissions);
    if (matcher.find()) {
      if (matcher.group("numeric") != null) {
        return Permission.fromNumeric(matcher.group("numeric"));
      } else {
        return Permission.fromAlphanumeric(matcher.group("alphanumeric"));
      }
    } else {
      return Permission.empty();
    }
  }

  public static List<Chmod> extractChmodsFromArguments(List<Argument> arguments) {
    List<Chmod> chmods = new ArrayList<>();
    List<String> argumentsStrings = arguments.stream()
      .map(arg -> ArgumentResolution.of(arg).value())
      .toList();

    List<Integer> chmodIndexes = findChmodIndexes(argumentsStrings);
    for (Integer chmodIndex : chmodIndexes) {
      Integer indexPermissions = skipOptions(chmodIndex, argumentsStrings);
      if (indexPermissions != null) {
        chmods.add(new Chmod(arguments.get(chmodIndex), arguments.get(indexPermissions), argumentsStrings.get(indexPermissions)));
      }
    }

    return chmods;
  }

  private static List<Integer> findChmodIndexes(List<String> arguments) {
    return IntStream.range(0, arguments.size())
      .filter(i -> "chmod".equals(arguments.get(i)))
      .boxed()
      .toList();
  }

  private static Integer skipOptions(int index, List<String> arguments) {
    do {
      index++;
    } while (index < arguments.size() && arguments.get(index) != null && CHMOD_OPTIONS_PATTERN.matcher(arguments.get(index)).matches());
    return index < arguments.size() ? index : null;
  }

  public boolean hasPermission(String right) {
    return permissions.rights.contains(right);
  }

  /**
   * Class dedicated to store permissions in the chmod way : <a href="https://linux.die.net/man/1/chmod">man chmod</a>
   */
  public static class Permission {
    private Permission() {
    }

    // Store permissions at the following format : <target>+<right>
    // Example : "u+w" -> user has write permission
    Set<String> rights = new HashSet<>();

    static Permission empty() {
      return new Permission();
    }

    public static Permission fromAlphanumeric(String alphanumerics) {
      Permission chmodRight = new Permission();
      for (String alphanumeric : alphanumerics.split(",")) {
        if (alphanumeric.contains("-")) {
          continue;
        }
        String[] split = alphanumeric.split("[+=]");
        chmodRight.addRights(split[0], split[1]);
      }
      return chmodRight;
    }

    public static Permission fromNumeric(String numeric) {
      Permission chmodRight = new Permission();
      numeric = ("0000" + numeric).substring(numeric.length());
      chmodRight.addSet(numeric.charAt(0));
      chmodRight.addRight(numeric.charAt(1), 'u');
      chmodRight.addRight(numeric.charAt(2), 'g');
      chmodRight.addRight(numeric.charAt(3), 'o');
      return chmodRight;
    }

    private void addRight(char digit, char target) {
      int value = digit - '0';
      addIfFlag(target + "+r", value, 0b100);
      addIfFlag(target + "+w", value, 0b010);
      addIfFlag(target + "+x", value, 0b001);
    }

    private void addSet(char digit) {
      int value = digit - '0';
      addIfFlag("u+s", value, 0b100);
      addIfFlag("g+s", value, 0b010);
      addIfFlag("+t", value, 0b001);
    }

    private void addIfFlag(String right, int number, int flag) {
      if ((number & flag) > 0) {
        rights.add(right);
      }
    }

    private void addRights(String targets, String rights) {
      if (targets.isEmpty() || targets.equals("a")) {
        targets = "ugo";
      }

      for (char target : targets.toCharArray()) {
        for (char right : rights.toCharArray()) {
          this.rights.add(target + "+" + right);
        }
      }
    }

    public boolean hasRight(String right) {
      return rights.contains(right);
    }
  }
}
