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
package org.sonar.iac.docker.parser.utils;

import com.sonar.sslr.api.RecognitionException;
import com.sonar.sslr.api.Rule;
import com.sonar.sslr.api.typed.ActionParser;
import javax.annotation.Nullable;
import org.fest.assertions.GenericAssert;
import org.sonar.api.batch.fs.internal.DefaultTextPointer;
import org.sonar.iac.docker.parser.DockerParser;
import org.sonar.iac.docker.parser.grammar.DockerKeyword;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.impl.AbstractDockerTreeImpl;
import org.sonar.sslr.tests.ParsingResultComparisonFailure;
import org.sonar.sslr.tests.RuleAssert;
import org.sonarsource.analyzer.commons.TokenLocation;

public class Assertions {

  public static RuleAssert assertThat(Rule actual) {
    return new RuleAssert(actual);
  }

  public static ParserAssert assertThat(DockerLexicalGrammar rule) {
    return new ParserAssert(DockerParser.create(rule));
  }

  /**
   * In most cases you need {@link #assertThat(DockerLexicalGrammar)} method.
   * <p>
   * This one is added to avoid mistakes like passing {@link DockerKeyword} and expected {@link DockerTree}
   * instead of {@link org.sonar.iac.docker.tree.api.SyntaxToken}.
   */
  public static ParserAssert assertKeyword(DockerKeyword rule) {
    return new ParserAssert(DockerParser.create(rule));
  }

  public static class ParserAssert extends GenericAssert<ParserAssert, ActionParser<DockerTree>> {

    public ParserAssert(ActionParser<DockerTree> actual) {
      super(ParserAssert.class, actual);
    }

    private void parseTillEof(String input) {
      AbstractDockerTreeImpl tree = (AbstractDockerTreeImpl) actual.parse(input);
      TokenLocation loc = new TokenLocation(1, 0, input);
      if (!tree.textRange().end().equals(new DefaultTextPointer(loc.endLine(), loc.endLineOffset()))) {
        throw new RecognitionException(
          0, "Did not match till EOF, but till line " + tree.textRange().end().line() + " line offset: " + tree.textRange().end().lineOffset());
      }
    }

    public ParserAssert matches(String input) {
      isNotNull();
      checkArgument(!hasTrailingWhitespaces(input), "Trailing whitespaces in input are not supported");
      String expected = "Rule '" + getRuleName() + "' should match:\n" + input;
      try {
        parseTillEof(input);
      } catch (RecognitionException e) {
        String actual = e.getMessage();
        throw new ParsingResultComparisonFailure(expected, actual);
      }
      return this;
    }

    public static void checkArgument(boolean expression, @Nullable Object errorMessage) {
      if (!expression) {
        throw new IllegalArgumentException(String.valueOf(errorMessage));
      }
    }

    private static boolean hasTrailingWhitespaces(String input) {
      return input.endsWith(" ") || input.endsWith("\n") || input.endsWith("\r") || input.endsWith("\t");
    }

    public ParserAssert notMatches(String input) {
      isNotNull();
      try {
        parseTillEof(input);
      } catch (RecognitionException e) {
        // expected
        return this;
      }
      throw new AssertionError("Rule '" + getRuleName() + "' should not match:\n" + input);
    }

    private String getRuleName() {
      return actual.rootRule().toString();
    }

  }

}
