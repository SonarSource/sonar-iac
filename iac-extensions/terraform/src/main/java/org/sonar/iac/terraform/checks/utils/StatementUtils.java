/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks.utils;

import java.util.Optional;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.HasStatements;
import org.sonar.iac.terraform.api.tree.Statement;

public class StatementUtils {

  private StatementUtils() {
  }

  public static boolean hasBlock(HasStatements tree, String identifier) {
    return tree.statements().stream()
      .filter(BlockTree.class::isInstance)
      .anyMatch(s -> isStatement(s, identifier));
  }

  public static boolean hasAttribute(HasStatements tree, String identifier) {
    return tree.statements().stream()
      .filter(AttributeTree.class::isInstance)
      .anyMatch(s -> isStatement(s, identifier));
  }

  public static boolean hasStatement(HasStatements tree, String identifier) {
    return tree.statements().stream().anyMatch(s -> isStatement(s, identifier));
  }

  public static Optional<AttributeTree> getAttribute(HasStatements tree, String identifier) {
    return getStatement(tree, identifier, AttributeTree.class);
  }

  public static Optional<BlockTree> getBlock(HasStatements tree, String identifier) {
    return getStatement(tree, identifier, BlockTree.class);
  }

  private static <T extends Statement> Optional<T> getStatement(HasStatements tree, String identifier, Class<T> tClass) {
    return tree.statements().stream()
      .filter(s -> isStatement(s, identifier))
      .filter(tClass::isInstance)
      .findFirst().map(terraformTree -> (T) terraformTree);
  }

  private static boolean isStatement(Statement statement, String identifier) {
    return identifier.equals(statement.identifier().value());
  }
}
