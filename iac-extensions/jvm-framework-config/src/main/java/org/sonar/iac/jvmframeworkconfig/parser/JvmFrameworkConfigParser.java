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
package org.sonar.iac.jvmframeworkconfig.parser;

import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.jvmframeworkconfig.parser.properties.JvmFrameworkConfigPropertiesParser;
import org.sonar.iac.jvmframeworkconfig.parser.yaml.JvmFrameworkConfigYamlParser;
import org.sonar.iac.jvmframeworkconfig.tree.api.JvmFrameworkConfig;

import static org.sonar.iac.jvmframeworkconfig.plugin.JvmFrameworkConfigSensor.isPropertiesFile;
import static org.sonar.iac.jvmframeworkconfig.plugin.JvmFrameworkConfigSensor.isYamlFile;

public class JvmFrameworkConfigParser implements TreeParser<Tree> {

  private static final JvmFrameworkConfigYamlParser YAML_PARSER = new JvmFrameworkConfigYamlParser();
  private static final JvmFrameworkConfigPropertiesParser PROPERTIES_PARSER = new JvmFrameworkConfigPropertiesParser();

  @Override
  public JvmFrameworkConfig parse(String source, @Nullable InputFileContext inputFileContext) {
    if (inputFileContext != null) {
      if (isYamlFile(inputFileContext)) {
        return YAML_PARSER.parse(source, inputFileContext);
      } else if (isPropertiesFile(inputFileContext)) {
        return PROPERTIES_PARSER.parse(source, inputFileContext);
      }
    }
    throw ParseException.createParseException("Unsupported file extension", inputFileContext, null);
  }
}
