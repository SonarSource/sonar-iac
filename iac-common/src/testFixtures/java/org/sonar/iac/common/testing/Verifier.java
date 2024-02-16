/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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
import org.assertj.core.api.SoftAssertions;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.HasComments;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextPointer;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonarsource.analyzer.commons.checks.verifier.MultiFileVerifier;
import org.sonarsource.analyzer.commons.checks.verifier.SingleFileVerifier;
import org.sonarsource.analyzer.commons.checks.verifier.internal.InternalIssueVerifier;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Verifier {

  protected Verifier() {
    // utility class
  }

  public static void verify(TreeParser<Tree> parser, Path path, IacCheck check) {
    verify(parser, path, check, TestContext::new);
  }

  public static void verify(TreeParser<Tree> parser, Path path, IacCheck check, Function<InternalIssueVerifier, TestContext> contextSupplier) {
    Tree root = parse(parser, path);
    verify(root, path, check, contextSupplier);
  }

  /**
   * This method should only be used if "Noncompliant" comments in the code cannot be used to verify the issues.
   */
  public static void verify(TreeParser<Tree> parser, Path path, IacCheck check, Issue... expectedIssues) {
    Tree root = parse(parser, path);
    verify(root, path, check, expectedIssues);
  }

  public static void verify(Tree root, Path path, IacCheck check, Issue... expectedIssues) {
    List<Issue> actualIssues = runAnalysis(new TestContext(createVerifier(path, root)), check, root);
    compare(actualIssues, Arrays.asList(expectedIssues));
  }

  public static void verify(Tree root, Path path, IacCheck check, Function<InternalIssueVerifier, TestContext> contextSupplier) {
    InternalIssueVerifier verifier = createVerifier(path, root);
    runAnalysis(contextSupplier.apply(verifier), check, root);
    verifier.assertOneOrMoreIssues();
  }

  public static File contentToTmp(@Nullable String content) {
    File tempFile;
    try {
      tempFile = File.createTempFile("tmp-parser-", "");
      if (content != null) {
        Files.write(tempFile.toPath(), content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);
      }
      tempFile.deleteOnExit();
      return tempFile;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void verify(TreeParser<Tree> parser, String content, IacCheck check) {
    var tempFile = contentToTmp(content);
    verify(parser, tempFile.toPath(), check);
  }

  public static void verify(TreeParser<Tree> parser, String content, IacCheck check, Issue... expectedIssues) {
    Tree root = parser.parse(content, null);
    var tempFile = contentToTmp(null);
    List<Issue> actualIssues = runAnalysis(new TestContext(createVerifier(tempFile.toPath(), root)), check, root);
    compare(actualIssues, Arrays.asList(expectedIssues));
  }

  public static void verifyNoIssue(TreeParser<Tree> parser, String content, IacCheck check) {
    var tempFile = contentToTmp(content);
    verifyNoIssue(parser, tempFile.toPath(), check, TestContext::new);
  }

  public static void verifyNoIssue(TreeParser<Tree> parser, Path path, IacCheck check) {
    verifyNoIssue(parser, path, check, TestContext::new);
  }

  public static void verifyNoIssue(TreeParser<Tree> parser, Path path, IacCheck check, Function<InternalIssueVerifier, TestContext> contextSupplier) {
    Tree root = parse(parser, path);
    verifyNoIssue(root, path, check, contextSupplier);
  }

  public static void verifyNoIssue(Tree root, Path path, IacCheck check, Function<InternalIssueVerifier, TestContext> contextSupplier) {
    InternalIssueVerifier verifier = createVerifier(path, root);
    List<Issue> actualIssues = runAnalysis(contextSupplier.apply(verifier), check, root);
    compare(actualIssues, Collections.emptyList());
  }

  protected static List<Issue> runAnalysis(TestContext ctx, IacCheck check, Tree root) {
    check.initialize(ctx);
    ctx.scan(root);
    return ctx.raisedIssues;
  }

  public static Tree parse(TreeParser<Tree> parser, Path path) {
    var testFileContent = readFile(path);
    return parse(parser, testFileContent, null);
  }

  public static Tree parse(TreeParser<Tree> parser, String content, @Nullable InputFileContext inputFileContext) {
    return parser.parse(content, inputFileContext);
  }

  private static InternalIssueVerifier createVerifier(Path path, Tree root) {
    return createVerifier(path, root, commentsVisitor());
  }

  protected static InternalIssueVerifier createVerifier(Path path, Tree root, BiConsumer<Tree, Map<Integer, Set<Comment>>> commentsVisitor) {
    var verifier = new InternalIssueVerifier(path, UTF_8);
    Map<Integer, Set<Comment>> commentsByLine = new HashMap<>();
    commentsVisitor.accept(root, commentsByLine);

    commentsByLine.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue)
      .forEach(comments -> comments.forEach(comment -> {
        TextPointer start = comment.textRange().start();
        verifier.addComment(path, start.line(), start.lineOffset() + 1, comment.value(), 2, 0);
      }));

    return verifier;
  }

  private static BiConsumer<Tree, Map<Integer, Set<Comment>>> commentsVisitor() {
    Set<TextRange> alreadyAdded = new HashSet<>();
    return (root, commentsByLine) -> (new TreeVisitor<>()).register(Tree.class,
      (ctx, tree) -> {
        if (tree instanceof HasComments && !alreadyAdded.contains(tree.textRange())) {
          for (Comment comment : ((HasComments) tree).comments()) {
            commentsByLine.computeIfAbsent(comment.textRange().start().line(), i -> new HashSet<>()).add(comment);
          }
          alreadyAdded.add(tree.textRange());
        }
      }).scan(new TreeContext(), root);
  }

  private static String readFile(Path path) {
    try {
      return Files.readString(path);
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
    private final InternalIssueVerifier verifier;
    private final List<Issue> raisedIssues = new ArrayList<>();

    public TestContext(InternalIssueVerifier verifier) {
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
      var issue = new Issue(textRange, message, secondaryLocations);
      if (!raisedIssues.contains(issue)) {
        TextPointer start = textRange.start();
        TextPointer end = textRange.end();

        var reportedIssue = ((SingleFileVerifier) verifier)
          .reportIssue(message)
          .onRange(start.line(), start.lineOffset() + 1, end.line(), end.lineOffset());

        secondaryLocations.forEach(secondary -> {
          if (secondary.filePath != null) {
            addSecondaryOnDifferentFile(((MultiFileVerifier.Issue) reportedIssue), secondary);
          } else {
            addSecondaryOnMainFile(reportedIssue, secondary);
          }
        });
        raisedIssues.add(issue);
      }
    }

    private static void addSecondaryOnMainFile(SingleFileVerifier.Issue reportedIssue, SecondaryLocation secondary) {
      reportedIssue.addSecondary(
        secondary.textRange.start().line(),
        secondary.textRange.start().lineOffset() + 1,
        secondary.textRange.end().line(),
        secondary.textRange.end().lineOffset(),
        secondary.message);
    }

    private static void addSecondaryOnDifferentFile(MultiFileVerifier.Issue reportedIssue, SecondaryLocation secondary) {
      reportedIssue.addSecondary(
        Path.of(secondary.filePath),
        secondary.textRange.start().line(),
        secondary.textRange.start().lineOffset() + 1,
        secondary.textRange.end().line(),
        secondary.textRange.end().lineOffset(),
        secondary.message);
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
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
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

  protected static void compare(List<Issue> actualIssues, List<Issue> expectedIssues) {
    Map<TextRange, Tuple> map = new HashMap<>();

    for (Issue issue : actualIssues) {
      TextRange range = issue.textRange;
      map.computeIfAbsent(range, r -> new Tuple()).addActual(issue);
    }

    for (Issue issue : expectedIssues) {
      TextRange range = issue.textRange;
      map.computeIfAbsent(range, r -> new Tuple()).addExpected(issue);
    }

    SoftAssertions softly = new SoftAssertions();
    map.values().stream()
      .map(Tuple::check)
      .filter(it -> !it.isBlank())
      .forEach(softly::fail);
    softly.assertAll();
  }

  private static class Tuple {
    private static final String NO_ISSUE = "* [NO_ISSUE] %s\n\n";
    private static final String WRONG_MESSAGE = "* [WRONG_MESSAGE]\nexpected: %s\nbut was:  %s";
    private static final String UNEXPECTED_ISSUE = "* [UNEXPECTED_ISSUE] %s\n\n";
    private static final String WRONG_NUMBER = "* [WRONG_NUMBER] Range %s: Expecting %s issue, but actual issues number is %s\n\n";
    private static final String NO_SECONDARY = "* [NO_SECONDARY] %s\n\n";
    private static final String UNEXPECTED_SECONDARY = "* [UNEXPECTED_SECONDARY] %s\n\n";

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
        return String.format(UNEXPECTED_ISSUE, formatIssue(actual.get(0)));

      } else if (actual.isEmpty() && !expected.isEmpty()) {
        return String.format(NO_ISSUE, formatIssue(expected.get(0)));

      } else if (actual.size() == 1 && expected.size() == 1) {
        var expectedIssue = expected.get(0);
        var actualIssue = actual.get(0);
        return compareIssues(expectedIssue, actualIssue);

      } else if (actual.size() != expected.size()) {
        return String.format(WRONG_NUMBER, actual.get(0).textRange, expected.size(), actual.size());

      } else {
        for (var i = 0; i < actual.size(); i++) {
          if (!actual.get(i).message.equals(expected.get(i).message)) {
            return String.format(WRONG_MESSAGE, formatIssue(expected.get(i)), formatIssue(actual.get(i)));
          }
        }
      }

      return "";
    }

    private static String formatIssue(Issue issue) {
      return String.format("issue(%s, \"%s\")", formatTextRange(issue.textRange), issue.message);
    }

    private static String formatTextRange(TextRange textRange) {
      return textRange.start().line() + ", " + textRange.start().lineOffset() + ", " + textRange.end().line() + ", " + textRange.end().lineOffset();
    }

    private static String formatSecondary(SecondaryLocation secondary) {
      return String.format("secondary(%s, \"%s\")", formatTextRange(secondary.textRange), secondary.message);
    }

    private static String compareIssues(Issue expectedIssue, Issue actualIssue) {
      String expectedMessage = expectedIssue.message;

      if (expectedMessage != null && !actualIssue.message.equals(expectedMessage)) {
        return String.format(WRONG_MESSAGE, formatIssue(expectedIssue), formatIssue(actualIssue));
      }

      StringBuilder secondaryMessages = new StringBuilder();

      if (!expectedIssue.secondaryLocations.isEmpty()) {
        expectedIssue.secondaryLocations.stream()
          .filter(e -> !actualIssue.secondaryLocations.contains(e))
          .forEach(second -> secondaryMessages.append(String.format(NO_SECONDARY, formatSecondary(second))));

        actualIssue.secondaryLocations.stream()
          .filter(e -> !expectedIssue.secondaryLocations.contains(e))
          .forEach(second -> secondaryMessages.append(String.format(UNEXPECTED_SECONDARY, formatSecondary(second))));
      }

      return secondaryMessages.toString();
    }
  }
}
