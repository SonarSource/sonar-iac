/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
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
