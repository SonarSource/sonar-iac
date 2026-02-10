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
package org.sonar.iac.docker.checks.utils;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.docker.tree.api.ArgumentList;
import org.sonar.iac.docker.tree.api.CodeInstruction;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.Flag;
import org.sonar.iac.docker.tree.api.TransferInstruction;

public final class CheckUtils {

  private static final String TAG_SEPARATOR = ":";
  private static final String DIGEST_SEPARATOR = "@";

  private CheckUtils() {
    // utils class
  }

  public static Optional<ArgumentList> codeToArgumentList(CodeInstruction codeInstruction) {
    if (codeInstruction.code() instanceof ArgumentList argumentList) {
      return Optional.of(argumentList);
    }
    return Optional.empty();
  }

  public static Optional<Flag> getParamByName(Collection<Flag> params, String name) {
    return params.stream().filter(param -> name.equals(param.name())).findFirst();
  }

  public static <T extends TransferInstruction> BiConsumer<CheckContext, T> ignoringHeredoc(BiConsumer<CheckContext, T> visitor) {
    return ignoringSpecificForms(Set.of(DockerTree.Kind.HEREDOCUMENT), visitor);
  }

  public static <T extends TransferInstruction> BiConsumer<CheckContext, T> ignoringSpecificForms(Collection<DockerTree.Kind> formKinds, BiConsumer<CheckContext, T> visitor) {
    return (ctx, hasArguments) -> {
      var kind = hasArguments.srcsAndDest().getKind();
      if (kind == null || !formKinds.contains(kind)) {
        visitor.accept(ctx, hasArguments);
      }
    };
  }

  public static boolean isScratchImage(String imageName) {
    return "scratch".equals(imageName);
  }

  public static Optional<String> getImageTag(String imageName) {
    int tagSeparatorIndex = imageName.indexOf(TAG_SEPARATOR);
    int digestSeparatorIndex = imageName.indexOf(DIGEST_SEPARATOR);
    if (tagSeparatorIndex < 0) {
      return Optional.empty();
    }
    if (digestSeparatorIndex < 0) {
      return Optional.of(imageName.substring(tagSeparatorIndex + 1));
    }
    // Tag is empty if tag separator is after digest separator as tag separator in image name is part of digest
    if (tagSeparatorIndex > digestSeparatorIndex) {
      return Optional.empty();
    }
    return Optional.of(imageName.substring(tagSeparatorIndex + 1, digestSeparatorIndex));
  }

  public static Optional<String> getImageDigest(String imageName) {
    int digestSeparatorIndex = imageName.indexOf(DIGEST_SEPARATOR);
    if (digestSeparatorIndex < 0) {
      return Optional.empty();
    }
    return Optional.of(imageName.substring(digestSeparatorIndex + 1));
  }
}
