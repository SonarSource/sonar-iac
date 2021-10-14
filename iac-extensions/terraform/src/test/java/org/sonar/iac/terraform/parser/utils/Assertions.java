/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.iac.terraform.parser.utils;

import com.sonar.sslr.api.RecognitionException;
import com.sonar.sslr.api.Rule;
import com.sonar.sslr.api.typed.ActionParser;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;
import org.fest.assertions.GenericAssert;
import org.sonar.api.batch.fs.internal.DefaultTextPointer;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.parser.HclNodeBuilder;
import org.sonar.iac.terraform.parser.TreeFactory;
import org.sonar.iac.terraform.parser.grammar.HclGrammar;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;
import org.sonar.iac.terraform.tree.impl.TerraformTreeImpl;
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.grammar.LexerlessGrammarBuilder;
import org.sonar.sslr.tests.ParsingResultComparisonFailure;
import org.sonar.sslr.tests.RuleAssert;
import org.sonarsource.analyzer.commons.TokenLocation;

public class Assertions {

  public static RuleAssert assertThat(Rule actual) {
    return new RuleAssert(actual);
  }

  public static ParserAssert assertThat(GrammarRuleKey rule) {
    return assertThat(HclLexicalGrammar.createGrammarBuilder(), rule);
  }

  public static ParserAssert assertThat(LexerlessGrammarBuilder b, GrammarRuleKey rule) {
    return new ParserAssert(new ActionParser<>(
      StandardCharsets.UTF_8,
      b,
      HclGrammar.class,
      new TreeFactory(),
      new HclNodeBuilder(),
      rule));
  }

  public static class ParserAssert extends GenericAssert<ParserAssert, ActionParser<TerraformTree>> {

    public ParserAssert(ActionParser<TerraformTree> actual) {
      super(ParserAssert.class, actual);
    }

    private void parseTillEof(String input) {
      TerraformTreeImpl tree = (TerraformTreeImpl) actual.parse(input);
      TokenLocation loc = new TokenLocation(1, 0, input);
      if (!tree.textRange().end().equals(new DefaultTextPointer(loc.endLine(), loc.endLineOffset()))) {
        throw new RecognitionException(
          0, "Did not match till EOF, but till line " + tree.textRange().end().line());
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
