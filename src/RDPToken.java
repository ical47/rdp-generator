
public class RDPToken implements RDPTokenType {
	public int tokenType;
	public String contents;
	
	public RDPToken(int tokenType, String contents) {
		this.tokenType = tokenType;
		this.contents = contents;
	}
}
