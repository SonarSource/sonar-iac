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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.RunInstruction;

@Rule(key = "S4790")
public class WeakHashAlgorithmsCheck implements IacCheck {

  private static final String MESSAGE = "Using weak hashing algorithms is security-sensitive.";
  private static final Set<String> OPENSSL_SENSITIVE_SUBCOMMAND = Set.of("md5", "sha1", "rmd160", "ripemd160");
  private static final Set<String> OPENSSL_SENSITIVE_DGST_OPTION  = Set.of("md2", "md4", "md5", "sha1", "ripemd160", "ripemd", "rmd160");

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, WeakHashAlgorithmsCheck::checkRun);
  }

  private static void checkRun(CheckContext ctx, RunInstruction runInstruction) {
    Command.parse(runInstruction.arguments()).stream()
      .filter(WeakHashAlgorithmsCheck::isOpenSslCallSensitive)
      .forEach(cmd -> ctx.reportIssue(cmd.textRange, MESSAGE));
  }

  private static boolean isOpenSslCallSensitive(Command command) {
    return "openssl".equals(command.executable) && !command.parameters.isEmpty()
      && (isOpenSslSensitiveSubCommand(command) || isOpenSslSensitiveDgstSubCommand(command));
  }

  private static boolean isOpenSslSensitiveSubCommand(Command command) {
    return OPENSSL_SENSITIVE_SUBCOMMAND.contains(command.parameters.get(0));
  }

  private static boolean isOpenSslSensitiveDgstSubCommand(Command command) {
    return "dgst".equals(command.parameters.get(0))
      && OPENSSL_SENSITIVE_DGST_OPTION.stream().anyMatch(command.options::containsKey);
  }

  /**
   * This class represent a bash/shell/powershell instruction.
   * <pre>
   *   {@link #executable}? ({@link #options} | {@link #parameters})*
   * </pre>
   */
  private static class Command {

    private static final Set<String> SEPARATOR = Set.of("&&", "|");

    private final String executable;
    private final Map<String, String> options;
    private final List<String> parameters;
    private final TextRange textRange;

    /**
     * This is the point of entry of the class, allowing to transform a list of {@link Argument} into a list of {@link #Command}, separating them using {@link #SEPARATOR}.
     */
    public static List<Command> parse(List<Argument> arguments) {
      return splitArgumentsPerGroup(arguments).stream()
        .map(Command::new)
        .collect(Collectors.toList());
    }

    /**
     * Divide the provided list of string per separator into sublist.
     * The sublist are wrapped up in GroupStringWithTextRange to keep the original text range with them.
     * <pre>
     *   ["exe1", "param1", "&&", "exe2", "param2"]
     *   -> [["exe1", "param1"], ["exe2", "param2"]]
     * </pre>
     */
    private static List<GroupStringWithTextRange> splitArgumentsPerGroup(List<Argument> arguments) {
      List<String> resolvedArguments = resolveArguments(arguments);
      int[] indexesSeparator = Stream.of(IntStream.of(-1),
          IntStream.range(0, resolvedArguments.size()).filter(i -> SEPARATOR.contains(resolvedArguments.get(i)))
        ).flatMapToInt(s-> s).toArray();
      return IntStream.range(0, indexesSeparator.length)
        .mapToObj(i -> {
          int indexFrom = indexesSeparator[i]+1;
          int indexTo = i + 1 < indexesSeparator.length ? indexesSeparator[i + 1] : arguments.size();
          return new GroupStringWithTextRange(arguments.subList(indexFrom, indexTo), resolvedArguments.subList(indexFrom, indexTo));
        })
        .collect(Collectors.toList());
    }

    private static List<String> resolveArguments(List<Argument> arguments) {
      return arguments.stream()
        .map(arg -> ArgumentResolution.of(arg).value())
        .collect(Collectors.toList());
    }

    public Command(GroupStringWithTextRange elements) {
      this.executable = elements.strings.isEmpty() ? null : elements.strings.get(0);
      this.options = new HashMap<>();
      this.parameters = new ArrayList<>();
      this.textRange = elements.textRange;

      elements.strings.stream()
        .skip(1)
        .forEach(this::processElement);
    }

    private void processElement(@Nullable String element) {
      if (element != null && element.startsWith("-")) {
        addOption(element);
      } else {
        this.parameters.add(element);
      }
    }

    private void addOption(String option) {
      option = option.replaceAll("^-++", "");
      String[] split = option.split("=", 2);
      if (split.length == 2) {
        options.put(split[0], split[1]);
      } else {
        options.put(split[0], null);
      }
    }

    private static class GroupStringWithTextRange {
      private final List<String> strings;
      private final TextRange textRange;

      public GroupStringWithTextRange(List<Argument> arguments, List<String> resolvedArguments) {
        this.textRange = arguments.isEmpty() ? null : TextRanges.merge(arguments.stream().map(HasTextRange::textRange).collect(Collectors.toList()));
        this.strings = resolvedArguments;
      }
    }
  }
}
