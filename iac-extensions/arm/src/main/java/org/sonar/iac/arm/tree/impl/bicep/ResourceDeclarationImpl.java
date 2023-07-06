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
package org.sonar.iac.arm.tree.impl.bicep;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.checks.TextUtils;

import static org.sonar.iac.arm.tree.impl.json.ArmHelper.addChildrenIfPresent;

public class ResourceDeclarationImpl extends AbstractArmTreeImpl implements ResourceDeclaration {

  private final SyntaxToken keyword;
  private final Identifier name;
  private final InterpolatedString typeAndVersion;
  @Nullable
  private final SyntaxToken existing;
  private final SyntaxToken equalsSign;
  private final ObjectExpression objectExpression;

  public ResourceDeclarationImpl(
    SyntaxToken keyword,
    Identifier name,
    InterpolatedString typeAndVersion,
    @Nullable SyntaxToken existing,
    SyntaxToken equalsSign,
    ObjectExpression objectExpression) {

    this.keyword = keyword;
    this.name = name;
    this.typeAndVersion = typeAndVersion;
    this.existing = existing;
    this.equalsSign = equalsSign;
    this.objectExpression = objectExpression;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(keyword);
    children.add(name);
    children.add(typeAndVersion);
    addChildrenIfPresent(children, existing);
    children.add(equalsSign);
    children.add(objectExpression);
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.RESOURCE_DECLARATION;
  }

  @Override
  public Identifier name() {
    return name;
  }

  @Override
  public StringLiteral version() {
    String text = TextUtils.getValue(typeAndVersion).orElse("");
    String[] split = text.split("@");
    if (split.length == 2) {
      int indexOfAt = text.indexOf("@");
      String version = split[1];
      TextRange typeAndVersionRange = typeAndVersion.textRange();
      TextRange tokenRange = TextRanges.range(
        typeAndVersionRange.start().line(),
        typeAndVersionRange.start().lineOffset() + indexOfAt + 1,
        typeAndVersionRange.end().line(),
        typeAndVersionRange.start().lineOffset() + indexOfAt + 1 + version.length());
      SyntaxToken token = new SyntaxTokenImpl(version, tokenRange, List.of());
      return new StringLiteralImpl(token);
    }
    return null;
  }

  @Override
  public StringLiteral type() {
    String text = TextUtils.getValue(typeAndVersion).orElse("");
    String[] split = text.split("@");
    if (split.length == 2) {
      String type = split[0];
      TextRange typeAndVersionRange = typeAndVersion.textRange();
      TextRange tokenRange = TextRanges.range(
        typeAndVersionRange.start().line(),
        typeAndVersionRange.start().lineOffset(),
        typeAndVersionRange.end().line(),
        typeAndVersionRange.start().lineOffset() + type.length());
      SyntaxToken token = new SyntaxTokenImpl(type, tokenRange, List.of());
      return new StringLiteralImpl(token);
    }
    return null;
  }

  @Override
  public List<Property> properties() {
    return objectExpression.properties();
  }
}
