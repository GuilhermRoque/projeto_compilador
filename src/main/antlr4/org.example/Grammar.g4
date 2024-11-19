grammar Grammar;

// Regras principais
programa       : instrucao+ EOF ;

instrucao      : declaracaoVar ';'
               | atribuicao ';'
               | comandoControle
               | comandoEntradaSaida ';' ;

declaracaoVar  : VAR IDENT ('=' expressao)? ;

atribuicao     : IDENT ASSIGN expressao ;

comandoControle : ifStatement
                | whileStatement ;

ifStatement    : IF '(' condicao ')' bloco ('else' bloco)? ;

whileStatement : WHILE '(' condicao ')' bloco ;

bloco          : '{' instrucao* '}' ;

// Comandos de entrada e saída
comandoEntradaSaida : PRINT '(' (STRING | expressao) ')'
                    | INPUT '(' IDENT ')' ;

// Regras para expressões
expressao      : termo (('+' | '-') termo)* ;
termo          : fator (('*' | '/') fator)* ;
fator          : base ('^' fator)? ;
base           : '-' base
               | NUMBER
               | TRUE
               | FALSE
               | IDENT
               | '(' expressao ')' ;

// Regras para condições
condicao       : expressao operadorRelacional expressao
               | '(' condicao ')'
               | condicao operadorLogico condicao ;

operadorRelacional : LESST | GREATERT | EQ ;
operadorLogico      : AND | OR ;

// Tokens
VAR             : 'var';
ASSIGN          : '=';
PRINT           : 'print';
INPUT           : 'input';
IF              : 'if';
WHILE           : 'while';
AND             : 'and';
OR              : 'or';
LESST           : '<';
GREATERT        : '>';
EQ              : '==';
NUMBER          : [0-9]+;
TRUE            : 'true';
FALSE           : 'false';
IDENT           : [a-zA-Z_][a-zA-Z0-9_]*;
STRING          : '"' (~["])* '"'; // String entre aspas
WS              : [ \t\r\n]+ -> skip; // Ignorar espaços em branco
