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
package org.sonar.iac.arm.tree.api;

import java.util.List;
import java.util.Optional;
import javax.annotation.CheckForNull;
import org.sonar.iac.arm.tree.api.bicep.ObjectProperty;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.common.api.tree.HasProperties;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.TextUtils;

public interface ResourceDeclaration extends Statement, HasProperties, ObjectProperty, HasResources {

  /**
   * The name of a resource is case-insensitive. Comparisons to this field should always respect this property.
   * An easy way to do this is via {@link TextUtils#isValue(Tree, String)}.
   * @see <a href=”https://learn.microsoft.com/en-us/azure/azure-resource-manager/management/resource-name-rules”>Microsoft - Naming rules and restrictions for Azure resources</a>
   */
  @CheckForNull
  StringLiteral name();

  @CheckForNull
  Identifier symbolicName();

  /**
   * An API version of the resource. Should always be a {@link StringLiteral} in Bicep, but can be an ARM template expression in JSON.
   * @return An API version of the resource.
   */
  @CheckForNull
  Expression version();

  TextTree type();

  /**
   * Returns list of properties under the "properties" property.
   * <p>
   * For following JSON:
   * <pre>
   *   {@code
   *    {
   *      "$schema": "https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#",
   *      "contentVersion": "1.0.0.0",
   *      "resources": [
   *        {
   *          "type": "Microsoft.ApiManagement/service",
   *          "apiVersion": "2021-08-01",
   *          "name": "example name",
   *          "location": "[parameters('location')]",
   *          "properties": {
   *            "property1": "value1"
   *          }
   *        }
   *      ]
   *    }
   *   }
   * </pre>
   *
   * The {@code property1} will be returned. To read {@code location} property please see {@link ResourceDeclaration#resourceProperties()}.
   */
  List<Property> properties();

  @CheckForNull
  SyntaxToken existing();

  /**
   * Returns list of all top level resource properties.
   */
  List<Property> resourceProperties();

  /**
   * Returns a resource property by the key, if it is present.
   * @param key name of the resource property
   * @return a {@link Property} if it is found or an empty Optional
   */
  default Optional<Property> getResourceProperty(String key) {
    return resourceProperties().stream()
      .filter(p -> TextUtils.isValue(p.key(), key).isTrue())
      .findFirst();
  }
}
