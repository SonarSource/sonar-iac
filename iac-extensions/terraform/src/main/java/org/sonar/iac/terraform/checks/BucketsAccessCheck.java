/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;

import java.util.function.Predicate;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static org.sonar.iac.terraform.checks.utils.PredicateUtils.exactMatchStringPredicate;

@Rule(key = "S6265")
public class BucketsAccessCheck extends AbstractResourceCheck {
  private static final String MESSAGE = "Make sure granting access to %s group is safe here.";
  private static final String SECONDARY_MSG = "Related bucket";

  @Override
  protected void registerResourceChecks() {
    register(BucketsAccessCheck::checkBucket, S3_BUCKET);
  }

  private static final Predicate<String> PUBLIC_READ_OR_WRITE = exactMatchStringPredicate("public-read(-write)?", CASE_INSENSITIVE);

  private static void checkBucket(CheckContext ctx, BlockTree tree) {
    PropertyUtils.get(tree, "acl", AttributeTree.class)
      .ifPresent(acl -> {
        if (TextUtils.matchesValue(acl.value(), PUBLIC_READ_OR_WRITE).isTrue()) {
          ctx.reportIssue(acl, String.format(MESSAGE, "AllUsers"), new SecondaryLocation(tree.labels().get(0), SECONDARY_MSG));
        } else if (TextUtils.isValue(acl.value(), "authenticated-read").isTrue()) {
          ctx.reportIssue(acl, String.format(MESSAGE, "AuthenticatedUsers"), new SecondaryLocation(tree.labels().get(0), SECONDARY_MSG));
        }
      });
  }
}
