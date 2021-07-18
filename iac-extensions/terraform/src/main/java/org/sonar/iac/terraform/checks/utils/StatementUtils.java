/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks.utils;

import java.util.Optional;
import org.sonar.iac.common.api.tree.HasProperties;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.StatementTree;

public class StatementUtils {

  private StatementUtils() {
  }

  public static Optional<ExpressionTree> getAttributeValue(Tree tree, String identifier) {
    return PropertyUtils.value(tree, identifier).filter(ExpressionTree.class::isInstance).map(ExpressionTree.class::cast);
  }

  public static Optional<BlockTree> getBlock(HasProperties tree, String identifier) {
    return getStatement(tree, identifier, BlockTree.class);
  }

  private static <T extends StatementTree> Optional<T> getStatement(HasProperties tree, String identifier, Class<T> tClass) {
    return tree.properties().stream()
      .map(StatementTree.class::cast)
      .filter(s -> isStatement(s, identifier))
      .filter(tClass::isInstance)
      .findFirst().map(terraformTree -> (T) terraformTree);
  }

  private static boolean isStatement(StatementTree statement, String identifier) {
    return identifier.equals(statement.key().value());
  }
}
