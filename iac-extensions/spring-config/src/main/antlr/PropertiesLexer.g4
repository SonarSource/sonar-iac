lexer grammar PropertiesLexer;

COMMENT   : [!#] -> pushMode(VALUE_MODE);
NEWLINE   : [\r\n\u2028\u2029]+;
DELIMITER : [ ]* [:=\t\f ] [ ]* -> pushMode(VALUE_MODE);
SLASH     : '\\' -> more, pushMode(INSIDE);
CHARACTER : ~ [!#:=\r\n\u2028\u2029];

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
