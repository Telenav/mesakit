
grammar GraphQuery;

// query -> OPEN_PARENTHESIS [query] CLOSE_PARENTHESIS    (identifier = 7)
// query -> string-attribute CONTAINS constant-value      roadName contains 'central'
// query -> attribute COMPARATOR constant-value           length < 10 meters
// query -> ! [query]                                     !isOnRamp then roadType = 'freeway'
// query -> [query] (THEN [query])*                       roadType = 'freeway' then isOffRamp then roadType = 'freeway'
// query -> [query] AND [query]                           identifier = 7 and roadName contains 'lomas'
// query -> [query] OR [query]                            roadType = 'freeway' or speedLimit > 60 mph
// query -> boolean-attribute                             isOffRamp

select : SELECT SP queryExpression=query EOF;

query
    : OPEN_PARENTHESIS parenthesizedQuery=query CLOSE_PARENTHESIS
    | notOperator=NOT notQuery=query
    | booleanAttribute=attribute
    | comparisonAttribute=attribute SP* comparisonOperator=(EQUAL | NOT_EQUAL | GREATER_THAN | LESS_THAN | GREATER_THAN_OR_EQUAL | LESS_THAN_OR_EQUAL) SP* comparisonValue=constantValue
    | containsAttribute=attribute SP containsOperator=CONTAINS SP containsValue=constantValue
    | closureQuery=query closure=ONE_OR_MORE
    | logicalLeftQuery=query SP logicalOperator=(AND | OR) SP logicalRightQuery=query
    | thenLeftQuery=query (SP thenOperator=THEN SP thenRightQuery=query)+
    ;

attribute
    : ATTRIBUTE_NAME
    ;

constantValue
    : BOOLEAN
    | STRING
    | DOUBLE SP unit=('inch' | 'inches' | 'feet' | 'meter' | 'meters' | 'kilometer' | 'kilometers' | 'mile' | 'miles' | 'kph' | 'mph' | 'msec')
    | INT SP unit=('inch' | 'inches' | 'feet' | 'meter' | 'meters' | 'kilometer' | 'kilometers' | 'mile' | 'miles' | 'kph' | 'mph' | 'msec')
    | DOUBLE
    | INT
    ;

SELECT
    : 'select'
    ;

NOT
    : '!'
    | 'not '
    ;

ONE_OR_MORE
    : '+'
    ;

OPEN_PARENTHESIS
    : '('
    ;

CLOSE_PARENTHESIS
    : ')'
    ;

CONTAINS
    : 'contains'
    ;

THEN
    : 'then'
    ;

BOOLEAN
    : 'true'
    | 'false'
    ;

AND : 'and'
    | '&&'
    ;

OR  : 'or'
    | '||'
    ;

EQUAL  : '=='
    | '='
    ;

NOT_EQUAL  : '!='  ;
GREATER_THAN  : '>'   ;
LESS_THAN  : '<'   ;
GREATER_THAN_OR_EQUAL  : '>='  ;
LESS_THAN_OR_EQUAL  : '<='  ;

ATTRIBUTE_NAME
    : ALPHA ATTRIBUTE_NAME_CHAR*
    ;

fragment ATTRIBUTE_NAME_CHAR
    : '-' | '_' | ':' | DIGIT | ALPHA
    ;

fragment DIGIT
    : ('0'..'9')
    ;

fragment ALPHA
    : ( 'A'..'Z' | 'a'..'z' )
    ;

STRING
    : '"' (ESC | ~ ["\\])*? '"'
    | '\'' (ESC | ~ ["\\])*? '\''
    ;

fragment VERTEX
    : 'vertex'
    ;

fragment ESC
    : '\\' (["\\/bfnrt] | UNICODE)
    ;

fragment UNICODE
    : 'u' HEX HEX HEX HEX
    ;

fragment HEX
    : [0-9a-fA-F]
    ;

DOUBLE
    : '-'? INT '.' [0-9]+
    ;

INT
    : '0' | [1-9] [0-9]*
    ;

SP
    : ' '
    ;

