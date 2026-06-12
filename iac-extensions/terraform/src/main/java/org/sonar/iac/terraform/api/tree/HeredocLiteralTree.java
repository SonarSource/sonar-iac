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
package org.sonar.iac.terraform.api.tree;

/**
 * A heredoc string literal ({@code <<TAG ... TAG} or {@code <<-TAG ... TAG}).
 *
 * <p>Heredocs are still text literals — {@link #value()} returns the full heredoc including markers,
 * so all consumers of {@code LiteralExprTree} keep working. In addition, the heredoc body is parsed
 * once at construction time so checks can reason about structured content:
 *
 * <ul>
 *   <li>If the body is a valid JSON object, {@link #content()} returns an {@link ObjectTree}
 *       that is equivalent to what {@code jsonencode({...})} would produce.</li>
 *   <li>Otherwise (invalid JSON, JSON of a non-object type, free-form text such as shell scripts)
 *       {@link #content()} returns a plain {@link LiteralExprTree} wrapping the raw body without
 *       the surrounding markers.</li>
 * </ul>
 */
public interface HeredocLiteralTree extends LiteralExprTree {
  ExpressionTree content();
}
