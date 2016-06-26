grammar QueryPath;

main: (queryPath (SEMI queryPath)* SEMI?)? EOF;

queryPath: fetchPath | simpleOrderPath;

fetchPath
    :
    // The alternative2 must be before alternative3, but I don't know why
    THIS (fetchNodes += fetchNode[true])*
    |
    fetchNodes += fetchNode[false] (fetchNodes += fetchNode[true])*
    |
    (fetchNodes += fetchNode[true])+
    ;

fetchNode[boolean requiresGetterType]
    :
    (
        getterType = (DOT | REQUIRED_DOT)
        |
        { !$ctx.requiresGetterType }?
    )
    (
        name = IDENTIFIER
        |
        collectionFetchType = (ALL | PARTIAL)
        LPRNT
        name = IDENTIFIER
        RPRNT
    )
    ;

simpleOrderPath
    :
    (orderOpportunity = (PRE | POST))?
    ORDER BY
    singleOrderPath (COMMA singleOrderPath)*
    ;

singleOrderPath
    :
    (
        // The alternative2 must be before alternative3, but I don't know why
        THIS (orderNodes += orderNode[true])*
        |
        orderNodes += orderNode[false] (orderNodes += orderNode[true])*
        |
        (orderNodes += orderNode[true])+
    )
    (sortMode = (ASC | DESC))?
    ;

orderNode[boolean requiresGetterType]
    :
    (
        getterType = (DOT | REQUIRED_DOT)
        |
        { !$ctx.requiresGetterType }?
    )
    name = IDENTIFIER
    ;

DOT: '.';

REQUIRED_DOT: '..';

ALL: 'all';

PARTIAL: 'partial';

PRE: 'pre';

POST: 'post';

ASC: 'asc';

DESC: 'desc';

THIS: 'this';

ORDER: 'order';

BY: 'by';

COMMA: ',';

SEMI: ';';

LPRNT: '(';

RPRNT: ')';

IDENTIFIER: [$A-Za-z][$A-Za-z0-9]*;

WS: (' ' | '\r' | '\n' | '\t') ->skip;

COMMENT: 
    (
    '/*' .*? '*/' 
    |
    '//' .*? '\r'? ('\n' | EOF)
    )
    ->skip;
