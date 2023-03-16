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
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.RunInstruction;

@Rule(key = "S4790")
public class WeakHashAlgorithmsCheck implements IacCheck {

  private static final String MESSAGE = "Using weak hashing algorithms is security-sensitive.";
  private static final Set<String> OPENSSL_SENSITIVE_SUBCOMMAND = Set.of("md5", "sha1", "rmd160", "ripemd160");
  private static final Set<String> OPENSSL_SENSITIVE_DGST_OPTION  = Set.of("-md2", "-md4", "-md5", "-sha1", "-ripemd160", "-ripemd", "-rmd160");

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, WeakHashAlgorithmsCheck::checkRun);
  }

  private static void checkRun(CheckContext ctx, RunInstruction runInstruction) {
    CommandDetector sensitiveOpenSslSubcommand = CommandDetector.builder()
      .with("openssl"::equals)
      .with(OPENSSL_SENSITIVE_SUBCOMMAND::contains)
      .build();
    CommandDetector sensitiveOpenSslDgst = CommandDetector.builder()
      .with("openssl"::equals)
      .with("dgst"::equals)
      .withOptional(s -> !OPENSSL_SENSITIVE_DGST_OPTION.contains(s) && s.startsWith("-"))
      .with(OPENSSL_SENSITIVE_DGST_OPTION::contains)
      .build();

    sensitiveOpenSslSubcommand.search(runInstruction.arguments())
      .forEach(sensitiveOpensslCall -> ctx.reportIssue(TextRanges.mergeRanges(sensitiveOpensslCall), MESSAGE));
    sensitiveOpenSslDgst.search(runInstruction.arguments())
      .forEach(sensitiveOpensslCall -> ctx.reportIssue(TextRanges.mergeRanges(sensitiveOpensslCall), MESSAGE));
  }

  private static class CommandDetector {

    List<Predicate<String>> predicates;
    List<Boolean> isOptionalPredicates;

    private CommandDetector(List<Predicate<String>> predicates, List<Boolean> isOptionalPredicates) {
      this.predicates = predicates;
      this.isOptionalPredicates = isOptionalPredicates;
    }

    public static Builder builder() {
      return new Builder();
    }

    /**
     * Return all block of arguments which match the list of predicates.
     */
    private List<List<Argument>> search(List<Argument> arguments) {
      List<ArgumentResolution> resolvedArgument = arguments.stream().map(ArgumentResolution::of).collect(Collectors.toList());
      List<List<Argument>> argumentBlockIssue = new ArrayList<>();

      int argIndex = 0;
      while (argIndex < arguments.size()) {
        Integer sizeMatch = fullMatch(resolvedArgument, argIndex);
        if (sizeMatch != null) {
          argumentBlockIssue.add(arguments.subList(argIndex, argIndex + sizeMatch));
          argIndex += sizeMatch;
        } else {
          argIndex++;
        }
      }
      return argumentBlockIssue;
    }

    /**
     * If the provided list of resolved argument match with the list of predicates, return the size of matched predicates, ignoring optional predicates that didn't match.
     * Otherwise, it will return null.
     */
    @CheckForNull
    private Integer fullMatch(List<ArgumentResolution> resolvedArgument, int argIndex) {
      int nbMatched = 0;
      for (int predicateIndex = 0; predicateIndex < predicates.size(); predicateIndex++) {
        if (resolvedArgument.size() <= argIndex + nbMatched) {
          return null;
        }

        Predicate<String> predicate = predicates.get(predicateIndex);
        String argValue = resolvedArgument.get(argIndex + nbMatched).value();
        boolean isOptional = isOptionalPredicates.get(predicateIndex);

        if (predicate.test(argValue)) {
          nbMatched++;
        } else if (!isOptional) {
          return null;
        }
      }
      return nbMatched;
    }

    public static class Builder {

      List<Predicate<String>> predicates = new ArrayList<>();
      List<Boolean> isOptionalPredicates = new ArrayList<>();

      private void addPredicate(Predicate<String> predicate, boolean optional) {
        predicates.add(predicate);
        isOptionalPredicates.add(optional);
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
        return new CommandDetector(predicates, isOptionalPredicates);
      }
    }
  }
}
