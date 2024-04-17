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
package org.sonar.iac.arm.checks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.FunctionCall;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.visitors.TreeContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;

@Rule(key = "S1192")
public class StringLiteralDuplicatedCheck implements IacCheck {

  private static final String MESSAGE = "Define a variable instead of duplicating this literal \"%s\" %d times.";
  private static final String SECONDARY_MESSAGE = "Duplication.";
  private static final Pattern ALLOWED_DUPLICATED_LITERALS = Pattern.compile("(?U)^[a-zA-Z_][.\\-\\w]+$");

  public static final int THRESHOLD_DEFAULT = 3;
  public static final int MINIMAL_LITERAL_LENGTH_DEFAULT = 5;

  @RuleProperty(
    key = "threshold",
    defaultValue = "" + THRESHOLD_DEFAULT)
  protected int threshold = THRESHOLD_DEFAULT;

  @RuleProperty(
    key = "minimal_literal_length",
    defaultValue = "" + MINIMAL_LITERAL_LENGTH_DEFAULT,
    description = "Specify the minimum number of characters a string literal must have to be considered as a potential duplication")
  protected int minimalLiteralLength = MINIMAL_LITERAL_LENGTH_DEFAULT;

  @Override
  public void initialize(InitContext init) {
    init.register(File.class, this::visitFile);
  }

  private void visitFile(CheckContext ctx, File file) {
    var stringVisitor = new StringVisitor();
    file.statements().forEach(stringVisitor::scan);
    stringVisitor.reportDuplicates(ctx);
  }

  private class StringVisitor extends TreeVisitor<TreeContext> {

    private static final TreeContext DUMMY_TREE_CONTEXT = new TreeContext();
    private final Map<String, List<StringLiteral>> sameLiteralOccurrences = new HashMap<>();

    public StringVisitor() {
      register(StringLiteral.class, (ctx, tree) -> visitLiteral(tree));
    }

    private void scan(Tree root) {
      super.scan(DUMMY_TREE_CONTEXT, root);
    }

    private void visitLiteral(StringLiteral tree) {
      if (!isIgnored(tree)) {
        var value = tree.value();
        sameLiteralOccurrences.computeIfAbsent(value, k -> new ArrayList<>()).add(tree);
      }
    }

    private boolean isIgnored(StringLiteral stringLiteral) {
      var value = stringLiteral.value();
      return value.length() < minimalLiteralLength
        || ALLOWED_DUPLICATED_LITERALS.matcher(value).matches()
        || isResourceTypeAndApiVersionField(stringLiteral)
        || isResourceId(stringLiteral);
    }

    private static boolean isResourceTypeAndApiVersionField(StringLiteral stringLiteral) {
      Tree parent = stringLiteral.parent();
      if (parent instanceof ResourceDeclaration resource) {
        return stringLiteral.value().contains("@") || resource.type() == stringLiteral || resource.version() == stringLiteral;
      }
      return false;
    }

    private static boolean isResourceId(StringLiteral stringLiteral) {
      Tree parent = stringLiteral.parent();
      return parent instanceof FunctionCall functionCall && "resourceId".equals(functionCall.name().value());
    }

    private void reportDuplicates(CheckContext ctx) {
      for (Map.Entry<String, List<StringLiteral>> literalOccurrences : sameLiteralOccurrences.entrySet()) {
        List<StringLiteral> occurrences = literalOccurrences.getValue();
        if (occurrences.size() >= threshold) {
          var literal = literalOccurrences.getKey();
          var message = MESSAGE.formatted(literal, occurrences.size());
          StringLiteral firstOccurrenceTree = occurrences.get(0);
          List<SecondaryLocation> otherOccurrencesLocation = occurrences.stream()
            .skip(1)
            .map(o -> new SecondaryLocation(o, SECONDARY_MESSAGE))
            .toList();
          ctx.reportIssue(firstOccurrenceTree, message, otherOccurrencesLocation);
        }
      }
    }
  }
}
