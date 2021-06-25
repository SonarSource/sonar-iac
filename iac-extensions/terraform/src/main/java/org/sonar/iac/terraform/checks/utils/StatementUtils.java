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

  public static boolean hasStatement(HasStatements tree, String name, Kind... kinds) {
    return getStatements(tree).anyMatch(s -> isStatement(s, name, kinds));
  }

  public static Optional<TerraformTree> getStatement(HasStatements tree, String name, Kind... kinds) {
    return getStatements(tree).filter(statement -> isStatement(statement, name, kinds)).findFirst();
  }

  private static Stream<TerraformTree> getStatements(HasStatements tree) {
    return tree.statements().stream().map(TerraformTree.class::cast);
  }

  private static boolean isStatement(TerraformTree tree, String name, Kind... kinds) {
    if (tree.is(kinds)) {
      if (tree.is(Kind.BLOCK)) return name.equals(((BlockTree) tree).type().value());
      if (tree.is(Kind.ATTRIBUTE)) return name.equals(((AttributeTree) tree).name().value());
    }
    return false;
  }
}
