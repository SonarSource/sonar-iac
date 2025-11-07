/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
package org.sonar.iac.jvmframeworkconfig.parser.properties;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

public class PropertiesTestUtils {

  public static PropertiesParser.PropertiesFileContext createPropertiesFileContext(String code) {
    var inputCode = CharStreams.fromString(code);
    var propertiesLexer = new PropertiesLexer(inputCode, true);
    var commonTokenStream = new CommonTokenStream(propertiesLexer);
    var parser = new PropertiesParser(commonTokenStream);

    // printing tokens for debugging
    commonTokenStream.fill();
    for (Token token : commonTokenStream.getTokens()) {
      System.out.println(((CommonToken) token).toString(parser));
    }

    var listener = new ErrorListener(null);
    parser.addErrorListener(listener);
    return parser.propertiesFile();
  }
}
