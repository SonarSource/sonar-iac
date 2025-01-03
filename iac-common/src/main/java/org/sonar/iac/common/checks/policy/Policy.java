/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
package org.sonar.iac.common.checks.policy;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;

public class Policy {

  private final Tree version;
  private final Tree id;
  private final List<Statement> statement;

  public <T extends Tree> Policy(T policyDocument, Function<T, List<T>> statementsProvider) {
    this.version = PropertyUtils.valueOrNull(policyDocument, "Version");
    this.id = PropertyUtils.valueOrNull(policyDocument, "Id");
    this.statement = statementsProvider.apply(policyDocument).stream().map(Statement::new).toList();
  }

  public Optional<Tree> version() {
    return Optional.ofNullable(version);
  }

  public Optional<Tree> id() {
    return Optional.ofNullable(id);
  }

  public List<Statement> statement() {
    return statement;
  }

  public static class Statement {
    private final Tree sid;
    private final Tree effect;
    private final Tree principal;
    private final Tree notPrincipal;
    private final Tree action;
    private final Tree notAction;
    private final Tree resource;
    private final Tree notResource;
    private final Tree condition;

    private Statement(Tree statement) {
      this.sid = PropertyUtils.valueOrNull(statement, "Sid");
      this.effect = PropertyUtils.valueOrNull(statement, "Effect");
      this.principal = PropertyUtils.valueOrNull(statement, "Principal");
      this.notPrincipal = PropertyUtils.valueOrNull(statement, "NotPrincipal");
      this.action = PropertyUtils.valueOrNull(statement, "Action");
      this.notAction = PropertyUtils.valueOrNull(statement, "NotAction");
      this.resource = PropertyUtils.valueOrNull(statement, "Resource");
      this.notResource = PropertyUtils.valueOrNull(statement, "NotResource");
      this.condition = PropertyUtils.valueOrNull(statement, "Condition");
    }

    public Optional<Tree> sid() {
      return Optional.ofNullable(sid);
    }

    public Optional<Tree> effect() {
      return Optional.ofNullable(effect);
    }

    public Optional<Tree> principal() {
      return Optional.ofNullable(principal);
    }

    public Optional<Tree> notPrincipal() {
      return Optional.ofNullable(notPrincipal);
    }

    public Optional<Tree> action() {
      return Optional.ofNullable(action);
    }

    public Optional<Tree> notAction() {
      return Optional.ofNullable(notAction);
    }

    public Optional<Tree> resource() {
      return Optional.ofNullable(resource);
    }

    public Optional<Tree> notResource() {
      return Optional.ofNullable(notResource);
    }

    public Optional<Tree> condition() {
      return Optional.ofNullable(condition);
    }
  }
}
