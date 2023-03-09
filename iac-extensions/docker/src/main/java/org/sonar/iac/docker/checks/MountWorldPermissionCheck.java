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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.CheckForNull;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.Flag;
import org.sonar.iac.docker.tree.api.RunInstruction;
import org.sonar.iac.docker.utils.CheckUtils;

@Rule(key = "S6469")
public class MountWorldPermissionCheck implements IacCheck {

  private static final String MESSAGE = "Remove world permissions for this sensitive %s.";

  private static final Pattern MOUNT_TYPE_PATTERN = Pattern.compile("type=(secret|ssh)");
  private static final Pattern MOUNT_MODE_PATTERN = Pattern.compile("mode=(\\d+)");



  private static final Map<String, String> DENOMINATION_BY_TYPE = Map.of(
    "secret", "file",
    "ssh", "agent"
  );

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, (ctx, run) ->
      CheckUtils.getParamByName(run.options(), "mount")
        .map(Flag::value)
        .ifPresent(mount -> checkMountParam(ctx, mount)));
  }

  private static void checkMountParam(CheckContext ctx, Argument mountOptions) {
    String value = ArgumentResolution.of(mountOptions).value();
    TextPointer start = mountOptions.textRange().start();
    MountOption type = MountOption.creatFromMatcher(MOUNT_TYPE_PATTERN.matcher(value), start);
    MountOption mode = MountOption.creatFromMatcher(MOUNT_MODE_PATTERN.matcher(value), start);

    if (type != null && mode != null && isModeSensitive(mode.value)) {
      ctx.reportIssue(mode.textRange, String.format(MESSAGE, DENOMINATION_BY_TYPE.get(type.value)));
    }
  }

  static class MountOption {
    final String value;
    final TextRange textRange;
    private MountOption(String value, TextRange textRange) {
      this.value = value;
      this.textRange = textRange;
    }

    @CheckForNull
    public static MountOption creatFromMatcher(Matcher matcher, TextPointer start) {
      if (matcher.find()) {
        int line = start.line();
        int startColumn = start.lineOffset() + matcher.start();
        int endColum = start.lineOffset() + matcher.end();
        String value = matcher.group(1);
        TextRange range = TextRanges.range(line, startColumn, line, endColum);
        return new MountOption(value, range);
      }
      return null;
    }
  }

  /**
   * Check if a mode is sensitive. A mose is sensitive if the world permission if different that none.
   * Concretely, it is simply checking that the last character is a digit which is not 0.
   */
  private static boolean isModeSensitive(String mode) {
    char lastChar = mode.charAt(mode.length()-1);
    return lastChar != '0';
  }
}
