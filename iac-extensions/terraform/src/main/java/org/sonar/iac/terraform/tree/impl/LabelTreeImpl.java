/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.terraform.tree.impl;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.LabelTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

public class LabelTreeImpl extends TerraformTreeImpl implements LabelTree {
  private static final Pattern ENCLOSING_QUOTE = Pattern.compile("(^\")|(\"$)");
  private final SyntaxToken token;

  public LabelTreeImpl(SyntaxToken token) {
    this.token = token;
  }

  @Override
  public SyntaxToken token() {
    return token;
  }

  @Override
  public String value() {
    // Terraform allows labels to be quoted or unquoted. The meaning however is the same.
    return ENCLOSING_QUOTE.matcher(token.value()).replaceAll("");
  }

  @Override
  public List<Tree> children() {
    return Collections.singletonList(token);
  }

  @Override
  public Kind getKind() {
    return Kind.LABEL;
  }
}
