import java.util.ArrayList;
import java.util.HashSet;


public class NonTerminal extends GrammarSymbol {
	private ArrayList<Production> productions = new ArrayList<>();
	private HashSet<Terminal> firstSet = new HashSet<>();
	private HashSet<Terminal> followSet = new HashSet<>();
	private boolean nullable;
	private boolean startToken = false;
	
	public NonTerminal(String name) {
		super(name);
	}

	public NonTerminal(String name, boolean startToken) {
		super(name);
		this.startToken = startToken;
	}

	public void addProduction(Production prod) {
		productions.add(prod);
	}
	
	public void addToFirstSet(Terminal value) {
		firstSet.add(value);
	}
	
	public void addToFollowSet(Terminal value) {
		followSet.add(value);
	}

	public boolean isNullable() {
		return nullable;
	}

	public boolean isStartToken() {
		return startToken;
	}

	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	public ArrayList<Production> getProductions() {
		return productions;
	}

	public Terminal[] getFirstSet() {
		Terminal[] copy = new Terminal[firstSet.size()];
		firstSet.toArray(copy);
		return copy;
	}

	public Terminal[] getFollowSet() {
		Terminal[] copy = new Terminal[followSet.size()];
		followSet.toArray(copy);
		return copy;
	}
}
