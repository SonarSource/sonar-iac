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

  public static void verify(TreeParser parser, Path path, IacCheck check) {
    Tree root = parse(parser, path);
    SingleFileVerifier verifier = createVerifier(path, root);
    runAnalysis(verifier, check, root);
    verifier.assertOneOrMoreIssues();
  }

  public static void verify(TreeParser parser, Path path, IacCheck check, TestIssue... expectedIssues) {
    Tree root = parse(parser, path);
    List<TestIssue> actualIssues = runAnalysis(null, check, root);
    compare(actualIssues, Arrays.asList(expectedIssues));
  }

  public static void verifyNoIssue(TreeParser parser, Path path, IacCheck check) {
    Tree root = parse(parser, path);
    List<TestIssue> actualIssues = runAnalysis(null, check, root);
    compare(actualIssues, Collections.emptyList());
  }

  private static List<TestIssue> runAnalysis(@Nullable SingleFileVerifier verifier, IacCheck check, Tree root) {
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
    private final List<TestIssue> raisedIssues = new ArrayList<>();

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
      TestIssue issue = new TestIssue(textRange, message);
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

  public static class TestIssue {
    private final TextRange textRange;
    private final String message;

    public TestIssue(TextRange textRange, @Nullable String message) {
      this.textRange = textRange;
      this.message = message;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      TestIssue testIssue = (TestIssue) o;
      return textRange.equals(testIssue.textRange) && Objects.equals(message, testIssue.message);
    }

    @Override
    public int hashCode() {
      return Objects.hash(textRange, message);
    }
  }

  private static void compare(List<TestIssue> actualIssues, List<TestIssue> expectedIssues) {
    Map<TextRange, Tuple> map = new HashMap<>();

    for (TestIssue issue : actualIssues) {
      // TODO: 16.06.21 line -> range
      TextRange line = issue.textRange;
      if (map.get(line) == null) {
        Tuple tuple = new Tuple();
        tuple.addActual(issue);
        map.put(line, tuple);
      } else {
        map.get(line).addActual(issue);
      }
    }

    for (TestIssue issue : expectedIssues) {
      TextRange line = issue.textRange;
      if (map.get(line) == null) {
        Tuple tuple = new Tuple();
        tuple.addExpected(issue);
        map.put(line, tuple);
      } else {
        map.get(line).addExpected(issue);
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

    List<TestIssue> actual = new ArrayList<>();
    List<TestIssue> expected = new ArrayList<>();

    void addActual(TestIssue actual) {
      this.actual.add(actual);
    }

    void addExpected(TestIssue expected) {
      this.expected.add(expected);
    }

    String check() {
      if (!actual.isEmpty() && expected.isEmpty()) {
        return String.format(UNEXPECTED_ISSUE, actual.get(0).textRange, actual.get(0).message);

      } else if (actual.isEmpty() && !expected.isEmpty()) {
        return String.format(NO_ISSUE, expected.get(0).textRange);

      } else if (actual.size() == 1 && expected.size() == 1) {
        TestIssue expectedIssue = expected.get(0);
        TestIssue actualIssue = actual.get(0);
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

    private static String compareIssues(TestIssue expectedIssue, TestIssue actualIssue) {
      String expectedMessage = expectedIssue.message;

      if (expectedMessage != null && !actualIssue.message.equals(expectedMessage)) {
        return String.format(WRONG_MESSAGE, actualIssue.textRange, expectedMessage, actualIssue.message);
      }

      return "";
    }
  }
}
