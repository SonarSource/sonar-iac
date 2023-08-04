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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.arm.parser.bicep.BicepKeyword;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.Decorator;
import org.sonar.iac.arm.tree.api.bicep.ForExpression;
import org.sonar.iac.arm.tree.api.bicep.HasDecorators;
import org.sonar.iac.arm.tree.api.bicep.HasKeyword;
import org.sonar.iac.arm.tree.api.bicep.IfCondition;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.StringComplete;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.checks.TextUtils;

import static org.sonar.iac.arm.tree.ArmHelper.addChildrenIfPresent;

public class ResourceDeclarationImpl extends AbstractArmTreeImpl implements ResourceDeclaration, HasDecorators, HasKeyword {

  private final List<Decorator> decorators;
  private final SyntaxToken keyword;
  private final Identifier name;
  private final InterpolatedString typeAndVersion;
  @Nullable
  private final SyntaxToken existing;
  private final SyntaxToken equalsSign;
  private final Expression body;

  public ResourceDeclarationImpl(
    List<Decorator> decorators,
    SyntaxToken keyword,
    Identifier name,
    InterpolatedString typeAndVersion,
    @Nullable SyntaxToken existing,
    SyntaxToken equalsSign,
    Expression body) {

    this.decorators = decorators;
    this.keyword = keyword;
    this.name = name;
    this.typeAndVersion = typeAndVersion;
    this.existing = existing;
    this.equalsSign = equalsSign;
    this.body = body;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>(decorators);
    children.add(keyword);
    children.add(name);
    children.add(typeAndVersion);
    addChildrenIfPresent(children, existing);
    children.add(equalsSign);
    children.add(body);
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.RESOURCE_DECLARATION;
  }

  @Override
  @CheckForNull
  public StringLiteral name() {
    return resourceProperties().stream()
      .filter(prop -> TextUtils.isValue(prop.key(), "name").isTrue())
      .filter(prop -> prop.value().is(Kind.STRING_COMPLETE))
      .findFirst()
      .map(prop -> ((StringComplete) prop.value()).content())
      .orElse(null);
  }

  @CheckForNull
  @Override
  public Identifier symbolicName() {
    return name;
  }

  @Override
  @CheckForNull
  public TextTree version() {
    String text = TextUtils.getValue(typeAndVersion).orElse("");
    if (text.contains("@")) {
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
    }
    return null;
  }

  @Override
  public TextTree type() {
    String text = TextUtils.getValue(typeAndVersion).orElse("");
    if (text.contains("@")) {
      String[] split = text.split("@");
      if (split.length == 2) {
        String type = split[0];
        TextRange typeAndVersionRange = typeAndVersion.textRange();
        TextRange tokenRange = TextRanges.range(
          typeAndVersionRange.start().line(),
          // The line offset needs to be shifted by 1 to take the quote into account
          typeAndVersionRange.start().lineOffset() + 1,
          typeAndVersionRange.end().line(),
          typeAndVersionRange.start().lineOffset() + type.length() + 1);
        SyntaxToken token = new SyntaxTokenImpl(type, tokenRange, List.of());
        return new StringLiteralImpl(token);
      }
    }
    // TODO SONARIAC-1019 ARM Bicep: make ResourceDelcaration.type() @CheckForNull
    return typeAndVersion;
  }

  @Override
  public List<Property> properties() {
    return getObjectBody()
      .map(objectBody -> propertiesOrEmpty(objectBody.properties()))
      .orElse(Collections.emptyList());
  }

  private Optional<ObjectExpression> getObjectBody() {
    ObjectExpression objectBody = null;
    if (body.is(Kind.OBJECT_EXPRESSION)) {
      objectBody = (ObjectExpression) body;
    } else if (body.is(Kind.IF_CONDITION)) {
      objectBody = ((IfCondition) body).object();
    } else {
      Expression bodyExpression = ((ForExpression) body).bodyExpression();
      if (bodyExpression.is(Kind.OBJECT_EXPRESSION)) {
        objectBody = ((ObjectExpression) bodyExpression);
      }
    }
    return Optional.ofNullable(objectBody);
  }

  @Override
  public List<ResourceDeclaration> childResources() {
    return getObjectBody()
      .map(ObjectExpression::nestedResources)
      .orElse(Collections.emptyList());
  }

  @Override
  public boolean existing() {
    if (existing != null) {
      return BicepKeyword.EXISTING.getValue().equals(existing.value());
    } else {
      return false;
    }
  }

  @Override
  public List<Property> resourceProperties() {
    return getObjectBody()
      .map(b -> b.properties().stream()
        .map(Property.class::cast)
        .collect(Collectors.toList()))
      .orElse(List.of());
  }

  @Override
  public List<Decorator> decorators() {
    return decorators;
  }

  public SyntaxToken keyword() {
    return keyword;
  }

  @CheckForNull
  public SyntaxToken getExisting() {
    return existing;
  }

  private static List<Property> propertiesOrEmpty(List<PropertyTree> properties) {
    return properties.stream()
      .filter(propertyTree -> "properties".equals(((TextTree) propertyTree.key()).value()))
      // TODO SONARIAC-1036 ARM Bicep: support Expression other than ObjectExpression for resource properties
      .filter(propertyTree -> ((Property) propertyTree).value().is(Kind.OBJECT_EXPRESSION))
      .map(p -> (Collections.<Property>unmodifiableList(((ObjectExpression) ((Property) p).value()).properties())))
      .findFirst()
      .orElse(List.of());
  }
}
