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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.docker.tree.api.ParamTree;
import org.sonar.iac.docker.tree.api.RunTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

@Rule(key = "S6469")
public class MountWorldPermissionCheck implements IacCheck {

  private static final String MESSAGE = "Remove world permissions for this sensitive %s.";
  private static final Map<String, String> SENSITIVE_MOUNT_TYPE_WITH_DENOMINATION = Map.of(
    "secret", "file",
    "ssh", "agent"
  );

  @Override
  public void initialize(InitContext init) {
    init.register(RunTree.class, (ctx, run) ->
      getOptionByName(run.options(), "mount")
        .map(MountWorldPermissionCheck::parseOption)
        .ifPresent(mount -> {
          String type = mount.get("type");
          String mode = mount.get("mode");
          if (type != null) {
            String denomination = SENSITIVE_MOUNT_TYPE_WITH_DENOMINATION.get(type);
            if (denomination != null && mode != null && isModeSensitive(mode)) {
              ctx.reportIssue(run, String.format(MESSAGE, denomination));
            }
          }
        })
    );
  }

  private static Optional<ParamTree> getOptionByName(@Nullable List<ParamTree> options, String optionName) {
    if (options == null) {
      return Optional.empty();
    } else {
      return options.stream().filter(option -> optionName.equals(option.name())).findFirst();
    }
  }

  /**
   * Parse a String which contain multiple key=value elements separated by comma : type=secret,id=foo,mode=0600,required
   * For elements without any '=value', the associated value will be null.
   */
  private static Map<String, String> parseOption(ParamTree option) {
    Map<String, String> result = new HashMap<>();
    SyntaxToken optionValue = option.value();
    if (optionValue != null) {
      TextRange range = optionValue.textRange();
      new DefaultTextRange(new );
      for (String value : optionValue.value().split(",")) {
        String[] values = value.split("=", 2);
        if (values.length == 2) {
          result.put(values[0], values[1]);
        } else {
          result.put(values[0], null);
        }
      }
    }
    return result;
  }

  /**
   * Check if a mode is sensitive. A mose is sensitive if the world permission if different that none.
   * Concretely, it is simply checking that the last character is a digit which is not 0.
   */
  private static boolean isModeSensitive(String mode) {
    char lastChar = mode.charAt(mode.length()-1);
    return lastChar >= '1' && lastChar <= '9';
  }
}
