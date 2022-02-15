/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.terraform.checks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.symbols.ResourceSymbol;

import static org.sonar.iac.terraform.checks.AbstractResourceCheck.isResource;

public abstract class AbstractNewResourceCheck implements IacCheck {

  private final Map<String, List<Consumer<ResourceSymbol>>> resourceConsumers = new HashMap<>();
  private final Map<String, Pattern> compiledPatterns = new HashMap<>();

  @Override
  public void initialize(InitContext init) {
    init.register(BlockTree.class, this::provideResource);
    registerResourceConsumer();
  }

  protected abstract void registerResourceConsumer();

  protected void provideResource(CheckContext ctx, BlockTree blockTree) {
    if (isResource(blockTree)) {
      ResourceSymbol resource = ResourceSymbol.fromPresent(ctx, blockTree);
      if (resourceConsumers.containsKey(resource.type)) {
        resourceConsumers.get(resource.type).forEach(consumer -> consumer.accept(resource));
      }
    }
  }

  protected void register(String resourceName, Consumer<ResourceSymbol> consumer) {
    resourceConsumers.computeIfAbsent(resourceName, i -> new ArrayList<>()).add(consumer);
  }

  protected void register(List<String> resourceNames, Consumer<ResourceSymbol> consumer) {
    resourceNames.forEach(resourceName -> register(resourceName, consumer));
  }

  private Pattern pattern(String regex, int flags) {
    return compiledPatterns.computeIfAbsent(String.format("pattern:%s,flags:%d", regex, flags), i -> Pattern.compile(regex, flags));
  }

  /**
   * Tests true iff the target expression is a string literal, and it's value is not equal to the expected one.
   */
  public Predicate<ExpressionTree> notEqualTo(String expected) {
    return expression -> TextUtils.isValue(expression, expected).isFalse();
  }

  /**
   * Tests true iff the target expression is a string literal, and it's value is equal to the expected one.
   */
  public Predicate<ExpressionTree> equalTo(String expected) {
    return expression -> TextUtils.isValue(expression, expected).isTrue();
  }

  /**
   * Tests true iff the target expression is a string literal that fully matches the pattern.
   */
  public Predicate<ExpressionTree> matchesPattern(String pattern, int flags) {
    return expression -> TextUtils.matchesValue(expression, s -> pattern(pattern, flags).matcher(s).matches()).isTrue();
  }

  /**
   * Tests true iff the target expression is a string literal that fully matches the case-insensitive pattern.
   */
  public Predicate<ExpressionTree> matchesPattern(String pattern) {
    return matchesPattern(pattern, Pattern.CASE_INSENSITIVE);
  }

  /**
   * Tests true iff the target expression is a string literal, and it's value is true.
   */
  public Predicate<ExpressionTree> isTrue() {
    return TextUtils::isValueTrue;
  }
}
