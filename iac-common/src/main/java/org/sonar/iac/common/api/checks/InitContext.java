/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.common.api.checks;

import java.util.function.BiConsumer;
import org.sonar.iac.common.api.tree.Tree;

public interface InitContext {

  <T extends Tree> void register(Class<T> cls, BiConsumer<CheckContext, T> visitor);
}
