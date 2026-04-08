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
package org.sonar.iac.arm.tree.impl.bicep;

import java.util.ArrayList;
import java.util.List;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedMultilineString;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class InterpolatedMultilineEmptyStringImpl extends AbstractArmTreeImpl implements InterpolatedMultilineString {

  private final SyntaxToken openingTripleApostrophe;
  private final SyntaxToken closingTripleApostrophe;

  public InterpolatedMultilineEmptyStringImpl(SyntaxToken openingTripleApostrophe, SyntaxToken closingTripleApostrophe) {
    this.openingTripleApostrophe = openingTripleApostrophe;
    this.closingTripleApostrophe = closingTripleApostrophe;
  }

  @Override
  public Kind getKind() {
    return Kind.INTERPOLATED_MULTILINE_STRING;
  }

  @Override
  public String value() {
    return "";
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(openingTripleApostrophe);
    children.add(closingTripleApostrophe);
    return children;
  }
}
