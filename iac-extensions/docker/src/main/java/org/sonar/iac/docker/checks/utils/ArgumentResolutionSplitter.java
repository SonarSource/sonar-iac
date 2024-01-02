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
package org.sonar.iac.docker.checks.utils;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.iac.docker.checks.utils.command.SeparatedList;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.SyntaxToken;
import org.sonar.iac.docker.tree.impl.ArgumentImpl;
import org.sonar.iac.docker.tree.impl.LiteralImpl;
import org.sonar.iac.docker.tree.impl.SyntaxTokenImpl;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

public final class ArgumentResolutionSplitter {

  private static final String STRING_IN_DOUBLE_QUOTES = "\"(?:\\\\.|[^\"])*+\"";
  private static final String STRING_IN_SIMPLE_QUOTES = "'(?:\\\\.|[^'])*+'";
  private static final String NON_SEPARATOR_CHARACTER = "[^;&|]";
  private static final String COMMAND_WITHOUT_OPERATOR = "(?:" + STRING_IN_DOUBLE_QUOTES + "|" + STRING_IN_SIMPLE_QUOTES + "|" + NON_SEPARATOR_CHARACTER + ")*+";
  private static final String OPERATORS = "(?:;|&&|&|\\|\\||\\|)";

  private static final String FIRST_COMMAND = "firstCommand";
  private static final String REMAINDER = "remainder";
  /**
   * Validate that the provided input is a mix of commands separated by operators. Split them as firstCommand and the rest.
   */
  private static final Pattern FIRST_COMMAND_AND_REST_REGEX = Pattern.compile(
    "^(?<" + FIRST_COMMAND + ">" + COMMAND_WITHOUT_OPERATOR + ")(?<" + REMAINDER + ">(?:" + OPERATORS + COMMAND_WITHOUT_OPERATOR + ")++)$");

  private static final String OPERATOR = "operator";
  private static final String COMMAND = "command";
  /**
   * Parse repeating operators and commands in the rest part of the {@link #FIRST_COMMAND_AND_REST_REGEX}.
   */
  private static final Pattern OPERATOR_AND_COMMAND_REGEX = Pattern.compile(
    "(?<" + OPERATOR + ">" + OPERATORS + ")(?<" + COMMAND + ">" + COMMAND_WITHOUT_OPERATOR + ")");

  private ArgumentResolutionSplitter() {
  }

  /**
   * Split commands by separators: {@code &&}, {@code ||}, {@code &}, {@code |} and {@code ;}.
   */
  public static SeparatedList<List<ArgumentResolution>, String> splitCommands(Iterable<ArgumentResolution> resolvedArguments) {
    var separatedListBuilder = new CommandDetector.SeparatedListBuilder();

    for (ArgumentResolution resolvedArgument : resolvedArguments) {
      parseCommand(separatedListBuilder, resolvedArgument);
    }
    return separatedListBuilder.build();
  }

  private static void parseCommand(CommandDetector.SeparatedListBuilder separatedListBuilder, ArgumentResolution resolvedArgument) {
    String argument = resolvedArgument.value();
    var fullMatcher = FIRST_COMMAND_AND_REST_REGEX.matcher(argument);
    if (fullMatcher.find()) {
      String firstCommand = fullMatcher.group(FIRST_COMMAND);
      String remainder = fullMatcher.group(REMAINDER);
      if (!firstCommand.isBlank()) {
        ArgumentResolution newResolvedArg = buildSubArgument(resolvedArgument, firstCommand, 0);
        separatedListBuilder.addToCurrentCommand(newResolvedArg);
      }
      parseRemainderOfTheCommand(separatedListBuilder, resolvedArgument, fullMatcher, remainder);
    } else {
      separatedListBuilder.addToCurrentCommand(resolvedArgument);
    }
  }

  private static void parseRemainderOfTheCommand(
    CommandDetector.SeparatedListBuilder separatedListBuilder,
    ArgumentResolution resolvedArgument,
    Matcher fullMatcher,
    String remainder) {
    var matcher = OPERATOR_AND_COMMAND_REGEX.matcher(remainder);
    while (matcher.find()) {
      String operator = matcher.group(OPERATOR);
      String command = matcher.group(COMMAND);
      separatedListBuilder.addOperator(operator);
      if (!command.isBlank()) {
        ArgumentResolution newResolvedArg = buildSubArgument(resolvedArgument, command, fullMatcher.start(REMAINDER) + matcher.start(COMMAND));
        separatedListBuilder.addToCurrentCommand(newResolvedArg);
      }
    }
  }

  private static ArgumentResolution buildSubArgument(ArgumentResolution resolvedArgument, String firstCommand, int offsetShift) {
    var argumentRange = resolvedArgument.argument().textRange();
    SyntaxToken token = new SyntaxTokenImpl(firstCommand, range(argumentRange.start().line(), argumentRange.start().lineOffset() + offsetShift, firstCommand),
      Collections.emptyList());
    var literal = new LiteralImpl(token);
    token.setParent(literal);
    Argument newArg = new ArgumentImpl(List.of(literal));
    literal.setParent(newArg);
    // workaround to keep the quotes preservation logic of ArgumentResolution who checks if the parent is ShellForm
    newArg.setParent(resolvedArgument.argument().parent());
    return ArgumentResolution.ofWithoutStrippingQuotes(newArg);
  }
}
