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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.RunInstruction;
import org.sonar.iac.docker.tree.api.TransferInstruction;
import org.sonar.iac.docker.utils.ArgumentUtils;

@Rule(key = "S2612")
public class PosixPermissionCheck implements IacCheck {

  private static final String MESSAGE = "Make sure this permission is safe.";

  private static final Pattern CHMOD_OPTIONS_PATTERN = Pattern.compile("-[a-zA-Z]|--[a-zA-Z-]++");
  private static final String NUMERIC = "(?<numeric>[0-7]{1,4})";
  private static final String ALPHANUMERIC = "[ugoa]*+[=+-][rwxXstugo]++";
  private static final String ALPHANUMERICS = "(?<alphanumeric>(?:" + ALPHANUMERIC + ",?+)++)";
  private static final Pattern PERMISSIONS_PATTERN = Pattern.compile(NUMERIC + "|" + ALPHANUMERICS);

  @Override
  public void initialize(InitContext init) {
    init.register(TransferInstruction.class, PosixPermissionCheck::checkTransferChmodPermission);
    init.register(RunInstruction.class, PosixPermissionCheck::checkRunChmodPermission);
  }

  /**
   * Methods to check RunInstruction permissions
   */
  private static void checkRunChmodPermission(CheckContext ctx, RunInstruction runInstruction) {
    for (Chmod chmod : listChmods(runInstruction)) {
      if (chmod.hasPermission("o+w") || chmod.hasPermission("g+s") || chmod.hasPermission("u+s")) {
        TextRange textRange = TextRanges.merge(List.of(chmod.chmodArg.textRange(), chmod.permissionsArg.textRange()));
        ctx.reportIssue(textRange, MESSAGE);
      }
    }
  }

  private static List<Chmod> listChmods(RunInstruction runInstruction) {
    List<Chmod> chmods = new ArrayList<>();
    List<String> arguments = runInstruction.arguments().stream()
      .map(arg -> ArgumentUtils.resolve(arg).value())
      .collect(Collectors.toList());

    List<Integer> chmodIndexes = findChmodIndexes(arguments);
    for (Integer chmodIndex : chmodIndexes) {
      Integer indexPermissions = skipOptions(chmodIndex, arguments);
      if (indexPermissions != null) {
        chmods.add(new Chmod(runInstruction.arguments().get(chmodIndex), runInstruction.arguments().get(indexPermissions), arguments.get(indexPermissions)));
      }
    }

    return chmods;
  }

  private static List<Integer> findChmodIndexes(List<String> arguments) {
    return IntStream.range(0, arguments.size())
      .filter(i -> "chmod".equals(arguments.get(i)))
      .boxed()
      .collect(Collectors.toList());
  }

  private static Integer skipOptions(int index, List<String> arguments) {
    do {
      index++;
    } while (index < arguments.size() && arguments.get(index) != null && CHMOD_OPTIONS_PATTERN.matcher(arguments.get(index)).matches());
    return index < arguments.size() ? index : null;
  }

  /**
   * Methods to check TransferInstruction (ADD/COPY) permissions
   */
  private static void checkTransferChmodPermission(CheckContext ctx, TransferInstruction transferInstruction) {
    transferInstruction.options().stream()
      .filter(flag -> flag.name().equals("chmod"))
      .filter(flag -> isPermissionSensitive(ArgumentUtils.resolve(flag.value()).value()))
      .forEach(flag -> ctx.reportIssue(flag, MESSAGE));
  }

  private static boolean isPermissionSensitive(@Nullable String permissionString) {
    if (permissionString == null) {
      return false;
    }
    Chmod.Permission permissions = Chmod.Permission.fromNumeric(permissionString);
    return permissions.rights.contains("o+w");
  }

  /**
   * Represent chmod call instruction in RUN Arguments, with parsed permissions ready to be checked
   */
  static class Chmod {
    Argument chmodArg;
    Argument permissionsArg;
    Permission permissions;

    public Chmod(Argument chmodArg, Argument permissionsArg, String permissions) {
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

    public boolean hasPermission(String right) {
      return permissions.rights.contains(right);
    }

    /**
     * Class dedicated to store permissions in the chmod way : <a href="https://linux.die.net/man/1/chmod">man chmod</a>
     */
    static class Permission {
      private Permission() {}

      // Store permissions at the following format : <target>+<right>
      // Example : "u+w" -> user has write permission
      Set<String> rights = new HashSet<>();

      static Permission empty() {
        return new Permission();
      }

      static Permission fromAlphanumeric(String alphanumerics) {
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

      static Permission fromNumeric(String numeric) {
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
        addIfFlag( "+t", value, 0b001);
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
    }
  }
}
