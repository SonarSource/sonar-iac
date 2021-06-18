/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.common.testing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.HasComments;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.TreeContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonarsource.analyzer.commons.checks.verifier.SingleFileVerifier;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class Verifier {

  private Verifier() {
    // utility class
  }

  public static void verify(TreeParser<Tree> parser, Path path, IacCheck check) {
    Tree root = parse(parser, path);
    SingleFileVerifier verifier = createVerifier(path, root);
    runAnalysis(verifier, check, root);
    verifier.assertOneOrMoreIssues();
  }

  /**
   * This method should only be used if "Noncompliant" comments in the code cannot be used to verify the issues.
   */
  public static void verify(TreeParser<Tree> parser, Path path, IacCheck check, Issue... expectedIssues) {
    Tree root = parse(parser, path);
    List<Issue> actualIssues = runAnalysis(null, check, root);
    compare(actualIssues, Arrays.asList(expectedIssues));
  }

  public static void verifyNoIssue(TreeParser<Tree> parser, Path path, IacCheck check) {
    Tree root = parse(parser, path);
    List<Issue> actualIssues = runAnalysis(null, check, root);
    compare(actualIssues, Collections.emptyList());
  }

  private static List<Issue> runAnalysis(@Nullable SingleFileVerifier verifier, IacCheck check, Tree root) {
    TestContext ctx = new TestContext(verifier);
    check.initialize(ctx);
    ctx.scan(root);
    return ctx.raisedIssues;
  }

  private static Tree parse(TreeParser<Tree> parser, Path path) {
    String testFileContent = readFile(path);
    return parser.parse(testFileContent, null);
  }

  private static SingleFileVerifier createVerifier(Path path, Tree root) {

    SingleFileVerifier verifier = SingleFileVerifier.create(path, UTF_8);

    final Set<TextRange> alreadyAdded = new HashSet<>();
    (new TreeVisitor<>())
      .register(Tree.class, (ctx, tree) -> {
        if (tree instanceof HasComments && !alreadyAdded.contains(tree.textRange())) {
          for (Comment comment : ((HasComments) tree).comments()) {
            TextPointer start = comment.textRange().start();
            verifier.addComment(start.line(), start.lineOffset() + 1, comment.value(), 2, 0);
          }
          alreadyAdded.add(tree.textRange());
        }
      }).scan(new TreeContext(), root);

    return verifier;
  }

  private static String readFile(Path path) {
    try {
      return new String(Files.readAllBytes(path), UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot read " + path, e);
    }
  }

  private static class TestContext extends TreeContext implements InitContext, CheckContext {

    private final TreeVisitor<TestContext> visitor;
    private final SingleFileVerifier verifier;
    private final List<Issue> raisedIssues = new ArrayList<>();

    public TestContext(@Nullable SingleFileVerifier verifier) {
      this.verifier = verifier;
      visitor = new TreeVisitor<>();
    }

    public void scan(@Nullable Tree root) {
      visitor.scan(this, root);
    }

    @Override
    public <T extends Tree> void register(Class<T> cls, BiConsumer<CheckContext, T> consumer) {
      visitor.register(cls, (ctx, node) -> consumer.accept(this, node));
    }

    @Override
    public void reportIssue(HasTextRange toHighlight, String message) {
      reportIssue(toHighlight.textRange(), message);
    }

    @Override
    public void reportIssue(TextRange textRange, String message) {
      Issue issue = new Issue(textRange, message);
      if (!raisedIssues.contains(issue)) {
        if (verifier != null) {
          TextPointer start = textRange.start();
          TextPointer end = textRange.end();
          verifier.reportIssue(message).onRange(start.line(), start.lineOffset() + 1, end.line(), end.lineOffset());
        }
        raisedIssues.add(issue);
      }
    }
  }

  public static class Issue {
    private final TextRange textRange;
    private final String message;

    public Issue(TextRange textRange, String message) {
      this.textRange = textRange;
      this.message = message;
    }

    public Issue(TextRange textRange) {
      this.textRange = textRange;
      this.message = null;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Issue issue = (Issue) o;
      return textRange.equals(issue.textRange) && Objects.equals(message, issue.message);
    }

    @Override
    public int hashCode() {
      return Objects.hash(textRange, message);
    }
  }

  private static void compare(List<Issue> actualIssues, List<Issue> expectedIssues) {
    Map<TextRange, Tuple> map = new HashMap<>();

    for (Issue issue : actualIssues) {
      TextRange range = issue.textRange;
      if (map.get(range) == null) {
        Tuple tuple = new Tuple();
        tuple.addActual(issue);
        map.put(range, tuple);
      } else {
        map.get(range).addActual(issue);
      }
    }

    for (Issue issue : expectedIssues) {
      TextRange range = issue.textRange;
      if (map.get(range) == null) {
        Tuple tuple = new Tuple();
        tuple.addExpected(issue);
        map.put(range, tuple);
      } else {
        map.get(range).addExpected(issue);
      }
    }

    StringBuilder errorBuilder = new StringBuilder();
    for (Tuple tuple : map.values()) {
      errorBuilder.append(tuple.check());
    }

    String errorMessage = errorBuilder.toString();
    if (!errorMessage.isEmpty()) {
      throw new AssertionError("\n\n" + errorMessage);
    }
  }

  private static class Tuple {
    private static final String NO_ISSUE = "* [NO_ISSUE] Expected but no issue on range %s.\n\n";
    private static final String WRONG_MESSAGE = "* [WRONG_MESSAGE] Issue at %s : \nExpected message : %s\nActual message : %s\n\n";
    private static final String UNEXPECTED_ISSUE = "* [UNEXPECTED_ISSUE] at %s with a message: \"%s\"\n\n";
    private static final String WRONG_NUMBER = "* [WRONG_NUMBER] Range %s: Expecting %s issue, but actual issues number is %s\n\n";

    List<Issue> actual = new ArrayList<>();
    List<Issue> expected = new ArrayList<>();

    void addActual(Issue actual) {
      this.actual.add(actual);
    }

    void addExpected(Issue expected) {
      this.expected.add(expected);
    }

    String check() {
      if (!actual.isEmpty() && expected.isEmpty()) {
        return String.format(UNEXPECTED_ISSUE, actual.get(0).textRange, actual.get(0).message);

      } else if (actual.isEmpty() && !expected.isEmpty()) {
        return String.format(NO_ISSUE, expected.get(0).textRange);

      } else if (actual.size() == 1 && expected.size() == 1) {
        Issue expectedIssue = expected.get(0);
        Issue actualIssue = actual.get(0);
        return compareIssues(expectedIssue, actualIssue);

      } else if (actual.size() != expected.size()) {
        return String.format(WRONG_NUMBER, actual.get(0).textRange, expected.size(), actual.size());

      } else {
        for (int i = 0; i < actual.size(); i++) {
          if (!actual.get(i).message.equals(expected.get(i).message)) {
            return String.format(WRONG_MESSAGE, actual.get(i).textRange, expected.get(i).message, actual.get(i).message);
          }
        }
      }

      return "";
    }

    private static String compareIssues(Issue expectedIssue, Issue actualIssue) {
      String expectedMessage = expectedIssue.message;

      if (expectedMessage != null && !actualIssue.message.equals(expectedMessage)) {
        return String.format(WRONG_MESSAGE, actualIssue.textRange, expectedMessage, actualIssue.message);
      }

      return "";
    }
  }
}
