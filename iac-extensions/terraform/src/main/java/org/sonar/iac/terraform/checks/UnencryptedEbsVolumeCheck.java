/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.iac.terraform.checks;

import java.util.List;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.LabelTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;

@Rule(key = "S6275")
public class UnencryptedEbsVolumeCheck extends AbstractResourceCheck {

  private static final String[] PROPERTIES = new String[] {"root_block_device", "ebs_block_device"};
  private static final String MESSAGE = "Make sure that using unencrypted volumes is safe here.";

  @Override
  protected void checkResource(CheckContext ctx, BlockTree resource) {
    List<LabelTree> labels = resource.labels();
    if (labels.isEmpty()) {
      return;
    }
    String resourceName = labels.get(0).value();
    switch (resourceName) {
      case "aws_ebs_encryption_by_default":
        checkEncrypted(ctx, resource, "enabled", true);
        break;
      case "aws_ebs_volume":
        checkEncrypted(ctx, resource, "encrypted", false);
        break;
      case "aws_launch_configuration":
        checkEncryptionProperties(ctx, resource);
        break;
      default:
        // do nothing
    }
  }

  private static void checkEncryptionProperties(CheckContext ctx, BlockTree resource) {
    for (String propertyName : PROPERTIES) {
      PropertyUtils.get(resource, propertyName, BlockTree.class)
        .ifPresent(propertyTree -> checkEncrypted(ctx, propertyTree, "encrypted", false));
    }
  }

  private static void checkEncrypted(CheckContext ctx, BlockTree tree, String key, boolean secureByDefault) {
    Optional<LiteralExprTree> booleanProperty = PropertyUtils.value(tree, key, TerraformTree.class)
      .filter(x -> x.is(TerraformTree.Kind.BOOLEAN_LITERAL))
      .map(LiteralExprTree.class::cast);

    if (!booleanProperty.isPresent()) {
      if (secureByDefault) {
        return;
      }
      List<LabelTree> labels = tree.labels();
      ctx.reportIssue(labels.isEmpty() ? tree.key() : labels.get(0), MESSAGE);
      return;
    }

    booleanProperty
      .filter(TextUtils::isValueFalse)
      .ifPresent(value -> ctx.reportIssue(value, MESSAGE));
  }
}
