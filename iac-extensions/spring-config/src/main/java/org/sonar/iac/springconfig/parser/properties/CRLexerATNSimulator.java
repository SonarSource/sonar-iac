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

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

/**
 * The ANTLR does not handle correctly locations when input contains CR (carriage return - \r) as a line separator.
 * The \r\n is handle correctly.
 * The \r as line separator is rare, but it should be supported.
 * This class fix the behaviour.
 * The ANTLR author opinion:
 * https://github.com/antlr/antlr4/pull/2519#issuecomment-1008069063
 */
public class CRLexerATNSimulator extends LexerATNSimulator {
  public CRLexerATNSimulator(Lexer recog, ATN atn, DFA[] dfas, PredictionContextCache sharedContextCache) {
    super(recog, atn, dfas, sharedContextCache);
  }

  @Override
  public void consume(CharStream input) {
    int curChar = input.LA(1);
    if (curChar == '\n') {
      line++;
      charPositionInLine = 0;
    } else if (curChar == '\r') {
      int nextChar = input.LA(2);
      if (nextChar != '\n') {
        line++;
        charPositionInLine = 0;
      }
    } else {
      charPositionInLine++;
    }
    input.consume();
  }
}
