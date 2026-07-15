/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.extension.visitors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.rule.RuleKey;
import org.sonar.iac.common.AbstractTestTree;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.TestFileSkipping;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.languages.IacLanguage;
import org.sonarsource.analyzer.commons.appsec.TestFileClassifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChecksVisitorTest {

  private static final RuleKey TEST_RULE_KEY = RuleKey.of("test-repo", "S001");

  private Checks<IacCheck> checks;
  private DurationStatistics statistics;

  @BeforeEach
  void setUp() {
    checks = mock(Checks.class);
    when(checks.all()).thenReturn(List.of());
    statistics = new DurationStatistics(new MapSettings().asConfig());
  }

  @Test
  void shouldInitializeChecksOnConstruction() {
    List<String> initCalls = new ArrayList<>();
    IacCheck testCheck = init -> initCalls.add("initialized");

    when(checks.all()).thenReturn(List.of(testCheck));
    when(checks.ruleKey(testCheck)).thenReturn(TEST_RULE_KEY);

    new ChecksVisitor(checks, statistics);

    assertThat(initCalls).containsExactly("initialized");
  }

  @Test
  void shouldRegisterTreeVisitorThroughContextAdapter() {
    List<Tree> visitedTrees = new ArrayList<>();
    IacCheck testCheck = init -> init.register(Tree.class, (ctx, tree) -> visitedTrees.add(tree));

    when(checks.all()).thenReturn(List.of(testCheck));
    when(checks.ruleKey(testCheck)).thenReturn(TEST_RULE_KEY);

    ChecksVisitor visitor = new ChecksVisitor(checks, statistics);

    Tree child = new TestTree();
    Tree root = new TestTree(child);
    InputFileContext ctx = mock(InputFileContext.class);
    visitor.scan(ctx, root);

    assertThat(visitedTrees).containsExactly(root, child);
  }

  @Test
  void shouldRegisterSubTreeVisitorThroughContextAdapter() {
    List<Tree> visitedTrees = new ArrayList<>();
    IacCheck testCheck = init -> init.register(SubTestTree.class, (ctx, tree) -> visitedTrees.add(tree));

    when(checks.all()).thenReturn(List.of(testCheck));
    when(checks.ruleKey(testCheck)).thenReturn(TEST_RULE_KEY);

    ChecksVisitor visitor = new ChecksVisitor(checks, statistics);

    SubTestTree subTree = new SubTestTree();
    Tree root = new TestTree(subTree);
    InputFileContext ctx = mock(InputFileContext.class);
    visitor.scan(ctx, root);

    assertThat(visitedTrees).containsExactly(subTree);
  }

  @Test
  void shouldRegisterPostVisitorThroughContextAdapter() {
    List<Tree> visitedTrees = new ArrayList<>();
    IacCheck testCheck = init -> init.registerPost(Tree.class, (ctx, tree) -> visitedTrees.add(tree));

    when(checks.all()).thenReturn(List.of(testCheck));
    when(checks.ruleKey(testCheck)).thenReturn(TEST_RULE_KEY);

    ChecksVisitor visitor = new ChecksVisitor(checks, statistics);

    Tree child = new TestTree();
    Tree root = new TestTree(child);
    InputFileContext ctx = mock(InputFileContext.class);
    visitor.scan(ctx, root);

    // Post-order: child first, then root
    assertThat(visitedTrees).containsExactly(child, root);
  }

  @Test
  void shouldRegisterPostVisitorForSubtypes() {
    List<Tree> visitedTrees = new ArrayList<>();
    IacCheck testCheck = init -> init.registerPost(SubTestTree.class, (ctx, tree) -> visitedTrees.add(tree));

    when(checks.all()).thenReturn(List.of(testCheck));
    when(checks.ruleKey(testCheck)).thenReturn(TEST_RULE_KEY);

    ChecksVisitor visitor = new ChecksVisitor(checks, statistics);

    SubTestTree subTree = new SubTestTree();
    Tree root = new TestTree(subTree);
    InputFileContext ctx = mock(InputFileContext.class);
    visitor.scan(ctx, root);

    assertThat(visitedTrees).containsExactly(subTree);
  }

  @Test
  void shouldCallRegisterBeforeChildrenAndRegisterPostAfterChildren() {
    List<String> events = new ArrayList<>();
    IacCheck testCheck = init -> {
      init.register(Tree.class, (ctx, tree) -> events.add("pre:" + tree.getClass().getSimpleName()));
      init.registerPost(Tree.class, (ctx, tree) -> events.add("post:" + tree.getClass().getSimpleName()));
    };

    when(checks.all()).thenReturn(List.of(testCheck));
    when(checks.ruleKey(testCheck)).thenReturn(TEST_RULE_KEY);

    ChecksVisitor visitor = new ChecksVisitor(checks, statistics);

    Tree child = new SubTestTree();
    Tree root = new TestTree(child);
    InputFileContext ctx = mock(InputFileContext.class);
    visitor.scan(ctx, root);

    // Pre-visit root, pre-visit child, post-visit child, post-visit root
    assertThat(events).containsExactly("pre:TestTree", "pre:SubTestTree", "post:SubTestTree", "post:TestTree");
  }

  @Test
  void shouldSkipTestFileSkippingCheckOnTestPathFile() {
    List<Tree> visited = new ArrayList<>();
    IacCheck skippingCheck = (IacCheck & TestFileSkipping) init -> init.register(Tree.class, (ctx, tree) -> visited.add(tree));
    when(checks.all()).thenReturn(List.of(skippingCheck));
    when(checks.ruleKey(skippingCheck)).thenReturn(TEST_RULE_KEY);

    ChecksVisitor visitor = new ChecksVisitor(checks, statistics, TestFileClassifier.of(new MapSettings().asConfig()));
    visitor.scan(testFileCtx("test/pod.yaml"), new TestTree());

    assertThat(visited).isEmpty();
  }

  @Test
  void shouldNotSkipTestFileSkippingCheckOnMainPathFile() {
    List<Tree> visited = new ArrayList<>();
    IacCheck skippingCheck = (IacCheck & TestFileSkipping) init -> init.register(Tree.class, (ctx, tree) -> visited.add(tree));
    when(checks.all()).thenReturn(List.of(skippingCheck));
    when(checks.ruleKey(skippingCheck)).thenReturn(TEST_RULE_KEY);

    ChecksVisitor visitor = new ChecksVisitor(checks, statistics, TestFileClassifier.of(new MapSettings().asConfig()));
    visitor.scan(testFileCtx("src/main/pod.yaml"), new TestTree());

    assertThat(visited).hasSize(1);
  }

  @Test
  void shouldSkipTestFileSkippingCheckOnTestPathFileViaRegisterPost() {
    List<Tree> visited = new ArrayList<>();
    IacCheck skippingCheck = (IacCheck & TestFileSkipping) init -> init.registerPost(Tree.class, (ctx, tree) -> visited.add(tree));
    when(checks.all()).thenReturn(List.of(skippingCheck));
    when(checks.ruleKey(skippingCheck)).thenReturn(TEST_RULE_KEY);

    ChecksVisitor visitor = new ChecksVisitor(checks, statistics, TestFileClassifier.of(new MapSettings().asConfig()));
    visitor.scan(testFileCtx("test/pod.yaml"), new TestTree());

    assertThat(visited).isEmpty();
  }

  @Test
  void shouldNotSkipNormalCheckOnTestPathFile() {
    List<Tree> visited = new ArrayList<>();
    IacCheck normalCheck = init -> init.register(Tree.class, (ctx, tree) -> visited.add(tree));
    when(checks.all()).thenReturn(List.of(normalCheck));
    when(checks.ruleKey(normalCheck)).thenReturn(TEST_RULE_KEY);

    ChecksVisitor visitor = new ChecksVisitor(checks, statistics, TestFileClassifier.of(new MapSettings().asConfig()));
    visitor.scan(testFileCtx("test/pod.yaml"), new TestTree());

    assertThat(visited).hasSize(1);
  }

  private static InputFileContext testFileCtx(String relativePath) {
    InputFile inputFile = mock(InputFile.class);
    when(inputFile.relativePath()).thenReturn(relativePath);
    return new InputFileContext(mock(SensorContext.class), inputFile, IacLanguage.UNKNOWN);
  }

  static class TestTree extends AbstractTestTree {
    private final List<Tree> children;

    public TestTree(Tree... children) {
      this.children = Arrays.asList(children);
    }

    @Override
    public TextRange textRange() {
      return null;
    }

    @Override
    public List<Tree> children() {
      return children;
    }
  }

  static class SubTestTree extends TestTree {
    public SubTestTree() {
      super();
    }
  }
}
