import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class RDPParser {

	private RDPLexer lexer;
	private HashMap<String, Terminal> terminals = new HashMap<>();
	private HashMap<String, NonTerminal> nonTerminals = new HashMap<>();
	
	public RDPParser(RDPLexer lexer) {
		this.lexer = lexer;
	}
	
	public void readTerminals() {
		RDPToken token = lexer.nextToken();
		while(token.tokenType != RDPTokenType.DOUBLEPERCENT) {
			// not an identifier, which is all we're reading right now
			if(token.tokenType == RDPTokenType.LINE_END) {
				token = lexer.nextToken();
				continue; // ignore line ends
			} else if(token.tokenType != RDPTokenType.IDENTIFIER) {
				error(RDPTokenType.IDENTIFIER, token.tokenType);
			}
			// add this to the list of available terminals
			terminals.put(token.contents, new Terminal(token.contents));

			token = lexer.nextToken();
		}
	}
	
	public void readNonTerminals() {
		RDPToken token = lexer.nextToken();
		boolean foundFirst = false;
		while(token.tokenType != RDPTokenType.DOUBLEPERCENT) {
			// not an identifier, which is all we're reading right now
			if(token.tokenType == RDPTokenType.LINE_END) {
				token = lexer.nextToken();
				continue; // ignore line ends
			} else if(token.tokenType != RDPTokenType.IDENTIFIER) {
				error(RDPTokenType.IDENTIFIER, token.tokenType);
			}
			if(!foundFirst) {
				// add this to the list of available nonterminals, and signify that it's the starting nonterminal
				nonTerminals.put(token.contents, new NonTerminal(token.contents, true));
				foundFirst = true;
			} else {
				// add this to the list of available nonterminals
				nonTerminals.put(token.contents, new NonTerminal(token.contents));
			}

			token = lexer.nextToken();
		}
	}
	
	public void readProductions() {
		RDPToken token = lexer.nextToken();
		while(token.tokenType != RDPTokenType.DOUBLEPERCENT) {
			if(token.tokenType == RDPTokenType.LINE_END) {
				token = lexer.nextToken();
				continue; // ignore line ends
			// not an identifier, which is all we're reading right now
			} else if(token.tokenType != RDPTokenType.IDENTIFIER) {
				error(RDPTokenType.IDENTIFIER, token.tokenType);
			}
			
			// the nonterminal that we're dealing with
			NonTerminal nt = nonTerminals.get(token.contents);
			if(nt == null) {
				error("Unregistered nonterminal: " + token.contents);
			}
			token = lexer.nextToken();
			
			// confirm the double colon following
			// TODO: create a match method for stuff like this
			if(token.tokenType != RDPTokenType.DOUBLECOLON) {
				error(RDPTokenType.DOUBLECOLON, token.tokenType);
			}
			token = lexer.nextToken();
			
			// create a new production to add
			Production prod = new Production();
			while(token.tokenType != RDPTokenType.LINE_END) {
				// Failed to get a token that was an identifier
				if(token.tokenType != RDPTokenType.IDENTIFIER) {
					error(RDPTokenType.IDENTIFIER, token.tokenType);
				}
				
				GrammarSymbol sym = null;
				// figure out if we're dealing with a terminal or nonterminal
				if(terminals.containsKey(token.contents)) {
					sym = terminals.get(token.contents);
				} else if(nonTerminals.containsKey(token.contents)) {
					sym = nonTerminals.get(token.contents);
				} else {
					error("Attempted to use nonregistered grammar symbol for production: " + token.contents);
				}
				// Add it to the list
				prod.addGrammarSymbol(sym);
				token = lexer.nextToken();
			}
			nt.addProduction(prod);
			token = lexer.nextToken();
		}
	}

	public void readFirstFollow() {
		RDPToken token = lexer.nextToken();
		while(token.tokenType != RDPTokenType.DOUBLEPERCENT) {
			if(token.tokenType == RDPTokenType.LINE_END) {
				token = lexer.nextToken();
				continue;
			} else if(token.tokenType != RDPTokenType.IDENTIFIER) {
				error(RDPTokenType.IDENTIFIER, token.tokenType);
			}

			// Get the nonterminal we're referring to
			NonTerminal nt = nonTerminals.get(token.contents);
			if(nt == null) {
				error("Attempted to use nonregistered nonterminal for first/follow sets: " + token.contents);
			}

			// Next token must be a := symbol
			token = lexer.nextToken();
			if(token.tokenType != RDPTokenType.COLONEQUALS) {
				error(RDPTokenType.COLONEQUALS, token.tokenType);
			}
			// Nullable?
			token = lexer.nextToken();
			if(token.tokenType != RDPTokenType.IDENTIFIER) {
				error(RDPTokenType.IDENTIFIER, token.tokenType);
			} else if (token.contents.equals("yes")) {
				nt.setNullable(true);
			} else if(token.contents.equals("no")) {
				nt.setNullable(false);
			} else {
				error("Encountered identifier that was not 'yes' or 'no': " + token.contents);
			}
			// Colon
			token = lexer.nextToken();
			if(token.tokenType != RDPTokenType.SINGLECOLON) {
				error(RDPTokenType.SINGLECOLON, token.tokenType);
			}

			token = lexer.nextToken();
			while(token.tokenType != RDPToken.SINGLECOLON) {
				if (token.tokenType != RDPTokenType.IDENTIFIER) {
					error(RDPTokenType.IDENTIFIER, token.tokenType);
				}
				// Make sure the identifier is in the terminals set
				if(!terminals.containsKey(token.contents)) {
					error("Attempted to use nonregistered terminal for first set: " + token.contents);
				}
				// otherwise, add it
				nt.addToFirstSet(terminals.get(token.contents));
				token = lexer.nextToken();
			}

			token = lexer.nextToken();
			while(token.tokenType != RDPToken.LINE_END) {
				if (token.tokenType != RDPTokenType.IDENTIFIER) {
					error(RDPTokenType.IDENTIFIER, token.tokenType);
				}
				// Make sure the identifier is in the terminals set
				if(!terminals.containsKey(token.contents)) {
					error("Attempted to use nonregistered terminal for follow set: " + token.contents);
				}
				// otherwise, add it
				nt.addToFollowSet(terminals.get(token.contents));
				token = lexer.nextToken();
			}
		}
	}

	private void error(String message) {
		throw new RuntimeException("PARSER: " + message + " on line " + lexer.getLineIndex() + ", source index " + lexer.getSourceIndex());
	}
	
	private void error(int typeExpected, int typeGotten) {
		throw new RuntimeException("PARSER: Expected token of type " + RDPUtil.tokenStr(typeExpected) 
				+ " but instead got " + RDPUtil.tokenStr(typeGotten) + 
				" on line " + lexer.getLineIndex() + ", source index " + lexer.getSourceIndex());
	}
	
	public void summarize() {
		System.out.println("I know of these terminals:");
		for(String t : terminals.keySet()) {
			System.out.print(t + " ");
		}
		System.out.println();
		System.out.println();
		
		System.out.println("I know of these nonterminals:");
		for(String t : nonTerminals.keySet()) {
			System.out.print(t + " ");
		}
		System.out.println();
		System.out.println();
		
		System.out.println("The nonterminals have these productions: ");
		for(String t : nonTerminals.keySet()) {
			NonTerminal nt = nonTerminals.get(t);
			for(Production prod : nt.getProductions()) {
				System.out.print(t + " :: ");
				for(GrammarSymbol sym : prod.getGrammarSymbols()) {
					System.out.print(sym.getName() + " ");
				}
				System.out.println();
			}
		}
		System.out.println();

		System.out.println("Here are the FIRST sets for all of the nonterminals:");
		for(String t : nonTerminals.keySet()) {
			NonTerminal nt = nonTerminals.get(t);
			System.out.print(t + " := ");
			for(Terminal term : nt.getFirstSet()) {
				System.out.print(term.getName() + " ");
			}
			System.out.println();
		}
		System.out.println();

		System.out.println("Here are the FOLLOW sets for all of the nonterminals:");
		for(String t : nonTerminals.keySet()) {
			NonTerminal nt = nonTerminals.get(t);
			System.out.print(t + " := ");
			for(Terminal term : nt.getFollowSet()) {
				System.out.print(term.getName() + " ");
			}
			System.out.println();
		}
	}
	
	public static void printFileTokens(String filename) throws FileNotFoundException {
		FileReader reader = new FileReader(filename);
		RDPLexer lexer = new RDPLexer(reader);
		
		RDPToken token = lexer.nextToken();
		do {
			System.out.println(RDPUtil.tokenStr(token.tokenType));
			token = lexer.nextToken();
		} while(token.tokenType != RDPTokenType.EOF);
		System.out.println("OK");
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		if(args.length != 2) {
			System.err.println("usage: RDPParser grammar_filename lexername");
			System.exit(0);
		}
		String grammarFile = args[0];
		String lexerName = args[1];
		// doing fun debug things
		printFileTokens(grammarFile);
		
		RDPParser parser = new RDPParser(
				new RDPLexer(
				new FileReader(grammarFile)));
		
		parser.readTerminals();
		parser.readNonTerminals();
		parser.readProductions();
		parser.readFirstFollow();
		parser.summarize();

		RDPGenerator generator = new RDPGenerator("gen", lexerName, parser.terminals, parser.nonTerminals);
		try {
			generator.generateCode();
		} catch(IOException ex) {
			System.out.println("Could not generate code. Reason: " + ex.getMessage());
		}
	}
}
