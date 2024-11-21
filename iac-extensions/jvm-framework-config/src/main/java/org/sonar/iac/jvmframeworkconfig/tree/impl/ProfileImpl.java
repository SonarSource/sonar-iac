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
package org.sonar.iac.jvmframeworkconfig.tree.impl;

import java.util.ArrayList;
import java.util.List;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.jvmframeworkconfig.tree.api.Profile;
import org.sonar.iac.jvmframeworkconfig.tree.api.Tuple;

public class ProfileImpl extends AbstractJvmFrameworkConfigImpl implements Profile {
  private final List<Tuple> properties;
  private final List<Comment> comments;
  private final String name;
  private final boolean active;

  public ProfileImpl(List<Tuple> properties, List<Comment> comments, String name, boolean active) {
    this.properties = properties;
    this.comments = comments;
    this.name = name;
    this.active = active;
  }

  @Override
  public List<Tree> children() {
    return new ArrayList<>(properties);
  }

  @Override
  public List<Tuple> properties() {
    return properties;
  }

  @Override
  public List<Comment> comments() {
    return comments;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public boolean isActive() {
    return active;
  }
}
