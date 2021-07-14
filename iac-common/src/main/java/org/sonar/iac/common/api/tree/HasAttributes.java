/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.common.api.tree;

import java.util.List;

public interface HasAttributes {
  <T extends AttributeTree> List<T> attributes();
}
