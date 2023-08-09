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
package org.sonar.iac.arm.tree.impl.json;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.TextUtils;

public class ResourceDeclarationImpl extends AbstractArmTreeImpl implements ResourceDeclaration {

  private final StringLiteral name;
  private final StringLiteral version;
  private final StringLiteral type;
  private final List<Property> properties;
  private final List<Property> resourceProperties;
  private final List<ResourceDeclaration> childResources;

  public ResourceDeclarationImpl(StringLiteral name,
    StringLiteral version,
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
    List<Tree> children = new ArrayList<>();
    children.add(name);
    children.add(version);
    children.add(type);
    properties.forEach(property -> {
      children.add(property.key());
      children.add(property.value());
    });
    children.addAll(childResources);
    return children;
  }

  /**
   * The name of a resource is case-insensitive. Comparisons to this field should always respect this property.
   * An easy way to do this is via {@link TextUtils#isValue(Tree, String)}.
   * @see <a href=”https://learn.microsoft.com/en-us/azure/azure-resource-manager/management/resource-name-rules”>Microsoft - Naming rules and restrictions for Azure resources</a>
   */
  @Override
  @CheckForNull
  public StringLiteral name() {
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
  public StringLiteral version() {
    return version;
  }

  @Override
  public StringLiteral type() {
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
