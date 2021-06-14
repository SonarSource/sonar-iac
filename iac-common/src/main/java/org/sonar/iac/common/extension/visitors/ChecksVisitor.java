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
package org.sonar.iac.common.extension.visitors;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.rule.RuleKey;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.DurationStatistics;

public class ChecksVisitor extends TreeVisitor<InputFileContext> {

  private final DurationStatistics statistics;

  public ChecksVisitor(Checks<IacCheck> checks, DurationStatistics statistics) {
    this.statistics = statistics;
    Collection<IacCheck> activeChecks = checks.all();
    for (IacCheck check : activeChecks) {
      RuleKey ruleKey = checks.ruleKey(check);
      Objects.requireNonNull(ruleKey);
      check.initialize(new ContextAdapter(ruleKey));
    }
  }

  public class ContextAdapter implements InitContext, CheckContext {

    public final RuleKey ruleKey;
    private InputFileContext currentCtx;
    private final Set<TextRange> raisedIssues;

    public ContextAdapter(RuleKey ruleKey) {
      this.ruleKey = ruleKey;
      raisedIssues = new HashSet<>();
    }

    @Override
    public <T extends Tree> void register(Class<T> cls, BiConsumer<CheckContext, T> visitor) {
      ChecksVisitor.this.register(cls, statistics.time(ruleKey.rule(), (ctx, tree) -> {
        currentCtx = ctx;
        visitor.accept(this, tree);
      }));
    }

    @Override
    public void reportIssue(HasTextRange toHighlight, String message) {
      reportIssue(toHighlight.textRange(), message);
    }

    @Override
    public void reportIssue(@Nullable TextRange textRange, String message) {
      // We avoid raising an issue on text ranges on which we already raised one. This is to avoid duplicate ones which might happen, for example, with Yaml anchors SONARIAC-78.
      // Once we'll need to introduce a secondary locations mechanism, a more sophisticated mechanism has to be used to detect duplicates.
      if (textRange == null || !raisedIssues.contains(textRange)) {
        currentCtx.reportIssue(ruleKey, textRange, message);
        raisedIssues.add(textRange);
      }
    }
  }
}
