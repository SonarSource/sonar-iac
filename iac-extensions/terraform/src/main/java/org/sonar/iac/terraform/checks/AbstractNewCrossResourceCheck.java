/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.terraform.checks;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.FileTree;

import static java.util.stream.Collectors.toMap;

public abstract class AbstractNewCrossResourceCheck extends AbstractNewResourceCheck {

  protected Map<String, BlockTree> blockNameToBlockTree = new HashMap<>();

  @Override
  public void initialize(InitContext init) {
    init.register(FileTree.class, (CheckContext ctx, FileTree tree) -> blockNameToBlockTree = tree.properties().stream()
      .filter(BlockTree.class::isInstance)
      .map(BlockTree.class::cast)
      .filter(AbstractResourceCheck::hasReferenceLabel)
      .collect(toMap(AbstractResourceCheck::getReferenceLabel, Function.identity(), (block1, block2) ->
      // In theory, a valid Terraform file should not contain two blocks with the same name. This check is to be on the safe side.
      block1)));

    super.initialize(init);
  }

}
