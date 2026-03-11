/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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
package org.sonar.iac.arm.tree.impl.bicep;

import java.util.List;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedMultilineString;
import org.sonar.iac.arm.tree.api.bicep.interpstring.InterpolatedStringLeftPiece;
import org.sonar.iac.arm.tree.api.bicep.interpstring.InterpolatedStringMiddlePiece;
import org.sonar.iac.arm.tree.api.bicep.interpstring.InterpolatedStringRightPiece;

public class InterpolatedMultilineStringImpl extends AbstractInterpolatedStringImpl implements InterpolatedMultilineString {

  public InterpolatedMultilineStringImpl(
    InterpolatedStringLeftPiece stringLeftPiece,
    List<InterpolatedStringMiddlePiece> stringMiddlePieces,
    InterpolatedStringRightPiece stringRightPiece) {
    super(stringLeftPiece, stringMiddlePieces, stringRightPiece);
  }

  @Override
  public Kind getKind() {
    return ArmTree.Kind.INTERPOLATED_MULTILINE_STRING;
  }
}
