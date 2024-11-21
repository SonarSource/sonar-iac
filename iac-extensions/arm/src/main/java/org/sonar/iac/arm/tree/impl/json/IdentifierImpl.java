/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.arm.tree.impl.json;

import java.util.List;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;

public class IdentifierImpl extends AbstractArmTreeImpl implements Identifier {

  private final String identifier;
  private final YamlTreeMetadata metadata;

  public IdentifierImpl(String identifier, YamlTreeMetadata metadata) {
    this.identifier = identifier;
    this.metadata = metadata;
  }

  @Override
  public String value() {
    return identifier;
  }

  @Override
  public TextRange textRange() {
    return metadata.textRange();
  }

  @Override
  public List<Tree> children() {
    return List.of();
  }

  @Override
  public Kind getKind() {
    return Kind.IDENTIFIER;
  }
}
