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
package org.sonar.iac.arm.checks;

import java.util.Map;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.bicep.HasKeyword;
import org.sonar.iac.arm.tree.impl.bicep.FileImpl;
import org.sonar.iac.arm.tree.impl.bicep.MetadataDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.ModuleDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.OutputDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.ParameterDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.ResourceDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.TargetScopeDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.VariableDeclarationImpl;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;

public class ElementsOrderTopLevelBicep implements IacCheck {

  private static final String MESSAGE = "Reorder the elements to match the recommended order.";

  private static final Map<String, Integer> expectedOrder = Map.of(
    ArmTree.Kind.TARGET_SCOPE_DECLARATION.name(), 0,
    ArmTree.Kind.METADATA_DECLARATION.name(), 1,
    ArmTree.Kind.PARAMETER_DECLARATION.name(), 2,
    ArmTree.Kind.FUNCTION_DECLARATION.name(), 3,
    ArmTree.Kind.VARIABLE_DECLARATION.name(), 4,
    ArmTree.Kind.RESOURCE_DECLARATION.name() + "EXISTING", 5,
    ArmTree.Kind.RESOURCE_DECLARATION.name(), 6,
    ArmTree.Kind.MODULE_DECLARATION.name(), 7,
    ArmTree.Kind.OUTPUT_DECLARATION.name(), 8);

  private String lastKind = ArmTree.Kind.TARGET_SCOPE_DECLARATION.name();
  private boolean issueFound;

  @Override
  public void initialize(InitContext init) {
    init.register(FileImpl.class, this::checkFile);
    init.register(TargetScopeDeclarationImpl.class, this::checkDeclaration);
    init.register(MetadataDeclarationImpl.class, this::checkDeclaration);
    init.register(ParameterDeclarationImpl.class, this::checkDeclaration);
    init.register(VariableDeclarationImpl.class, this::checkDeclaration);
    init.register(ResourceDeclarationImpl.class, this::checkResourceDeclaration);
    init.register(ModuleDeclarationImpl.class, this::checkDeclaration);
    init.register(OutputDeclarationImpl.class, this::checkDeclaration);
  }

  private void checkFile(CheckContext checkContext, FileImpl file) {
    lastKind = ArmTree.Kind.TARGET_SCOPE_DECLARATION.name();
    issueFound = false;
  }

  private void checkDeclaration(CheckContext checkContext, HasKeyword tree) {
    if (!issueFound) {
      var kind = tree.getKind().name();
      if (expectedOrder.get(kind) < expectedOrder.get(lastKind)) {
        checkContext.reportIssue(tree.keyword(), MESSAGE);
        issueFound = true;
      } else {
        lastKind = kind;
      }
    }
  }

  private void checkResourceDeclaration(CheckContext checkContext, ResourceDeclarationImpl tree) {
    if (!issueFound) {
      var existing = "";
      if (tree.existing() != null) {
        existing = "EXISTING";
      }
      var kind = tree.getKind().name() + existing;

      if (expectedOrder.get(kind) < expectedOrder.get(lastKind) && tree.parent() instanceof File) {
        checkContext.reportIssue(tree.keyword(), MESSAGE);
        issueFound = true;
      } else {
        lastKind = kind;
      }
    }
  }
}
