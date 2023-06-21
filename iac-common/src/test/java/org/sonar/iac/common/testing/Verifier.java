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
package org.sonar.iac.common.testing;

import java.io.File;
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
import java.util.function.Function;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.impl.TextPointer;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.HasComments;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRanges;
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
    verify(parser, path, check, TestContext::new);
  }

  public static void verify(TreeParser<Tree> parser, Path path, IacCheck check, Function<SingleFileVerifier, TestContext> contextSupplier) {
    Tree root = parse(parser, path);
    verify(root, path, check, contextSupplier);
  }

  /**
   * This method should only be used if "Noncompliant" comments in the code cannot be used to verify the issues.
   */
  public static void verify(TreeParser<Tree> parser, Path path, IacCheck check, Issue... expectedIssues) {
    Tree root = parse(parser, path);
    List<Issue> actualIssues = runAnalysis(new TestContext(createVerifier(path, root)), check, root);
    compare(actualIssues, Arrays.asList(expectedIssues));
  }

  public static void verify(Tree root, Path path, IacCheck check, Function<SingleFileVerifier, TestContext> contextSupplier) {
    SingleFileVerifier verifier = createVerifier(path, root);
    runAnalysis(contextSupplier.apply(verifier), check, root);
    verifier.assertOneOrMoreIssues();
  }

  public static void verify(TreeParser<Tree> parser, String content, IacCheck check, Issue... expectedIssues) {
    Tree root = parser.parse(content, null);
    File tempFile;
    try {
      tempFile = File.createTempFile("tmp-parser-", "");
      tempFile.deleteOnExit();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    List<Issue> actualIssues = runAnalysis(new TestContext(createVerifier(tempFile.toPath(), root)), check, root);
    compare(actualIssues, Arrays.asList(expectedIssues));
  }

  public static void verifyNoIssue(TreeParser<Tree> parser, Path path, IacCheck check) {
    verifyNoIssue(parser, path, check, TestContext::new);
  }

  public static void verifyNoIssue(TreeParser<Tree> parser, Path path, IacCheck check, Function<SingleFileVerifier, TestContext> contextSupplier) {
    Tree root = parse(parser, path);
    verifyNoIssue(root, path, check, contextSupplier);
  }

  public static void verifyNoIssue(Tree root, Path path, IacCheck check, Function<SingleFileVerifier, TestContext> contextSupplier) {
    SingleFileVerifier verifier = createVerifier(path, root);
    List<Issue> actualIssues = runAnalysis(contextSupplier.apply(verifier), check, root);
    compare(actualIssues, Collections.emptyList());
  }

  private static List<Issue> runAnalysis(TestContext ctx, IacCheck check, Tree root) {
    check.initialize(ctx);
    ctx.scan(root);
    return ctx.raisedIssues;
  }

  public static Tree parse(TreeParser<Tree> parser, Path path) {
    String testFileContent = readFile(path);
    return parser.parse(testFileContent, null);
  }

  private static SingleFileVerifier createVerifier(Path path, Tree root) {
    SingleFileVerifier verifier = SingleFileVerifier.create(path, UTF_8);
    Map<Integer, Set<Comment>> commentsByLine = new HashMap<>();
    final Set<TextRange> alreadyAdded = new HashSet<>();
    (new TreeVisitor<>())
      .register(Tree.class, (ctx, tree) -> {
        if (tree instanceof HasComments && !alreadyAdded.contains(tree.textRange())) {
          for (Comment comment : ((HasComments) tree).comments()) {
            commentsByLine.computeIfAbsent(comment.textRange().start().line(), i -> new HashSet<>()).add(comment);
          }
          alreadyAdded.add(tree.textRange());
        }

      }).scan(new TreeContext(), root);

    commentsByLine.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue)
      .forEach(comments -> comments.forEach(comment -> {
        TextPointer start = comment.textRange().start();
        verifier.addComment(start.line(), start.lineOffset() + 1, comment.value(), 2, 0);
      }));

    return verifier;
  }

  private static String readFile(Path path) {
    try {
      return new String(Files.readAllBytes(path), UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot read " + path, e);
    }
  }

  public static Issue issue(int startLine, int startColumn, int endLine, int endColumn) {
    return new Issue(TextRanges.range(startLine, startColumn, endLine, endColumn));
  }

  public static Issue issue(TextRange range) {
    return new Issue(range);
  }

  public static Issue issue(int startLine, int startColumn, int endLine, int endColumn, @Nullable String message, SecondaryLocation... secondaryLocations) {
    return new Issue(TextRanges.range(startLine, startColumn, endLine, endColumn), message, List.of(secondaryLocations));
  }

  public static Issue issue(TextRange textRange, @Nullable String message, SecondaryLocation... secondaryLocations) {
    return new Issue(textRange, message, List.of(secondaryLocations));
  }

  public static class TestContext extends TreeContext implements InitContext, CheckContext {

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
    public void reportIssue(TextRange textRange, String message) {
      reportIssue(textRange, message, Collections.emptyList());
    }

    @Override
    public void reportIssue(HasTextRange toHighlight, String message) {
      reportIssue(toHighlight.textRange(), message, Collections.emptyList());
    }

    @Override
    public void reportIssue(HasTextRange toHighlight, String message, SecondaryLocation secondaryLocations) {
      reportIssue(toHighlight.textRange(), message, Collections.singletonList(secondaryLocations));
    }

    @Override
    public void reportIssue(HasTextRange toHighlight, String message, List<SecondaryLocation> secondaryLocations) {
      reportIssue(toHighlight.textRange(), message, secondaryLocations);
    }

    protected void reportIssue(TextRange textRange, String message, List<SecondaryLocation> secondaryLocations) {
      if (verifier == null) {
        throw new UnsupportedOperationException("Verifier is not provided for this context. It is not expected to report an issue in this context.");
      }

      Issue issue = new Issue(textRange, message, secondaryLocations);
      if (!raisedIssues.contains(issue)) {
        TextPointer start = textRange.start();
        TextPointer end = textRange.end();
        SingleFileVerifier.Issue reportedIssue = verifier
          .reportIssue(message)
          .onRange(start.line(), start.lineOffset() + 1, end.line(), end.lineOffset());
        secondaryLocations.forEach(secondary -> reportedIssue.addSecondary(
          secondary.textRange.start().line(),
          secondary.textRange.start().lineOffset() + 1,
          secondary.textRange.end().line(),
          secondary.textRange.end().lineOffset(),
          secondary.message));
        raisedIssues.add(issue);
      }
    }
  }

  public static class Issue {
    private final TextRange textRange;
    private final String message;
    private final List<SecondaryLocation> secondaryLocations;

    public Issue(TextRange textRange, @Nullable String message, List<SecondaryLocation> secondaryLocations) {
      this.textRange = textRange;
      this.message = message;
      this.secondaryLocations = secondaryLocations;
    }

    public Issue(TextRange textRange, @Nullable String message, SecondaryLocation secondaryLocation) {
      this(textRange, message, Collections.singletonList(secondaryLocation));
    }

    public Issue(TextRange textRange, @Nullable String message) {
      this(textRange, message, Collections.emptyList());
    }

    public Issue(TextRange textRange) {
      this(textRange, null);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      Issue other = (Issue) o;
      return this.textRange.equals(other.textRange)
        && Objects.equals(this.message, other.message)
        && this.secondaryLocations.equals(other.secondaryLocations);
    }

    @Override
    public int hashCode() {
      return Objects.hash(textRange, message, secondaryLocations);
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
    private static final String NO_SECONDARY = "* [NO_SECONDARY] Expected but no secondary location for issue at line %d on range %s.\n\n";
    private static final String UNEXPECTED_SECONDARY = "* [UNEXPECTED_SECONDARY] for issue at line %s at %s with a message: \"%s\"\n\n";

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

      StringBuilder secondaryMessages = new StringBuilder();

      if (!expectedIssue.secondaryLocations.isEmpty()) {
        expectedIssue.secondaryLocations.stream()
          .filter(e -> !actualIssue.secondaryLocations.contains(e))
          .forEach(second -> secondaryMessages.append(String.format(NO_SECONDARY, expectedIssue.textRange.start().line(), second.textRange)));

        actualIssue.secondaryLocations.stream()
          .filter(e -> !expectedIssue.secondaryLocations.contains(e))
          .forEach(second -> secondaryMessages.append(String.format(UNEXPECTED_SECONDARY, actualIssue.textRange.start().line(), second.textRange, second.message)));
      }

      return secondaryMessages.toString();
    }
  }
}
