package org.example;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;

import javax.naming.InitialContext;
import java.io.IOException;

public class Main {
    public static void main(String[] args)throws IOException {
        CharStream charStream = CharStreams.fromFileName("teste.Grammar");
        GrammarLexer lexer = new GrammarLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        GrammarParser parser = new GrammarParser(tokens);
        GrammarParser.ProgramaContext tree = parser.programa();
        visitador visitador = new visitador();
        visitador.visit(tree);

        System.out.println(visitador.getPCode());



    }
}