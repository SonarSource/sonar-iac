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
package org.sonar.iac.terraform.checks.gcp;

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.terraform.api.tree.ObjectElementTree;
import org.sonar.iac.terraform.api.tree.ObjectTree;
import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;
import org.sonar.iac.terraform.symbols.AttributeSymbol;
import org.sonar.iac.terraform.symbols.ResourceSymbol;

import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isFalse;

@Rule(key = "S6405")
public class ComputeInstanceSshKeysCheck extends AbstractNewResourceCheck {

  private static final String MESSAGE = "Make sure that enabling project-wide SSH keys is safe here.";
  private static final String OMITTING_MESSAGE = "Omitting \"metadata.block-project-ssh-keys\" enables project-wide SSH keys. Make sure it is safe here.";

  @Override
  protected void registerResourceConsumer() {
    register(List.of("google_compute_instance", "google_compute_instance_template"),
      resource -> checkMetadata(resource, true));

    register("google_compute_instance_from_template",
      resource -> checkMetadata(resource, false));
  }

  /**
   * @param resource the target resource (of type 'google_compute_instance' or 'google_compute_instance_from_template')
   * @param reportOnMissing should we report in case of undefined 'block-project-ssh-keys' (or part of its path)?
   */
  private static void checkMetadata(ResourceSymbol resource, boolean reportOnMissing) {
    AttributeSymbol metadata = resource.attribute("metadata");

    if (metadata.isAbsent()) {
      if (reportOnMissing) {
        metadata.reportIfAbsent(OMITTING_MESSAGE);
      }
      return;
    }

    if (!(metadata.tree.value() instanceof ObjectTree)) {
      return;
    }

    var metadataObj = (ObjectTree) metadata.tree.value();
    PropertyUtils.get(metadataObj, "block-project-ssh-keys", ObjectElementTree.class)
      .ifPresentOrElse(sshKeysProperty -> {
        if (isFalse().test(sshKeysProperty.value())) {
          metadata.ctx.reportIssue(sshKeysProperty, MESSAGE);
        }
      }, () -> {
        if (reportOnMissing) {
          metadata.ctx.reportIssue(metadata.tree.key(), OMITTING_MESSAGE);
        }
      });
  }
}
