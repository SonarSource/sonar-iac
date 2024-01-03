/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.arm.tree;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.tree.api.bicep.AmbientTypeReference;
import org.sonar.iac.arm.tree.api.bicep.SingularTypeExpression;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.TypeExpression;
import org.sonar.iac.arm.tree.impl.bicep.AmbientTypeReferenceImpl;
import org.sonar.iac.arm.tree.impl.bicep.SingularTypeExpressionImpl;
import org.sonar.iac.arm.tree.impl.bicep.SyntaxTokenImpl;
import org.sonar.iac.arm.tree.impl.bicep.TypeExpressionImpl;
import org.sonar.iac.common.api.tree.impl.SeparatedListImpl;
import org.sonar.iac.common.api.tree.impl.TextRanges;

import static org.assertj.core.api.Assertions.assertThat;

class BicepTypeExpressionResolutionTest {

  @Test
  void shouldResolveSimpleAmbientTypeReference() {
    TypeExpression typeExpression = createExampleTypeExpression();

    String actual = BicepTypeExpressionResolution.resolve(typeExpression);

    assertThat(actual).isEqualTo("array");
  }

  @Test
  void shouldResolveMaxDepth1() {
    TypeExpression typeExpression = createExampleTypeExpression();

    String actual = BicepTypeExpressionResolution.resolve(typeExpression, 1);

    assertThat(actual).isEmpty();
  }

  private static TypeExpression createExampleTypeExpression() {
    SyntaxToken syntaxToken = new SyntaxTokenImpl("array", TextRanges.range(1, 0, 1, 5), List.of());
    AmbientTypeReference ambientTypeReference = new AmbientTypeReferenceImpl(syntaxToken);
    SingularTypeExpression singularTypeExpression = new SingularTypeExpressionImpl(ambientTypeReference, List.of());
    SeparatedListImpl<SingularTypeExpression, SyntaxToken> list = SeparatedListImpl.separatedList(singularTypeExpression, List.of());
    TypeExpression typeExpression = new TypeExpressionImpl(list);
    return typeExpression;
  }
}
