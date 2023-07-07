/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.arm.tree.impl.bicep.interpstring;

import com.sonar.sslr.api.typed.Optional;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.common.api.tree.Tree;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InterpolatedStringLeftPiece {
  private final SyntaxToken leftQuote;
  @Nullable private final SyntaxToken stringChars;
  private final SyntaxToken dollarLcurly;

  public InterpolatedStringLeftPiece(SyntaxToken leftQuote, Optional<SyntaxToken> stringChars, SyntaxToken dollarLcurly) {
    this.leftQuote = leftQuote;
    this.stringChars = stringChars.orNull();
    this.dollarLcurly = dollarLcurly;
  }

  public List<Tree> children() {
    return Stream.of(leftQuote, stringChars, dollarLcurly)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }
}
