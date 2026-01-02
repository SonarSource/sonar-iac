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
package org.sonar.iac.docker.checks;

import java.util.function.BiConsumer;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.visitors.TreeContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.docker.checks.utils.MultiStageBuildInspector;
import org.sonar.iac.docker.tree.api.Body;

public abstract class AbstractFinalImageCheck implements IacCheck {

  private final TreeVisitor<FinalImageContext> visitor = new TreeVisitor<>();

  @Override
  public void initialize(InitContext init) {
    init.register(Body.class, this::processBody);
    initializeOnFinalImage();
  }

  protected abstract void initializeOnFinalImage();

  protected <T extends Tree> void register(Class<T> cls, BiConsumer<CheckContext, T> consumer) {
    visitor.register(cls, (ctx, node) -> consumer.accept(ctx.checkContext, node));
  }

  private void processBody(CheckContext checkContext, Body body) {
    var multiStageBuildInspector = MultiStageBuildInspector.of(body);
    var stages = body.dockerImages();
    var finalImageContext = new FinalImageContext(checkContext);

    stages.stream()
      .filter(multiStageBuildInspector::isStageInFinalImage)
      .forEach(stage -> visitor.scan(finalImageContext, stage));
  }

  static class FinalImageContext extends TreeContext {
    public final CheckContext checkContext;

    public FinalImageContext(CheckContext checkContext) {
      this.checkContext = checkContext;
    }
  }
}
