RDPGenerator
=
Generates a recursive descent parser

After seeing a lot of the Programming Languages class people writing their Wren Recursive Descent Parser, and having to write this program for my compilers class, I figure I'd post the source so maybe someone could benefit from its generated code.

I may add a watermark to the generated code so Dr. Fenwick can weed out the people using this program to cheat on their Programming Languages lab ;)

Usage
=
If you wish to use the RDP generator, simply compile it and run using `java RDPGenerator grammar.g LexerName`. An ETF grammar is provided as an example. No lexer is provided, but you can write your own, provided that it returns tokens of the same name as the ones specified in the grammar - as well as implementing from the Lexer interface provided.

Provided there are no errors, the RDP generator will dump all of the generated files into a `gen/` folder where you can compile them using `javac *.java`, and then run programs from there.

License
=
I currently retain all rights to this program. You may compile it and use it as you please, but you may not edit nor redistribute the code until further notice.

