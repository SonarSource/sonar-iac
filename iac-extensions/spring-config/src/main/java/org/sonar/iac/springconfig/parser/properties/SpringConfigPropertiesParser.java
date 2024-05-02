/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.springconfig.parser.properties;

import javax.annotation.Nullable;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.springconfig.tree.api.SpringConfig;

public class SpringConfigPropertiesParser implements TreeParser<Tree> {

  @Override
  public SpringConfig parse(String source, @Nullable InputFileContext inputFileContext) {
    var inputCode = CharStreams.fromString(source);
    var propertiesLexer = new PropertiesLexer(inputCode);
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
