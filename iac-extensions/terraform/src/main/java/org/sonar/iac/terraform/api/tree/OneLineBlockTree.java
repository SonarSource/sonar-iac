/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.api.tree;

import java.util.List;
import java.util.Optional;

public interface OneLineBlockTree extends TerraformTree {
  SyntaxToken type();
  List<LabelTree> labels();
  Optional<AttributeTree> attribute();
}
