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
package org.sonar.iac.terraform.checks.gcp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.extension.visitors.TreeContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.symbols.ResourceSymbol;

import static org.sonar.iac.terraform.api.tree.TerraformTree.Kind.ATTRIBUTE_ACCESS;
import static org.sonar.iac.terraform.checks.AbstractNewResourceCheck.isResource;
import static org.sonar.iac.terraform.checks.AbstractNewResourceCheck.resourceType;
import static org.sonar.iac.terraform.checks.utils.TerraformUtils.attributeAccessToString;

class PolicyReferenceCollector extends TreeVisitor<TreeContext> {
  private final Map<String, List<AttributeTree>> policyReferences = new HashMap<>();
  private final Set<String> relevantResources;

  public PolicyReferenceCollector(Set<String> relevantResourceTypes) {
    relevantResources = relevantResourceTypes;
    register(BlockTree.class, (ctx, tree) -> {
      if (isResource(tree) && relevantResources.contains(resourceType(tree))) {
        collectReference(tree);
      }
    });
  }

  private void collectReference(BlockTree tree) {
    PropertyUtils.get(tree, "policy_data", AttributeTree.class)
      .filter(policyData -> policyData.value().is(ATTRIBUTE_ACCESS))
      .ifPresent(policyData -> {
        String key = attributeAccessToString((AttributeAccessTree) policyData.value());
        policyReferences.computeIfAbsent(key, s -> new ArrayList<>()).add(policyData);
      });
  }

  private List<AttributeTree> getReferrersList(String policyDataName) {
    List<AttributeTree> res = policyReferences.get(String.format("data.google_iam_policy.%s.policy_data", policyDataName));
    return res == null ? Collections.emptyList() : res;
  }

  public void checkPolicy(ResourceSymbol data, Predicate<ExpressionTree> isSensitiveRole, String primaryMsg, String secondaryMsg) {
    List<AttributeTree> referrers = getReferrersList(data.name);
    if (referrers.isEmpty()) {
      return;
    }

    SecondaryLocation[] secondary = referrers.stream()
      .map(referrer -> new SecondaryLocation(referrer, secondaryMsg))
      .toArray(SecondaryLocation[]::new);

    data.blocks("binding").forEach(
      binding -> binding.attribute("role")
        .reportIf(isSensitiveRole, primaryMsg, secondary));
  }
}
