import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

/**
 * Created by alek on 9/23/15.
 */
public class RDPGenerator {
    private HashMap<String, Terminal> terminals = new HashMap<>();
    private HashMap<String, NonTerminal> nonTerminals = new HashMap<>();
    private String outDir;
    private String lexerName;

    public RDPGenerator(String outDir, String lexerName, HashMap<String, Terminal> terminals, HashMap<String, NonTerminal> nonTerminals) {
        this.terminals = terminals;
        this.nonTerminals = nonTerminals;
        this.outDir = outDir;
        this.lexerName = lexerName;
    }

    public void generateCode() throws IOException {
        // Create a directory
        File outDirFile = new File(outDir);
        if(outDirFile.exists() && !outDirFile.isDirectory()) {
            throw new IOException("Output directory (" + outDir + ") already exists as a file and not a directory");
        } else if(outDirFile.exists()) {
            System.out.println("WARNING: output directory already exists - files may get overwritten");
        } else {
            if(!outDirFile.mkdir()) {
                throw new IOException("Could not create output directory (" + outDir + ")");
            }
        }

        // Generate Lexer.java (interface)
        try {
            generateLexer();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to generate the Lexer code.");
        }

        // Generate Tokens.java (interface)
        try {
            generateTokens();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to generate the Tokens code");
        }

        // Generate Parser.java (concrete class)
        try {
            generateParser();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to generate the Parser code");
        }

        // Generate Driver.java (concrete class)
        try {
            generateDriver();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to generate the Driver code");
        }
    }

    private void generateLexer() throws IOException {
        // This should be easy
        File lexerFile = new File(outDir, "Lexer.java");
        if(lexerFile.exists()) {
            System.out.println("WARNING: File Lexer.java exists, deleting it");
            lexerFile.delete();
        }
        FileWriter writer = new FileWriter(lexerFile);
        writer.write("public interface Lexer {\n" +
                "   public int getToken();\n" +
                "}");
        writer.close();
    }

    private void generateTokens() throws IOException {
        File tokensFile = new File(outDir, "Tokens.java");
        if(tokensFile.exists()) {
            System.out.println("WARNING: File Tokens.java exists, deleting it");
            tokensFile.delete();
        }
        FileWriter writer = new FileWriter(tokensFile);
        writer.write("public interface Tokens {\n");
        // fill in tokens here
        int tokenStart = 100;
        for(String token : terminals.keySet()) {
            writer.write("    public static final int " + token + " = " + tokenStart + ";\n");
            tokenStart++;
        }
        writer.write("}");
        writer.close();
    }

    private void generateParser() throws IOException {
        File parserFile = new File(outDir, "Parser.java");
        if(parserFile.exists()) {
            System.out.println("WARNING: File Parser.java exists, deleting it");
            parserFile.delete();
        }
        FileWriter writer = new FileWriter(parserFile);
        writer.write("public class Parser implements Tokens {\n");
        // fields
        writer.write("    private Lexer lexer;\n");
        writer.write("    private int currTok;\n");
        // put some basic functions in there
        writer.write("    public Parser(Lexer lexer) {\n");
        writer.write("        this.lexer = lexer;\n");
        writer.write("        this.currTok = -1;\n");
        writer.write("    }\n");
        writer.write("    public void parse() {\n");
        writer.write("        this.currTok = lexer.getToken();\n");

        // this is where we call the starting nonterminal
        NonTerminal start = getStartingNonTerminal();
        assert start != null;
        writer.write("        " + start.getName() + "();\n");
        writer.write("    }\n");

        // now for the fun stuff... generating the code for all of the nonterminals
        for(String nonTerminalName : nonTerminals.keySet()) {
            NonTerminal nonTerminal = nonTerminals.get(nonTerminalName);
            writer.write("    private void " + nonTerminalName + "() {\n");

            // do the first one, where we won't be dealing with else if's
            Production prod = nonTerminal.getProductions().get(0);
            GrammarSymbol sym = prod.getGrammarSymbols().get(0);
            if(sym instanceof NonTerminal) {
                NonTerminal nt = (NonTerminal)sym;
                // create the if statement for all of the first set
                writer.write("        if(currTok == " + nt.getFirstSet()[0].getName());
                for(int i = 1; i < nt.getFirstSet().length; i++)
                    writer.write(" || currTok == " + nt.getFirstSet()[i].getName());
                writer.write(") {\n");
                // call the nonterminal that created this first set
                writer.write("            " + sym.getName() + "();\n");
            } else if(sym instanceof Terminal) {
                Terminal term = (Terminal)sym;
                // get the if statement - super easy
                writer.write("        if(currTok == " + term.getName() + ") {\n");
                // do the match for the terminal
                writer.write("            match(" + term.getName() + ");\n");
            }

            // then complete this production with the matches/production calls
            for(int i = 1; i < prod.getGrammarSymbols().size(); i++) {
                sym = prod.getGrammarSymbols().get(i);
                if(sym instanceof NonTerminal) {
                    writer.write("            " + sym.getName() + "();\n");
                } else if(sym instanceof Terminal) {
                    writer.write("            match(" + sym.getName() + ");\n");
                } else {
                    assert false;
                }
            }
            writer.write("        }\n"); // if statement

            // do the rest of the productions - exactly the same way
            for(int prodNum = 1; prodNum < nonTerminal.getProductions().size(); prodNum++) {
                prod = nonTerminal.getProductions().get(prodNum);
                sym = prod.getGrammarSymbols().get(0);
                if(sym instanceof NonTerminal) {
                    NonTerminal nt = (NonTerminal)sym;
                    // create the if statement for all of the first set
                    writer.write("        else if(currTok == " + nt.getFirstSet()[0].getName());
                    for(int i = 1; i < nt.getFirstSet().length; i++)
                        writer.write("|| currTok == " + nt.getFirstSet()[i].getName());
                    writer.write(") {\n");
                    // call the nonterminal that created this first set
                    writer.write("            " + sym.getName() + "();\n");
                } else if(sym instanceof Terminal) {
                    Terminal term = (Terminal)sym;
                    // get the if statement - super easy
                    writer.write("        else if(currTok == " + term.getName() + ") {\n");
                    // do the match for the terminal
                    writer.write("            match(" + term.getName() + ");\n");
                }

                // then complete this production with the matches/production calls
                for(int i = 1; i < prod.getGrammarSymbols().size(); i++) {
                    sym = prod.getGrammarSymbols().get(i);
                    if(sym instanceof NonTerminal) {
                        writer.write("            " + sym.getName() + "();\n");
                    } else if(sym instanceof Terminal) {
                        writer.write("            match(" + sym.getName() + ");\n");
                    } else {
                        assert false;
                    }
                }
                writer.write("        }\n"); // else if statement
            }

            writer.write("        else ");
            // is this nonterminal nullable?
            if(nonTerminal.isNullable()) {
                // if so, make sure we've got something in the follow set actually following us
                Terminal follow = nonTerminal.getFollowSet()[0];
                writer.write("if(");
                writer.write("currTok == " + follow.getName());
                for(int i = 1; i < nonTerminal.getFollowSet().length; i++) {
                    follow = nonTerminal.getFollowSet()[i];
                    writer.write(" || currTok == " + follow.getName());
                }
                writer.write(") {\n");
                writer.write("            return;\n");
                writer.write("        }\n"); // else statement
                writer.write("        else ");
            }

            writer.write("error();\n");
            writer.write("    }\n");
        }

        writer.write("    private void match(int tok) {\n");
        writer.write("        if(tok != currTok) error();\n");
        writer.write("        else currTok = lexer.getToken();\n");
        writer.write("    }\n");

        writer.write("    private void error() {\n");
        writer.write("        throw new RuntimeException(\"Encountered an error while parsing (currTok = \" + currTok + \")\");\n");
        writer.write("    }\n");

        writer.write("}");

        writer.close();
    }

    private void generateDriver() throws IOException {
        File driverFile = new File(outDir, "Driver.java");
        if(driverFile.exists()) {
            System.out.println("WARNING: File Driver.java exists, deleting it");
            driverFile.delete();
        }
        FileWriter writer = new FileWriter(driverFile);
        writer.write("import java.io.FileReader;\n");
        writer.write("import java.io.IOException;\n");
        writer.write("public class Driver {\n");
        writer.write("    public void parse(String str) {\n");
        writer.write("        " + lexerName + " lexer = new " + lexerName + "(str);\n");
        writer.write("        Parser parser = new Parser(lexer);\n");
        writer.write("        try {\n");
        writer.write("            parser.parse();\n");
        writer.write("            System.out.println(\"OK\");\n");
        writer.write("        } catch(RuntimeException ex) {\n");
        writer.write("            System.out.println(\"invalid parse string\");\n");
        writer.write("        }\n");
        writer.write("    }\n");

        writer.write("    public static void main(String[] args) throws IOException {\n");
        writer.write("        if(args.length == 0) {\n");
        writer.write("            System.out.println(\"usage: ParserDriver filename\");\n");
        writer.write("            System.exit(1);\n");
        writer.write("        }\n");
        writer.write("        FileReader reader = new FileReader(args[0]);\n");
        writer.write("        Driver driver = new Driver();\n");
        writer.write("        String parseStr = \"\";\n");
        writer.write("        int c = -1;\n");
        writer.write("        while((c = reader.read()) != -1) parseStr += (char)c;\n");
        writer.write("        driver.parse(parseStr);\n");
        writer.write("    }\n");
        writer.write("}");

        writer.close();
    }

    private NonTerminal getStartingNonTerminal() {
        for(String name : nonTerminals.keySet()) {
            NonTerminal nt = nonTerminals.get(name);
            if(nt.isStartToken())
                return nt;
        }
        return null;
    }

    public String getOutDir() {
        return outDir;
    }
}
