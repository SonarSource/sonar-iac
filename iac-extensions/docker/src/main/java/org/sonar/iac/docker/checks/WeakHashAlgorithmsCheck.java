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
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.RunInstruction;

@Rule(key = "S4790")
public class WeakHashAlgorithmsCheck implements IacCheck {

  private static final String MESSAGE = "Using weak hashing algorithms is security-sensitive.";
  private static final Set<String> OPENSSL_SENSITIVE_SUBCOMMAND = Set.of("md5", "sha1", "rmd160", "ripemd160");
  private static final Set<String> OPENSSL_SENSITIVE_DGST_OPTION  = Set.of("-md2", "-md4", "-md5", "-sha1", "-ripemd160", "-ripemd", "-rmd160");

  private static final CommandDetector sensitiveOpenSslSubcommand = CommandDetector.builder()
    .with("openssl"::equals)
    .with(OPENSSL_SENSITIVE_SUBCOMMAND::contains)
    .build();
  private static final CommandDetector sensitiveOpenSslDgst = CommandDetector.builder()
    .with("openssl"::equals)
    .with("dgst"::equals)
    .withOptional(s -> !OPENSSL_SENSITIVE_DGST_OPTION.contains(s) && s.startsWith("-"))
    .with(OPENSSL_SENSITIVE_DGST_OPTION::contains)
    .build();

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, WeakHashAlgorithmsCheck::checkRun);
  }

  private static void checkRun(CheckContext ctx, RunInstruction runInstruction) {
    List<ArgumentResolution> resolvedArgument = runInstruction.arguments().stream().map(ArgumentResolution::of).collect(Collectors.toList());

    sensitiveOpenSslSubcommand.search(resolvedArgument).forEach(command -> ctx.reportIssue(command, MESSAGE));
    sensitiveOpenSslDgst.search(resolvedArgument).forEach(command -> ctx.reportIssue(command, MESSAGE));
  }

  static class Command implements HasTextRange {
    List<ArgumentResolution> resolvedArguments;

    public Command(List<ArgumentResolution> resolvedArguments) {
      this.resolvedArguments = resolvedArguments;
    }

    @Override
    public TextRange textRange() {
      return TextRanges.mergeElementsWithTextRange(resolvedArguments.stream().map(ArgumentResolution::argument).collect(Collectors.toList()));
    }
  }

  private static class CommandDetector {

    List<CommandPredicate> predicates;

    private CommandDetector(List<CommandPredicate> predicates) {
      this.predicates = predicates;
    }

    public static Builder builder() {
      return new Builder();
    }

    /**
     * Return all block of arguments which match the list of predicates.
     */
    private List<Command> search(List<ArgumentResolution> resolvedArguments) {
      List<Command> commands = new ArrayList<>();

      for (int argIndex = 0; argIndex < resolvedArguments.size(); argIndex++) {
        Integer sizeCommand = fullMatch(resolvedArguments, argIndex);
        if (sizeCommand != null) {
          commands.add(new Command(resolvedArguments.subList(argIndex, argIndex + sizeCommand)));
          argIndex += sizeCommand - 1;
        }
      }
      return commands;
    }

    /**
     * If the provided list of resolved argument match with the list of predicates, return the size of matched predicates, ignoring optional predicates that didn't match.
     * Otherwise, it will return null.
     */
    @CheckForNull
    private Integer fullMatch(List<ArgumentResolution> resolvedArgument, int argIndex) {
      int sizeCommand = 0;
      for (CommandPredicate commandPredicate : predicates) {
        if (resolvedArgument.size() <= argIndex + sizeCommand) {
          return null;
        }

        String argValue = resolvedArgument.get(argIndex + sizeCommand).value();
        if (commandPredicate.predicate.test(argValue)) {
          sizeCommand++;
        } else if (!commandPredicate.optional) {
          return null;
        }
      }
      return sizeCommand;
    }

    public static class Builder {

      List<CommandPredicate> predicates = new ArrayList<>();

      private void addPredicate(Predicate<String> predicate, boolean optional) {
        predicates.add(new CommandPredicate(predicate, optional));
      }

      public Builder with(Predicate<String> predicate) {
        addPredicate(predicate, false);
        return this;
      }

      public Builder withOptional(Predicate<String> predicate) {
        addPredicate(predicate, true);
        return this;
      }

      public CommandDetector build() {
        return new CommandDetector(predicates);
      }
    }

    static class CommandPredicate {
      Predicate<String> predicate;
      boolean optional;

      public CommandPredicate(Predicate<String> predicate, boolean optional) {
        this.predicate = predicate;
        this.optional = optional;
      }
    }
  }
}
