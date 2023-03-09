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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.tree.api.AddInstruction;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.CopyInstruction;
import org.sonar.iac.docker.tree.api.Flag;
import org.sonar.iac.docker.utils.ArgumentUtils;

import static org.sonar.iac.docker.utils.ArgumentUtils.ArgumentResolution.Status.RESOLVED;

@Rule(key = "S6470")
public class DirectoryCopySourceCheck implements IacCheck {

  private static final String MESSAGE_CURRENT_OR_ROOT = "%s recursively might inadvertently add sensitive data to the container. Make sure it is safe here.";
  private static final String MESSAGE_GLOBBING = "%s using a glob pattern might inadvertently add sensitive data to the container. Make sure it is safe here.";
  private static final Pattern WINDOWS_DRIVE_PATTERN = Pattern.compile("^[a-zA-Z]:$");

  @Override
  public void initialize(InitContext init) {
    init.register(AddInstruction.class, DirectoryCopySourceCheck::checkAdd);
    init.register(CopyInstruction.class, DirectoryCopySourceCheck::checkCopy);
  }

  private static void checkAdd(CheckContext ctx, AddInstruction add) {
    for (Argument src : add.srcs()) {
      ArgumentUtils.ArgumentResolution resolution = ArgumentUtils.resolve(src);
      String path = ArgumentUtils.resolve(src).value();
      if (resolution.is(RESOLVED) && path != null && !path.startsWith("http://") && !path.startsWith("https://")) {
        reportIfSensitive(ctx, src, isSensitivePath(path), "Adding files");
      }
    }
  }

  private static void checkCopy(CheckContext ctx, CopyInstruction copyInstruction) {
    if (hasFromOption(copyInstruction.options())) return;

    for (Argument src : copyInstruction.srcs()) {
      ArgumentUtils.ArgumentResolution resolution = ArgumentUtils.resolve(src);
      String path = ArgumentUtils.resolve(src).value();
      if (resolution.is(RESOLVED) && path != null) {
        reportIfSensitive(ctx, src, isSensitivePath(path), "Copying");
      }
    }
  }

  private static boolean hasFromOption(List<Flag> options) {
    return options.stream().anyMatch(param -> param.name().equals("from"));
  }

  private static void reportIfSensitive(CheckContext ctx, Argument src, PathSensitivity sensitivity, String messagePrefix) {
    if(sensitivity == PathSensitivity.ROOT_OR_CURRENT) {
      ctx.reportIssue(src, String.format(MESSAGE_CURRENT_OR_ROOT, messagePrefix));
    } else if (sensitivity == PathSensitivity.TOP_LEVEL_GLOBBING) {
      ctx.reportIssue(src, String.format(MESSAGE_GLOBBING, messagePrefix));
    }
  }

  private enum PathSensitivity {
    SAFE, ROOT_OR_CURRENT, TOP_LEVEL_GLOBBING
  }

  /**
   * Indicate if a path is sensitive. A path is sensitive if any of the current condition is true :
   * <ul>
   * <li>if the provided path resolve to a root level or to the current directory</li>
   * <li>if the top level entry end with a wildcard and there is no level below</li>
   * </ul>
   * Examples of root level : '/' ; '/test/..' ; 'c:/' ; 'c:/test/..'
   * Examples of current level : '.' ; './test/..'
   * Examples of top level entry ending with wildcard and nothing behind : 'a*' ; './a*' ; '/a*' ; './test/../a*'
   */
  private static PathSensitivity isSensitivePath(String path) {
    String[] levels = normalize(path);
    if (levels.length == 0) return PathSensitivity.ROOT_OR_CURRENT;
    if (levels.length == 1 && (isRootOrCurrent(levels[0]))) return PathSensitivity.ROOT_OR_CURRENT;
    int topLevel = getLevelToCheckIndex(levels);
    if (levels[topLevel].endsWith("*") && levels.length == topLevel+1) return PathSensitivity.TOP_LEVEL_GLOBBING;
    return PathSensitivity.SAFE;
  }

  private static boolean isRootOrCurrent(String level) {
    return (level.isEmpty() || ".".equals(level) || WINDOWS_DRIVE_PATTERN.matcher(level).find());
  }

  /**
   * Provide the index of the level that must be check in regard of if it is ending with a wildcard.
   * The purpose is to skip the first level if it corresponds to a root or current folder representation.
   * Examples :
   * - will return index 0 : test, test/test2
   * - will return index 1 : ./a, /a, c:/a
   */
  private static int getLevelToCheckIndex(String[] levels) {
    if (isRootOrCurrent(levels[0])) return 1;
    return 0;
  }

  static String[] normalize(String path) {
    Deque<String> levels = new ArrayDeque<>();
    for (String current : path.split("/")) {
      if ("..".equals(current) && !levels.isEmpty()) {
        levels.removeLast();
      } else if (current.isEmpty()) {
        if (levels.isEmpty()) {
          levels.add(current);
        }
      } else if (".".equals(current) && levels.isEmpty()) {
        levels.add(current);
      } else if (!".".equals(current)) {
        levels.add(current);
      }
    }
    return levels.toArray(new String[] {});
  }
}
