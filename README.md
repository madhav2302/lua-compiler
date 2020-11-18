## Lua Interpreter

It is an assignment of course Programming Language Principle (COP5556) taught in University of Florida. 


### About the project

It is simple Lua interpreter built on Java, which performs basic arithmetic operations and function calls in Lua.
Grammar for the assignment is [here](resources/grammar.txt).

The project is divided into 4 parts -
1. [Scanner](src/cop5556fa19/Scanner.java)      - It scans the input and generates the token according to Lua language.
1. [Parser](src/cop5556fa19/Parser.java) (1)   - We create Abstract Syntax Tree for basic arithmetic operations. 
1. [Parser](src/cop5556fa19/Parser.java) (2)   - We create Abstract Syntax Tree for remaining things like function call, chunk, blocks, etc.
1. [Interpreter](src/interpreter/ASTVisitorAdapter.java)  - We use the AST and interpret the operations the basic operations parsed by parser.

Assignment documents can be found [here](resources).

JUnit Tests - 
1. [Scanner Tests](src/cop5556fa19/ScannerTest.java)
1. [Parser Tests](src/cop5556fa19/ParserTest.java)
1. [Interpreter Tests](src/cop5556fa19/TestInterpreter.java)


External Libraries needed -
1. junit-jupiter-api-5.5.x.jar 
1. apiguardian-api-1.x.x.jar
1. junit-platform-console-standalone-1.5.x.jar

The above libraries can be downloaded from [here](https://drive.google.com/file/d/1lXeAvx_tw3lvYJbw743E7dkN-49dqowv/view).
