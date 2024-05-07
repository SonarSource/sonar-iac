parser grammar PropertiesParser;

options {
    tokenVocab = PropertiesLexer;
}

propertiesFile
    : LEADING_SPACING? row* EOF
    ;

row
    : comment
    | line
    ;

line
    : key (DELIMITER value = key?)? eol
    ;

key
    : CHARACTER (CHARACTER|COMMENT)*
    ;

eol
    : NEWLINE+
    | EOF
    ;

commentText
    : (CHARACTER|DELIMITER|COMMENT)*
    ;

commentStartAndText
    : COMMENT commentText
    ;

comment
    : LEADING_SPACING? commentStartAndText eol
    ;
