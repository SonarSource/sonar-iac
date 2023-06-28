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
package org.sonar.iac.arm.checks;

import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6388")
public class UnencryptedCloudServicesCheck extends AbstractArmResourceCheck {

  public static final String FORMAT_OMITTING = "Omitting \"%s\" enables clear-text storage. Make sure it is safe here.";

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Compute/virtualMachines",
      resource -> resource.object("storageProfile").list("dataDisks").objects().forEach(
        dataDisk -> dataDisk.object("managedDisk").object("diskEncryptionSet")
          .reportIfAbsent(FORMAT_OMITTING)
          .property("id")
          .reportIf(isEmpty(), String.format(FORMAT_OMITTING, "id"))
          .reportIfAbsent(FORMAT_OMITTING)
    ));
  }

  private static Predicate<Expression> isEmpty() {
    return e -> TextUtils.matchesValue(e, ""::equals).isTrue();
  }
}
