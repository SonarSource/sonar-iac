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
package org.sonar.iac.jvmframeworkconfig.plugin.visitors;

import java.util.Set;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.NoSonarNextLineResolver;
import org.sonar.iac.common.extension.visitors.NoSonarVisitor;
import org.sonar.iac.jvmframeworkconfig.tree.api.Profile;
import org.sonar.iac.jvmframeworkconfig.tree.api.SyntaxToken;
import org.sonar.iac.jvmframeworkconfig.tree.api.Tuple;

import static org.sonar.iac.jvmframeworkconfig.plugin.JvmFrameworkConfigSensor.isPropertiesFile;

public class JvmFrameworkConfigNoSonarVisitor extends NoSonarVisitor {

  private NoSonarNextLineResolver noSonarNextLineResolver;

  public JvmFrameworkConfigNoSonarVisitor(NoSonarFilter noSonarFilter) {
    super(noSonarFilter);
    this.noSonarNextLineResolver = new NoSonarNextLineResolver();
  }

  @Override
  protected void registerCommentConsumers() {
    register(SyntaxToken.class, (ctx, token) -> {
      addNoSonarLines(token.comments());
      if (!token.value().isBlank()) {
        noSonarNextLineResolver.addCodeLines(token.textRange());
      }
    });
    register(Profile.class, (ctx, profile) -> addNoSonarLines(profile.comments()));
    register(Tuple.class, (ctx, tuple) -> noSonarNextLineResolver.addElement(tuple.textRange()));
  }

  @Override
  protected void before(InputFileContext ctx, Tree root) {
    super.before(ctx, root);
    noSonarNextLineResolver = new NoSonarNextLineResolver();
  }

  @Override
  protected void after(InputFileContext ctx, Tree root) {
    if (isPropertiesFile(ctx)) {
      // A comment of a properties file is always alone on its line, so a comment suppressing issues also covers the next property
      noSonarLines().addAll(noSonarNextLineResolver.coveredLines(Set.copyOf(noSonarLines())));
    }
    super.after(ctx, root);
  }
}
