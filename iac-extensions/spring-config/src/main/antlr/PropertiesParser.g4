parser grammar PropertiesParser;

options {
    tokenVocab = PropertiesLexer;
}

propertiesFile
    : (LEADING_SPACING row)? row* EOF
    ;

row
    : line
    | comment
    ;

line
    : key (DELIMITER value = key?)? eol
    ;

key
    : CHARACTER+
    ;

eol
    : NEWLINE+
    | EOF
    ;

commentText
    : CHARACTER*
    ;

commentStartAndText
    : COMMENT commentText
    ;

comment
    : commentStartAndText eol
    ;
