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

import com.sonar.sslr.api.typed.Optional;
import org.sonar.iac.arm.tree.api.bicep.ImportDeclaration;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.arm.tree.impl.bicep.importdecl.ImportAsClause;
import org.sonar.iac.arm.tree.impl.bicep.importdecl.ImportWithClause;
import org.sonar.iac.common.api.tree.Tree;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ImportDeclarationImpl extends AbstractArmTreeImpl implements ImportDeclaration {
  private final SyntaxToken keyword;
  private final InterpolatedString specification;
  @Nullable
  private final ImportWithClause withClause;
  @Nullable
  private final ImportAsClause asClause;
  private final SyntaxToken newLine;

  public ImportDeclarationImpl(SyntaxToken keyword, InterpolatedString specification, Optional<ImportWithClause> withClause, Optional<ImportAsClause> asClause,
    SyntaxToken newLine) {
    this.keyword = keyword;
    this.specification = specification;
    this.withClause = withClause.orNull();
    this.asClause = asClause.orNull();
    this.newLine = newLine;
  }

  @Override
  public List<Tree> children() {
    List<Tree> result = new ArrayList<>();
    result.add(keyword);
    result.add(specification);
    if (withClause != null) {
      result.addAll(withClause.children());
    }
    if (asClause != null) {
      result.addAll(asClause.children());
    }
    result.add(newLine);
    return result;
  }
}
