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

import java.util.Collections;
import java.util.List;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.PropertyValue;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;

public class ArrayExpressionImpl extends AbstractArmTreeImpl implements ArrayExpression {
  private final List<PropertyValue> values;
  private final YamlTreeMetadata metadata;

  public ArrayExpressionImpl(YamlTreeMetadata metadata, List<PropertyValue> values) {
    this.metadata = metadata;
    this.values = values;
  }

  @Override
  public TextRange textRange() {
    return metadata.textRange();
  }

  @Override
  public List<Tree> children() {
    return Collections.unmodifiableList(values);
  }

  @Override
  public Kind getKind() {
    return Kind.ARRAY_EXPRESSION;
  }

  @Override
  public List<PropertyValue> values() {
    return values;
  }
}
