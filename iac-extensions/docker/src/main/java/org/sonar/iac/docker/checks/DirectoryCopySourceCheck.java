/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.tree.api.AddTree;
import org.sonar.iac.docker.tree.api.CopyTree;
import org.sonar.iac.docker.tree.api.ParamTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;
import org.sonar.iac.docker.utils.SyntaxTokenUtils;

@Rule(key = "S6470")
public class DirectoryCopySourceCheck implements IacCheck {

  private static final String MESSAGE_CURRENT_OR_ROOT = "Make sure that recursively copying directories is safe here.";
  private static final String MESSAGE_GLOBING = "Make sure that using globbing in a %s source is safe here.";

  @Override
  public void initialize(InitContext init) {
    init.register(AddTree.class, DirectoryCopySourceCheck::checkAdd);
    init.register(CopyTree.class, DirectoryCopySourceCheck::checkCopy);
  }

  private static void checkAdd(CheckContext ctx, AddTree add) {
    for (SyntaxToken src : add.srcs()) {
      String path = SyntaxTokenUtils.sanitize(src);
      if (!path.startsWith("http://") && !path.startsWith("https://")) {
        reportIfSensitive(ctx, src, isSensitivePath(path), "ADD");
      }
    }
  }

  private static void checkCopy(CheckContext ctx, CopyTree copy) {
    if (hasOption(copy.options(), "from")) return;

    for (SyntaxToken src : copy.srcs()) {
      reportIfSensitive(ctx, src, isSensitivePath(SyntaxTokenUtils.sanitize(src)), "COPY");
    }
  }

  private static boolean hasOption(List<ParamTree> options, String key) {
    return options.stream().anyMatch(param -> param.name().equals(key));
  }

  private static void reportIfSensitive(CheckContext ctx, SyntaxToken src, PathSensitivity sensitivity, String instructionName) {
    if(sensitivity == PathSensitivity.ROOT_OR_CURRENT) {
      ctx.reportIssue(src, MESSAGE_CURRENT_OR_ROOT);
    } else if (sensitivity == PathSensitivity.TOP_LEVEL_GLOBBING) {
      ctx.reportIssue(src, String.format(MESSAGE_GLOBING, instructionName));
    }
  }

  enum PathSensitivity {
    SAFE, ROOT_OR_CURRENT, TOP_LEVEL_GLOBBING
  }

  /**
   * Indicate if a path is sensitive. A path is sensitive if any of the current condition is true :
   * - if the provided path resolve to a root level or to the current directory
   * - if there is any top level entry (level <= 1) ending with a wildcard *
   * Examples of root level : '/' ; '/test/..' ; 'c:/' ; 'c:/test/..'
   * Examples of current level : '.' ; './test/..'
   * Examples of top level entry ending with wildcard : 'a*' ; './a*' ; '/a*' ; './test/../a*'
   */
  private static PathSensitivity isSensitivePath(String path) {
    String[] splitted = path.split("/");
    String regexWindowsDrive = "^[a-zA-Z]:$";
    boolean isAbsolutePath = splitted.length == 0 || splitted[0].isEmpty() || splitted[0].matches(regexWindowsDrive);
    int level = 0;
    boolean hasTopLevelWithWildcard = false;

    for (int i = 0; i < splitted.length; i++) {
      if (splitted[i].equals("..")) {
        level--;
      } else if (!splitted[i].equals(".") && !splitted[i].isEmpty() && (i > 0 || !splitted[i].matches(regexWindowsDrive))) {
        level++;
      }

      if (level <= 1 && splitted[i].endsWith("*")) {
        hasTopLevelWithWildcard = true;
      }

      if (level < 0) {
        if (isAbsolutePath) {
          // in case of absolute path, we can't go above root, so we reset to root
          level = 0;
        } else {
          // in case of relative path, if we go above the current folder, we suppose we lose the track and can't report any issue
          return PathSensitivity.SAFE;
        }
      }
    }

    if (level == 0) return PathSensitivity.ROOT_OR_CURRENT;
    if (level == 1 && hasTopLevelWithWildcard) return PathSensitivity.TOP_LEVEL_GLOBBING;
    return PathSensitivity.SAFE;
  }
}
