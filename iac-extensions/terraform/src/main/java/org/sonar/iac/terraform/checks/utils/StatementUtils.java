/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks.utils;

import java.util.Optional;
import java.util.stream.Stream;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.HasStatements;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.api.tree.TerraformTree.Kind;

public class StatementUtils {

  private StatementUtils() {
  }

  public static boolean hasBlock(HasStatements tree, String type) {
    return hasStatement(tree, type, Kind.BLOCK);
  }

  public static boolean hasAttribute(HasStatements tree, String name) {
    return hasStatement(tree, name, Kind.ATTRIBUTE);
  }

  public static Optional<AttributeTree> getAttribute(HasStatements tree, String name) {
    return getStatement(tree, name, Kind.ATTRIBUTE);
  }

  public static Optional<AttributeTree> getBlock(HasStatements tree, String name) {
    return getStatement(tree, name, Kind.BLOCK);
  }

  private static boolean hasStatement(HasStatements tree, String key, Kind kind) {
    return getStatements(tree).anyMatch(s -> isStatement(s, key, kind));
  }

  private static <T extends TerraformTree> Optional<T> getStatement(HasStatements tree, String name, Kind kind) {
    return getStatements(tree).filter(s -> isStatement(s, name, kind)).findFirst().map(terraformTree -> (T) terraformTree);
  }

  private static Stream<TerraformTree> getStatements(HasStatements tree) {
    return tree.statements().stream().map(TerraformTree.class::cast);
  }

  private static boolean isStatement(TerraformTree tree, String key, Kind kind) {
    if (tree.is(kind)) {
      if (tree.is(Kind.BLOCK)) return key.equals(((BlockTree) tree).type().value());
      if (tree.is(Kind.ATTRIBUTE)) return key.equals(((AttributeTree) tree).name().value());
    }
    return false;
  }
}
