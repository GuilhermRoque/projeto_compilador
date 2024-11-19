package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class visitador extends GrammarBaseVisitor<String> {

    // Gerenciamento de escopo
    private Stack<Map<String, Boolean>> escopos = new Stack<>();
    private StringBuilder pcode = new StringBuilder();
    private int labelCounter = 0; // Para gerar rótulos únicos

    public visitador() {
        // Inicia o escopo global
        escopos.push(new HashMap<>());
    }

    private void enterScope() {
        escopos.push(new HashMap<>());
    }

    private void exitScope() {
        escopos.pop();
    }

    private boolean declareVariable(String name) {
        Map<String, Boolean> currentScope = escopos.peek();
        if (currentScope.containsKey(name)) {
            System.out.println("Erro: Variável '" + name + "' já declarada no escopo atual.");
            return false;
        }
        currentScope.put(name, true);
        return true;
    }

    private boolean isVariableDeclared(String name) {
        for (int i = escopos.size() - 1; i >= 0; i--) {
            if (escopos.get(i).containsKey(name)) {
                return true;
            }
        }
        System.out.println("Erro: Variável '" + name + "' não foi declarada.");
        return false;
    }

    private String generateLabel(String base) {
        return base + "_" + (labelCounter++);
    }

    @Override
    public String visitDeclaracaoVar(GrammarParser.DeclaracaoVarContext ctx) {
        String varName = ctx.IDENT().getText();
        if (declareVariable(varName)) {
            pcode.append("lda #").append(varName).append("\n");
            if (ctx.expressao() != null) {
                visit(ctx.expressao());
                pcode.append("sto\n");
            }
        }
        return null;
    }

    @Override
    public String visitAtribuicao(GrammarParser.AtribuicaoContext ctx) {
        String varName = ctx.IDENT().getText();
        if (isVariableDeclared(varName)) {
            pcode.append("lda #").append(varName).append("\n");
            visit(ctx.expressao());
            pcode.append("sto\n");
        }
        return null;
    }

    @Override
    public String visitIfStatement(GrammarParser.IfStatementContext ctx) {
        String labelElse = generateLabel("ELSE");
        String labelEnd = generateLabel("ENDIF");

        visit(ctx.condicao());
        pcode.append("fjp ").append(labelElse).append("\n");

        enterScope();
        visit(ctx.bloco(0)); // Bloco "then"
        exitScope();

        if (ctx.bloco().size() > 1) { // Verifica se há um bloco "else"
            pcode.append("ujp ").append(labelEnd).append("\n");
            pcode.append(labelElse).append(":\n");
            enterScope();
            visit(ctx.bloco(1)); // Bloco "else"
            exitScope();
        } else {
            pcode.append(labelElse).append(":\n");
        }

        pcode.append(labelEnd).append(":\n");
        return null;
    }

    @Override
    public String visitWhileStatement(GrammarParser.WhileStatementContext ctx) {
        String labelStart = generateLabel("WHILE");
        String labelEnd = generateLabel("ENDWHILE");

        pcode.append(labelStart).append(":\n");
        visit(ctx.condicao());
        pcode.append("fjp ").append(labelEnd).append("\n");

        enterScope();
        visit(ctx.bloco());
        exitScope();

        pcode.append("ujp ").append(labelStart).append("\n");
        pcode.append(labelEnd).append(":\n");
        return null;
    }

    @Override
    public String visitComandoEntradaSaida(GrammarParser.ComandoEntradaSaidaContext ctx) {
        if (ctx.PRINT() != null) {
            if (ctx.STRING() != null) {
                String text = ctx.STRING().getText();
                pcode.append("ldc ").append(text).append("\n");
                pcode.append("wri\n");
            } else {
                visit(ctx.expressao());
                pcode.append("wri\n");
            }
        } else if (ctx.INPUT() != null) {
            String varName = ctx.IDENT().getText();
            if (isVariableDeclared(varName)) {
                pcode.append("lda #").append(varName).append("\n");
                pcode.append("rd\n");
                pcode.append("sto\n");
            }
        }
        return null;
    }

    @Override
    public String visitExpressao(GrammarParser.ExpressaoContext ctx) {
        visit(ctx.termo(0));
        for (int i = 1; i < ctx.termo().size(); i++) {
            visit(ctx.termo(i));
            String op = ctx.getChild(2 * i - 1).getText();
            switch (op) {
                case "+":
                    pcode.append("add\n");
                    break;
                case "-":
                    pcode.append("sub\n");
                    break;
            }
        }
        return null;
    }

    @Override
    public String visitTermo(GrammarParser.TermoContext ctx) {
        visit(ctx.fator(0));
        for (int i = 1; i < ctx.fator().size(); i++) {
            visit(ctx.fator(i));
            String op = ctx.getChild(2 * i - 1).getText();
            switch (op) {
                case "*":
                    pcode.append("mul\n");
                    break;
                case "/":
                    pcode.append("div\n");
                    break;
            }
        }
        return null;
    }
    @Override
    public String visitFator(GrammarParser.FatorContext ctx) {
        String text = ctx.getText(); // Obtém o texto do nó atual

        if (text.matches("\\d+")) { // Para números
            pcode.append("ldc ").append(text).append("\n");
        } else if ("true".equals(text)) { // Para booleano true
            pcode.append("ldc true\n");
        } else if ("false".equals(text)) { // Para booleano false
            pcode.append("ldc false\n");
        } else if (text.matches("[a-zA-Z_][a-zA-Z0-9_]*")) { // Para identificadores (variáveis)
            String varName = text;
            if (isVariableDeclared(varName)) {
                pcode.append("lod #").append(varName).append("\n");
            }
        } else if (ctx.getChildCount() == 3 && "(".equals(ctx.getChild(0).getText())) {
            // Para expressões entre parênteses, verifica se a estrutura é (expressao)
            visit(ctx.getChild(1)); // Visita o nó do meio, que é a expressão
        } else if (ctx.base() != null) { // Para base (negações ou outras expressões recursivas)
            visit(ctx.base());
        }
        return null;
    }




    @Override
    public String visitCondicao(GrammarParser.CondicaoContext ctx) {
        visit(ctx.expressao(0));
        visit(ctx.expressao(1));
        String op = ctx.operadorRelacional().getText();
        switch (op) {
            case "<":
                pcode.append("let\n");
                break;
            case ">":
                pcode.append("grt\n");
                break;
            case "==":
                pcode.append("equ\n");
                break;
        }
        return null;
    }

    public String getPCode() {
        return pcode.toString();
    }
}
