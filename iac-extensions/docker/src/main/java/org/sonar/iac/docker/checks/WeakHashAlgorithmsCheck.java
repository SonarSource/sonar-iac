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
      .forEach(cmd -> ctx.reportIssue(cmd, MESSAGE));
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
   * Class to represent a bash/shell/powershell instruction.
   * <pre>
   *   {@link #executable}? ({@link #options} | {@link #parameters})*
   * </pre>
   */
  private static class Command implements HasTextRange {

    private static final Set<String> SEPARATOR = Set.of("&&", "|", ";");

    private final String executable;
    private final Map<String, String> options = new HashMap<>();
    private final List<String> parameters = new ArrayList<>();
    private final TextRange textRange;

    /**
     * Point of entry of the class, transform a list of {@link Argument} into a list of {@link #Command}, separating them using {@link #SEPARATOR}.
     */
    public static List<Command> parse(List<Argument> arguments) {
      return splitArgumentsPerGroup(arguments).stream()
        .map(Command::new)
        .collect(Collectors.toList());
    }

    /**
     * Divide the provided list of string per separator into sublist.
     * The sublists are wrapped up in CommandArguments to keep the original arguments with them.
     * <pre>
     *   ["exe1", "param1", "&&", "exe2", "param2"]
     *   -> [["exe1", "param1"], ["exe2", "param2"]]
     * </pre>
     */
    private static List<CommandArguments> splitArgumentsPerGroup(List<Argument> arguments) {
      List<String> resolvedArguments = resolveArguments(arguments);
      List<Integer> indexesSeparator = new ArrayList<>();
      indexesSeparator.add(-1);
      for (int i = 0; i < resolvedArguments.size(); i++) {
        if (SEPARATOR.contains(resolvedArguments.get(i))) {
          indexesSeparator.add(i);
        }
      }
      return IntStream.range(0, indexesSeparator.size())
        .mapToObj(i -> createCommandArguments(arguments, resolvedArguments, indexesSeparator, i))
        .collect(Collectors.toList());
    }

    private static CommandArguments createCommandArguments(List<Argument> arguments, List<String> resolvedArguments, List<Integer> indexesSeparator, int index) {
      int indexFrom = indexesSeparator.get(index) + 1;
      int indexTo = index + 1 < indexesSeparator.size() ? indexesSeparator.get(index + 1) : arguments.size();
      return new CommandArguments(arguments.subList(indexFrom, indexTo), resolvedArguments.subList(indexFrom, indexTo));
    }

    private static List<String> resolveArguments(List<Argument> arguments) {
      return arguments.stream()
        .map(arg -> ArgumentResolution.of(arg).value())
        .collect(Collectors.toList());
    }

    public Command(CommandArguments elements) {
      this.executable = elements.resolvedArguments.isEmpty() ? null : elements.resolvedArguments.get(0);
      this.textRange = TextRanges.mergeRanges(elements.arguments);

      elements.resolvedArguments.stream()
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

    @Override
    public TextRange textRange() {
      return this.textRange;
    }

    private static class CommandArguments {
      private final List<String> resolvedArguments;
      private final List<Argument> arguments;

      public CommandArguments(List<Argument> arguments, List<String> resolvedArguments) {
        this.arguments = arguments;
        this.resolvedArguments = resolvedArguments;
      }
    }
  }
}
