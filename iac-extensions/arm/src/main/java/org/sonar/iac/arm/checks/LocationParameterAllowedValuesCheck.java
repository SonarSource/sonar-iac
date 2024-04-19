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

import java.util.Locale;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.arm.symbols.Symbol;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.HasSymbol;
import org.sonar.iac.arm.tree.api.ParameterDeclaration;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.impl.bicep.ParameterDeclarationImpl;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;

@Rule(key = "S6953")
public class LocationParameterAllowedValuesCheck implements IacCheck {

  private static final String MESSAGE_BICEP = "Remove this @allowed decorator from the parameter specifying the location.";
  private static final String MESSAGE_JSON = "Remove this allowedValues property from the parameter specifying the location.";

  @Override
  public void initialize(InitContext init) {
    init.register(ResourceDeclaration.class, LocationParameterAllowedValuesCheck::checkResourceLocation);
  }

  private static void checkResourceLocation(CheckContext checkContext, ResourceDeclaration resourceDeclaration) {
    resourceDeclaration.resourceProperties().stream()
      .filter(property -> "location".equals(property.key().value().toLowerCase(Locale.ROOT)))
      .map(Property::value)
      .forEach(expression -> reportIfParameterWithAllowedValuesIsUsed(checkContext, expression));
  }

  private static void reportIfParameterWithAllowedValuesIsUsed(CheckContext checkContext, Expression expression) {
    if (expression instanceof HasSymbol expressionWithSymbol) {
      Optional.ofNullable(expressionWithSymbol.symbol())
        .map(Symbol::findAssignmentDeclaration)
        .filter(declaration -> declaration.is(ArmTree.Kind.PARAMETER_DECLARATION))
        .map(ParameterDeclaration.class::cast)
        .filter(parameterDeclaration -> !parameterDeclaration.allowedValues().isEmpty())
        .ifPresent(declaration -> reportOnDeclaration(checkContext, declaration));
    }
  }

  private static void reportOnDeclaration(CheckContext checkContext, ParameterDeclaration declaration) {
    ArmTree treeToRaiseOn = declaration.allowedValues().get(0).parent();
    boolean isBicep = declaration instanceof ParameterDeclarationImpl;
    if (isBicep) {
      treeToRaiseOn = treeToRaiseOn.parent();
    }
    if (treeToRaiseOn != null) {
      checkContext.reportIssue(treeToRaiseOn, isBicep ? MESSAGE_BICEP : MESSAGE_JSON);
    }
  }
}
