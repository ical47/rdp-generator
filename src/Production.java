import java.util.ArrayList;


public class Production {
	private ArrayList<GrammarSymbol> grammarSymbols = new ArrayList<>();
	
	public void addGrammarSymbol(GrammarSymbol sym) {
		grammarSymbols.add(sym);
	}
	
	public ArrayList<GrammarSymbol> getGrammarSymbols() {
		return grammarSymbols;
	}
}
