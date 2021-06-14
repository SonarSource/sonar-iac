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
import java.util.HashSet;
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
    createVerifier(parser, path, check).assertOneOrMoreIssues();
  }

  private static SingleFileVerifier createVerifier(TreeParser parser, Path path, IacCheck check) {

    SingleFileVerifier verifier = SingleFileVerifier.create(path, UTF_8);

    String testFileContent = readFile(path);
    Tree root = parser.parse(testFileContent, null);

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

    TestContext ctx = new TestContext(verifier);
    check.initialize(ctx);
    ctx.scan(root);

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
    private final Set<TextRange> raisedIssues = new HashSet<>();

    public TestContext(SingleFileVerifier verifier) {
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
      if (!raisedIssues.contains(textRange)) {
        TextPointer start = textRange.start();
        TextPointer end = textRange.end();
        verifier
          .reportIssue(message)
          .onRange(start.line(), start.lineOffset() + 1, end.line(), end.lineOffset());
        raisedIssues.add(textRange);
      }
    }
  }
}
