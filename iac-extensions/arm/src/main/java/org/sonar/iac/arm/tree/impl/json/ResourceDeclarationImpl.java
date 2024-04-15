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
package org.sonar.iac.arm.tree.impl.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.annotation.CheckForNull;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.api.tree.Tree;

public class ResourceDeclarationImpl extends AbstractArmTreeImpl implements ResourceDeclaration {

  private final Expression name;
  private final Expression version;
  private final StringLiteral type;
  private final List<Property> properties;
  private final List<Property> resourceProperties;
  private final List<ResourceDeclaration> childResources;

  public ResourceDeclarationImpl(Expression name,
    Expression version,
    StringLiteral type,
    List<Property> properties,
    List<Property> resourceProperties,
    List<ResourceDeclaration> childResources) {
    this.name = name;
    this.version = version;
    this.type = type;
    this.properties = properties;
    this.resourceProperties = resourceProperties;
    this.childResources = childResources;
  }

  @Override
  public List<Tree> children() {
    // We don't need to add "properties" here, as it's included in the "resourceProperties" in order to visit its ObjectExpression
    List<Tree> children = new ArrayList<>();
    children.add(name);
    children.add(version);
    children.add(type);
    Set<String> alreadyAddedChildren = Set.of("type", "apiversion", "name", "resources");
    resourceProperties.forEach(property -> {
      TextTree propertyKey = property.key();
      if (!alreadyAddedChildren.contains(propertyKey.value().toLowerCase(Locale.ROOT))) {
        children.add(propertyKey);
        children.add(property.value());
      }
    });
    children.addAll(childResources);
    return children;
  }

  @Override
  @CheckForNull
  public Expression name() {
    return name;
  }

  @Override
  @CheckForNull
  public Identifier symbolicName() {
    return null;
  }

  @Override
  @CheckForNull
  public SyntaxToken existing() {
    return null;
  }

  @Override
  public Expression version() {
    return version;
  }

  @Override
  public TextTree type() {
    return type;
  }

  @Override
  public List<Property> properties() {
    return properties;
  }

  @Override
  public List<Property> resourceProperties() {
    return resourceProperties;
  }

  @Override
  public List<ResourceDeclaration> childResources() {
    return childResources;
  }

  @Override
  public Kind getKind() {
    return Kind.RESOURCE_DECLARATION;
  }
}
