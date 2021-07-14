/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.plugin;

import org.sonar.api.resources.AbstractLanguage;

public class CloudformationLanguage extends AbstractLanguage {

  public static final String KEY = "cloudformation";
  public static final String NAME = "CloudFormation";

  public CloudformationLanguage() {
    super(KEY, NAME);
  }

  @Override
  public String[] getFileSuffixes() {
    return new String[0];
  }
}
