
public abstract class GrammarSymbol {
	private String name;
	
	protected GrammarSymbol(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
