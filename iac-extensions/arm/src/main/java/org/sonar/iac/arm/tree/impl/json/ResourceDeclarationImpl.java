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
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class ResourceDeclarationImpl extends AbstractArmTreeImpl implements ResourceDeclaration {

  @Nullable
  private final ResourceDeclaration parentResource;
  private final Identifier name;
  private final StringLiteral version;
  private final StringLiteral type;
  private final List<Property> properties;

  public ResourceDeclarationImpl(@Nullable ResourceDeclaration parentResource, Identifier name, StringLiteral version, StringLiteral type, List<Property> properties) {
    this.parentResource = parentResource;
    this.name = name;
    this.version = version;
    this.type = type;
    this.properties = properties;
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
  @CheckForNull
  public ResourceDeclaration parentResource() {
    return parentResource;
  }

  @Override
  public List<Property> properties() {
    return properties;
  }

  @Override
  public Kind getKind() {
    return Kind.RESOURCE_DECLARATION;
  }
}
