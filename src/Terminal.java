
public class Terminal extends GrammarSymbol {
	public Terminal(String name) {
		super(name);
	}
	
	@Override
	public int hashCode() {
		return getName().hashCode();
	}
}
