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
lexer grammar PropertiesLexer;

// Adds support for CR as line terminator into the generated Java code
@members {
public PropertiesLexer(CharStream input, boolean crLexerCostructor) {
  super(input);
  _interp = new CRLexerATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
}
}

COMMENT         : [!#] -> pushMode(COMMENT_MODE);
NEWLINE         : [ \t\f\r\n\u2028\u2029]+ -> channel(HIDDEN);
DELIMITER       : [ ]* [:=\t\f ] [ ]* -> pushMode(VALUE_MODE);
CHARACTER       : ~ [!#:=\t\f ] -> pushMode(KEY_MODE);

mode KEY_MODE;

KEY_DELIMITER   : [ ]* [:=\t\f ] [ ]* -> type(DELIMITER), popMode, pushMode(VALUE_MODE);
KEY_SLASH       : '\\' -> more, pushMode(INSIDE);
KEY_CHARACTER   : ~ [ :=\t\f\r\n\u2028\u2029] -> type(CHARACTER);

mode COMMENT_MODE;

COMMENT_NEW_LINE  : [\r\n\u2028\u2029]+    -> type(NEWLINE), popMode;
COMMENT_CHAR      : ~ [\r\n\u2028\u2029]   -> type(CHARACTER);

mode INSIDE;

SLASH_DELIMITER : ~[\r\n]    -> type(CHARACTER), popMode;
SLASH_JOINT     : '\r'? '\n' -> channel(HIDDEN), pushMode(IGNORE_LEADING_SPACES);

mode IGNORE_LEADING_SPACES;

NOT_SPACE    : ~[ ]  -> type(CHARACTER), popMode;
IGNORE_SPACE : [ ]+   -> channel(HIDDEN), popMode;

mode VALUE_MODE;

VALUE_TERM      : [\r\n\u2028\u2029]+    -> type(NEWLINE), popMode;
VALUE_SLASH     : '\\'                   -> more, pushMode(INSIDE);
VALUE_CHARACTER : ~ [\r\n\u2028\u2029]   -> type(CHARACTER);
