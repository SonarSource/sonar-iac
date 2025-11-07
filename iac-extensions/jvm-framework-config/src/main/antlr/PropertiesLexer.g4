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
lexer grammar PropertiesLexer;

// Adds support for CR as line terminator into the generated Java code
@members {
public PropertiesLexer(CharStream input, boolean crLexerConstructor) {
  super(input);
  _interp = new CRLexerATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
}
}

COMMENT         : [!#]                     -> pushMode(COMMENT_MODE);
WHITESPACE      : [ \t\f\r\n\u2028\u2029]+ -> channel(HIDDEN);
DELIMITER       : [ ]* [:=\t\f ] [ ]*      -> pushMode(VALUE_MODE);
CHARACTER       : ~ [!#:=\t\f ]            -> pushMode(KEY_MODE);

mode KEY_MODE;

KEY_DELIMITER   : [ ]* [:=\t\f ] [ ]*         -> type(DELIMITER), popMode, pushMode(VALUE_MODE);
KEY_TERM        : [\r\n\u2028\u2029]+         -> type(WHITESPACE), popMode;
KEY_SLASH       : '\\'                        -> more, pushMode(INSIDE);
KEY_CHARACTER   : ~ [ :=\t\f\r\n\u2028\u2029] -> type(CHARACTER);

mode COMMENT_MODE;

COMMENT_NEW_LINE  : [\r\n\u2028\u2029]+    -> type(WHITESPACE), popMode;
COMMENT_CHAR      : ~ [\r\n\u2028\u2029]   -> type(CHARACTER);

mode INSIDE;

SLASH_DELIMITER : ~[\r\n\u2028\u2029]    -> type(CHARACTER), popMode;
SLASH_JOINT     : [\r\n\u2028\u2029]+    -> channel(HIDDEN), pushMode(IGNORE_LEADING_SPACES);

mode IGNORE_LEADING_SPACES;

NOT_SPACE    : ~[ ]   -> type(CHARACTER), popMode;
IGNORE_SPACE : [ ]+   -> channel(HIDDEN), popMode;

mode VALUE_MODE;

VALUE_TERM      : [\r\n\u2028\u2029]+    -> type(WHITESPACE), popMode;
VALUE_SLASH     : '\\'                   -> more, pushMode(INSIDE);
VALUE_CHARACTER : ~ [\r\n\u2028\u2029]   -> type(CHARACTER);
