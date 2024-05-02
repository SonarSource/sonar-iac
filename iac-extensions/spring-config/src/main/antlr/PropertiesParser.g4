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
