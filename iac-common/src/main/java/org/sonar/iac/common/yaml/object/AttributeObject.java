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
package org.sonar.iac.common.yaml.object;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

public class AttributeObject extends YamlObject<TupleTree> {

  AttributeObject(CheckContext ctx, @Nullable TupleTree tree, String key, Status status) {
    super(ctx, tree, key, status);
  }

  public static AttributeObject fromPresent(CheckContext ctx, YamlTree tree, String key) {
    if (tree instanceof TupleTree tupleTree) {
      return new AttributeObject(ctx, tupleTree, key, Status.PRESENT);
    }
    return new AttributeObject(ctx, null, key, Status.UNKNOWN);
  }

  public static AttributeObject fromAbsent(CheckContext ctx, String key) {
    return new AttributeObject(ctx, null, key, Status.ABSENT);
  }

  public AttributeObject reportIfValue(Predicate<YamlTree> predicate, String message, List<SecondaryLocation> secondaryLocations) {
    if (isValue(predicate)) {
      ctx.reportIssue(tree.value(), message, secondaryLocations);
    }
    return this;
  }

  public AttributeObject reportIfValue(Predicate<YamlTree> predicate, String message) {
    return reportIfValue(predicate, message, Collections.emptyList());
  }

  public AttributeObject reportIfAbsent(@Nullable HasTextRange hasTextRange, String message, List<SecondaryLocation> secondaryLocations) {
    if (isAbsent() && hasTextRange != null) {
      report(hasTextRange.textRange(), message, secondaryLocations);
    }
    return this;
  }

  public AttributeObject reportIfAbsent(@Nullable HasTextRange hasTextRange, String message) {
    return reportIfAbsent(hasTextRange, message, Collections.emptyList());
  }

  private AttributeObject report(@Nullable TextRange textRange, String message) {
    if (textRange != null) {
      ctx.reportIssue(textRange, message);
    }
    return this;
  }

  private AttributeObject report(@Nullable TextRange textRange, String message, List<SecondaryLocation> secondaryLocations) {
    if (textRange != null) {
      ctx.reportIssue(() -> textRange, message, secondaryLocations);
    }
    return this;
  }

  public AttributeObject reportOnKey(String message) {
    if (tree != null) {
      report(tree.key().textRange(), message);
    }
    return this;
  }

  public AttributeObject reportOnKey(String message, List<SecondaryLocation> secondaryLocations) {
    if (tree != null) {
      report(tree.key().textRange(), message, secondaryLocations);
    }
    return this;
  }

  public AttributeObject reportOnValue(String message) {
    if (tree != null) {
      report(tree.value().toHighlight(), message);
    }
    return this;
  }

  public AttributeObject reportOnValue(String message, List<SecondaryLocation> secondaryLocations) {
    if (tree != null) {
      report(tree.value().toHighlight(), message, secondaryLocations);
    }
    return this;
  }

  public boolean isValue(Predicate<YamlTree> predicate) {
    return tree != null && predicate.test(tree.value());
  }

  @CheckForNull
  public String asStringValue() {
    if (tree == null) {
      return null;
    }
    return TextUtils.getValue(tree.value()).orElse(null);
  }
}
