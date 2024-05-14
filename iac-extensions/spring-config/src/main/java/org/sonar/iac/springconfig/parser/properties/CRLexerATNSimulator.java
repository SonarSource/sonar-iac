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
 * ANTLR does not handle correctly locations when the input contains CR (carriage return - \r) as a line separator.
 * The \r\n is handled correctly.
 * The \r as line separator is rare, but it should be supported.
 * This class fixes the behaviour.
 * The ANTLR author opinion:
 * https://github.com/antlr/antlr4/pull/2519#issuecomment-1008069063
 */
public class CRLexerATNSimulator extends LexerATNSimulator {

  private static final int CURRENT_CHAR_TO_CONSUME = 1;
  private static final int NEXT_CHAR_TO_CONSUME = 2;

  public CRLexerATNSimulator(Lexer recog, ATN atn, DFA[] dfas, PredictionContextCache sharedContextCache) {
    super(recog, atn, dfas, sharedContextCache);
  }

  @Override
  public void consume(CharStream input) {
    int curChar = input.LA(CURRENT_CHAR_TO_CONSUME);
    if (curChar == '\n') {
      line++;
      charPositionInLine = 0;
    } else if (curChar == '\r') {
      int nextChar = input.LA(NEXT_CHAR_TO_CONSUME);
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
