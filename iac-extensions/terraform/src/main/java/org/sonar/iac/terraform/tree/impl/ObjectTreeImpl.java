/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import java.util.List;
import javax.annotation.Nullable;
import org.sonar.iac.terraform.api.tree.ObjectElementTree;
import org.sonar.iac.terraform.api.tree.ObjectTree;
import org.sonar.iac.terraform.api.tree.SeparatedTrees;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

public class ObjectTreeImpl extends AbstractCollectionValueTree<ObjectElementTree> implements ObjectTree {

  public ObjectTreeImpl(SyntaxToken openBrace, @Nullable SeparatedTrees<ObjectElementTree> elements, SyntaxToken closeBrace) {
    super(openBrace, elements, closeBrace);
  }

  @Override
  public List<ObjectElementTree> properties() {
    return elements().trees();
  }

  @Override
  public Kind getKind() {
    return Kind.OBJECT;
  }
}
