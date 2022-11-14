package org.sonar.iac.docker.parser.utils;

import com.sonar.sslr.api.RecognitionException;
import com.sonar.sslr.api.Rule;
import com.sonar.sslr.api.typed.ActionParser;
import javax.annotation.Nullable;
import org.fest.assertions.GenericAssert;
import org.sonar.api.batch.fs.internal.DefaultTextPointer;
import org.sonar.iac.docker.parser.DockerParser;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.impl.DockerTreeImpl;
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.grammar.LexerlessGrammarBuilder;
import org.sonar.sslr.tests.ParsingResultComparisonFailure;
import org.sonar.sslr.tests.RuleAssert;
import org.sonarsource.analyzer.commons.TokenLocation;

public class DockerAssertions {

  public static RuleAssert assertThat(Rule actual) {
    return new RuleAssert(actual);
  }

  public static ParserAssert assertThat(GrammarRuleKey rule) {
    return assertThat(DockerLexicalGrammar.createGrammarBuilder(), rule);
  }

  public static ParserAssert assertThat(LexerlessGrammarBuilder b, GrammarRuleKey rule) {
    return new ParserAssert(new DockerParser(rule));
  }

  public static class ParserAssert extends GenericAssert<ParserAssert, ActionParser<DockerTree>> {

    public ParserAssert(ActionParser<DockerTree> actual) {
      super(ParserAssert.class, actual);
    }

    private void parseTillEof(String input) {
      DockerTreeImpl tree = (DockerTreeImpl) actual.parse(input);
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
