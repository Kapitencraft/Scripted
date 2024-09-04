package net.kapitencraft.scripted.edit.text.language.java;

import net.kapitencraft.scripted.code.var.type.abstracts.RegistryType;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.edit.Token;
import net.kapitencraft.scripted.edit.text.language.Tokenizer;

import java.util.ArrayList;
import java.util.List;

public class JavaTokenizer extends Tokenizer {
    private static final List<String> MODIFIERS = List.of("class", "final"); //add more modifiers like static in future (TODO)

    public List<Token> tokenize(String content) {
        List<Token> tokens = new ArrayList<>();
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            switch (c) {
                case '"': {
                    int start = i;
                    do {//read string
                        i++;
                        c = content.charAt(i);
                    } while (c != '"');
                    String content1 = content.substring(start + 1, i - 1);
                    tokens.add(new Token(content1, Token.Type.PRIM_STRING));
                    break;
                }
                case '\n': tokens.add(new Token("\n", Token.Type.NEW_LINE)); break;
                case '\t': //tabs and space ignored
                case ' ': {
                    continue;
                }
                case '\'': {
                    char c1 = content.charAt(i + 1);
                    tokens.add(new Token(String.valueOf(c1), Token.Type.PRIM_CHAR));
                    i+=2;
                    break;
                }
                //separators
                case '.': {
                    if (Character.isDigit(content.charAt(i+1))) {
                        tokens.add(readNum(i, content));
                    } else {
                        tokens.add(new Token(".", Token.Type.SEPARATOR));
                    }
                    break;
                }
                case '?': tokens.add(new Token("?", Token.Type.WHEN_CONDITION_SEPARATOR)); break;
                case ',': tokens.add(new Token(",", Token.Type.NEXT_PARAM)); break;
                case '(': tokens.add(new Token("(", Token.Type.BRACKET_OPEN)); break;
                case ')': tokens.add(new Token(")", Token.Type.BRACKET_CLOSE)); break;
                case '{': tokens.add(new Token("{", Token.Type.CURLY_BRACKET_OPEN)); break;
                case '}': tokens.add(new Token("}", Token.Type.CURLY_BRACKET_CLOSE)); break;
                case ';': tokens.add(new Token(";", Token.Type.EXPR_END)); break;
                //bool operation
                case '&': tokens.add(new Token("&", Token.Type.AND)); break;
                case '^': tokens.add(new Token("^", Token.Type.XOR)); break;
                case '|': tokens.add(new Token("|", Token.Type.OR)); break;
                case '!': tokens.add(new Token("!", Token.Type.NOT)); break;
                //math operation
                case '+': {
                    if (last(tokens).type == Token.Type.ADD) {
                        removeLast(tokens);
                        tokens.add(new Token("++", Token.Type.ASSIGN_WITH_OPERATION));
                    } else {
                        tokens.add(new Token("+", Token.Type.ADD));
                    }
                    break;
                }
                case '-': {
                    if (last(tokens).type == Token.Type.SUB) {
                        removeLast(tokens);
                        tokens.add(new Token("--", Token.Type.ASSIGN_WITH_OPERATION));
                    } else {
                        tokens.add(new Token("-", Token.Type.SUB));
                    }
                    break;
                }
                case '/': tokens.add(new Token("/", Token.Type.DIV)); break;
                case '*': tokens.add(new Token("*", Token.Type.MULT)); break;
                case '%': tokens.add(new Token("%", Token.Type.MOD)); break;

                //compare
                case '<': tokens.add(new Token("<", Token.Type.LESSER)); break;
                case '>': tokens.add(new Token(">", Token.Type.GREATER)); break;
                case '=': {
                    Token.Type lastType = last(tokens).type;
                    if (isMathOperation(lastType)) { //mathematical operation methods
                        removeLast(tokens);
                        tokens.add(new Token(last(tokens).value + '=', Token.Type.ASSIGN_WITH_OPERATION));
                        break;
                    }
                    if (lastType == Token.Type.LESSER || lastType == Token.Type.GREATER || lastType == Token.Type.NOT) { //comparators
                        removeLast(tokens);
                        Token.Type newType = switch (lastType) {
                            case LESSER -> Token.Type.LEQUAL;
                            case GREATER -> Token.Type.GEQUAL;
                            case NOT -> Token.Type.NEQUAL;
                            default -> throw new IllegalStateException("Unexpected (and illegal) value: " + lastType);
                        };
                        tokens.add(new Token(last(tokens).value + "=", newType));
                        break;
                    }
                    if (lastType == Token.Type.ASSIGN) {
                        removeLast(tokens);
                        tokens.add(new Token("==", Token.Type.EQUAL));
                        i++;
                    } else {
                        tokens.add(new Token("=", Token.Type.ASSIGN));
                    }
                    break;
                }
                default: {
                    if (Character.isDigit(c)) {
                        tokens.add(readNum(i, content));
                    } else if (Character.isLetter(c) || c == '#') { //if # it's registry tag
                        int start = i;
                        do {
                            i++;
                            c = content.charAt(i);
                        } while (Character.isLetterOrDigit(c) || c == ':');
                        String subS = content.substring(start, i);
                        i = tokenizeString(subS, i, tokens, content);
                    } else {
                        continue;
                    }
                    i--; //req
                }
            }
        }
        tokens.add(new Token("", Token.Type.EOF));
        return tokens;
    }

    private int tokenizeString(String toTokenize, int i, List<Token> tokens, String content) {
        switch (toTokenize) {
            case "if" -> tokens.add(new Token("if", Token.Type.IF_IDENTIFIER));
            case "else" -> tokens.add(new Token("else", Token.Type.ELSE_IDENTIFIER));
            case "do" -> tokens.add(new Token("do", Token.Type.DO_IDENTIFIER));
            case "while" -> tokens.add(new Token("while", Token.Type.WHILE_IDENTIFIER));
            case "for" -> tokens.add(new Token("for", Token.Type.FOR_IDENTIFIER));
            case "return" -> tokens.add(new Token("return", Token.Type.RETURN_IDENTIFIER));
            case "break" -> tokens.add(new Token("break", Token.Type.BREAK_IDENTIFIER));
            case "continue" -> tokens.add(new Token("continue", Token.Type.CONTINUE_IDENTIFIER));
            default -> {
                if (toTokenize.contains(":")) { //is registry-element or -list (or when mod)
                    String regKey = toTokenize.substring(0, toTokenize.indexOf(":"));
                    if (RegistryType.TYPES_FOR_NAME.containsKey(regKey)) tokens.add(new Token(toTokenize, Token.Type.PRIM_REG_ELEMENT));
                    else {
                        tokens.add(new Token(regKey, Token.Type.VAR_NAME));
                        tokens.add(new Token(":", Token.Type.WHEN_FALSE_SEPARATOR));
                        return tokenizeString(toTokenize.substring(toTokenize.indexOf(":")), i, tokens, content);
                    }
                } else { //decompile var types
                    char c = content.charAt(i);
                    if (MODIFIERS.contains(toTokenize)) tokens.add(new Token(toTokenize, Token.Type.MODIFIER));
                    else if ("List".equals(toTokenize) || "Map".equals(toTokenize) || "Multimap".equals(toTokenize)) {
                        StringBuilder builder = new StringBuilder(toTokenize);
                        int bracketCount = 0;
                        do {
                            i++;
                            c = content.charAt(i);
                            builder.append(c);
                            if (c == '<') bracketCount++;
                        } while (c != '>' && bracketCount > 0);
                        tokens.add(new Token(builder.toString(), Token.Type.VAR_TYPE));
                    } else if (VarType.NAME_MAP.containsKey(toTokenize)) tokens.add(new Token(toTokenize, Token.Type.VAR_TYPE));
                    else if (c == '(' || c == '.' || c == ',' || c == ')') tokens.add(new Token(toTokenize, Token.Type.METHOD_NAME));
                    else tokens.add(new Token(toTokenize, Token.Type.VAR_NAME));
                }
            }
        }
        return i;
    }

    private static boolean isMathOperation(Token.Type type) {
        return switch (type) {
            case MULT:
            case DIV:
            case ADD:
            case SUB:
            case MOD: yield true;
            default: yield false;
        };
    }

    private static boolean isBoolOperation(Token.Type type) {
        return switch (type) {
            case OR:
            case XOR:
            case AND: yield true;
            default: yield false;
        };
    }

    private static boolean isOperation(Token.Type type) {
        return isMathOperation(type) || isBoolOperation(type);
    }

}
