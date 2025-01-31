/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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
import java.util.stream.Collectors;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.interpstring.InterpolatedStringLeftPiece;
import org.sonar.iac.arm.tree.api.bicep.interpstring.InterpolatedStringMiddlePiece;
import org.sonar.iac.arm.tree.api.bicep.interpstring.InterpolatedStringRightPiece;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.api.tree.Tree;

public class InterpolatedStringImpl extends AbstractArmTreeImpl implements InterpolatedString {
  private final InterpolatedStringLeftPiece stringLeftPiece;
  private final List<InterpolatedStringMiddlePiece> stringMiddlePieces;
  private final InterpolatedStringRightPiece stringRightPiece;

  public InterpolatedStringImpl(InterpolatedStringLeftPiece stringLeftPiece,
    List<InterpolatedStringMiddlePiece> stringMiddlePieces,
    InterpolatedStringRightPiece stringRightPiece) {
    this.stringLeftPiece = stringLeftPiece;
    this.stringMiddlePieces = stringMiddlePieces;
    this.stringRightPiece = stringRightPiece;
  }

  @Override
  public List<Tree> children() {
    List<Tree> result = new ArrayList<>();
    result.add(stringLeftPiece);
    result.addAll(stringMiddlePieces);
    result.add(stringRightPiece);
    return result;
  }

  @Override
  public Kind getKind() {
    return Kind.INTERPOLATED_STRING;
  }

  @Override
  public String value() {
    return stringLeftPiece.value() +
      stringMiddlePieces.stream()
        .map(TextTree::value)
        .collect(Collectors.joining())
      + stringRightPiece.value();
  }
}
