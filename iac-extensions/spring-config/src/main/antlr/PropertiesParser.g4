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
parser grammar PropertiesParser;

options {
    tokenVocab = PropertiesLexer;
}

propertiesFile
    : row* EOF
    ;

row
    : comment
    | line
    | invalidLine
    ;

line
    : key (DELIMITER value = key?)? eol
    ;

key
    : CHARACTER+
    ;

eol
    : WHITESPACE+
    | EOF
    ;

// The lines that starts from delimiter doesn't make much sense but Spring accept such entries without any error.
// This is a separate rule because we don't want to insert such lines into the AST in the visitor from the regular visitLine method.
invalidLine
    : DELIMITER value = key? eol
    ;

comment
    : commentStartAndText eol
    ;

commentStartAndText
    : COMMENT commentText
    ;

commentText
    : CHARACTER*
    ;
