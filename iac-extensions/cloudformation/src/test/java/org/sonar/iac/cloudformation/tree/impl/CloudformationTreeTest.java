/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.tree.impl;

import org.sonar.iac.cloudformation.api.tree.FileTree;
import org.sonar.iac.cloudformation.parser.CloudformationParser;

public abstract class CloudformationTreeTest {

  protected FileTree parse(String source) {
    CloudformationParser parser = new CloudformationParser();
    return parser.parse(source, null);
  }
}
