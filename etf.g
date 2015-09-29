ID
NUM
PLUS
TIMES
LPAR
RPAR
EOF
%%
E
EP
T
TP
F
%%
E :: T EP
EP :: PLUS T EP
T :: F TP
TP :: TIMES F TP
F :: LPAR E RPAR
F :: ID
F :: NUM
%%
E := no : LPAR ID NUM : EOF RPAR
EP := yes : PLUS : EOF RPAR
T := no : LPAR ID NUM : PLUS EOF RPAR
TP := yes : TIMES : PLUS EOF RPAR
F := no : LPAR ID NUM : TIMES PLUS EOF RPAR
%%
