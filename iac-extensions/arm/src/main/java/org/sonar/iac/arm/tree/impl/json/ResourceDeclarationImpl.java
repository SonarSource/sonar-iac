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
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class ResourceDeclarationImpl extends AbstractArmTreeImpl implements ResourceDeclaration {

  private final Identifier name;
  private final StringLiteral version;
  private final StringLiteral type;
  private final List<Property> properties;
  private final List<ResourceDeclaration> childResources;

  public ResourceDeclarationImpl(Identifier name, StringLiteral version, StringLiteral type, List<Property> properties, List<ResourceDeclaration> childResources) {
    this.name = name;
    this.version = version;
    this.type = type;
    this.properties = properties;
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

  @Override
  public Identifier name() {
    return name;
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
  public List<ResourceDeclaration> childResources() {
    return childResources;
  }

  @Override
  public Kind getKind() {
    return Kind.RESOURCE_DECLARATION;
  }
}
