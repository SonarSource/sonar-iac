/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.jvmframeworkconfig.parser.properties;

import javax.annotation.Nullable;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.jvmframeworkconfig.tree.api.JvmFrameworkConfig;

public class JvmFrameworkConfigPropertiesParser implements TreeParser<Tree> {

  @Override
  public JvmFrameworkConfig parse(String source, @Nullable InputFileContext inputFileContext) {
    var inputCode = CharStreams.fromString(source);
    var propertiesLexer = new PropertiesLexer(inputCode, true);
    var commonTokenStream = new CommonTokenStream(propertiesLexer);
    var parser = new PropertiesParser(commonTokenStream);

    var listener = new ErrorListener(inputFileContext);
    parser.removeErrorListeners();
    parser.addErrorListener(listener);

    var propertiesFileContext = parser.propertiesFile();
    var visitor = new PropertiesParseTreeVisitor();
    return visitor.visitPropertiesFile(propertiesFileContext);
  }
}
