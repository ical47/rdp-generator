
public class RDPUtil {
	public static String tokenStr(int tokenType) {
		switch(tokenType) {
		case RDPTokenType.COLONEQUALS: 
			return "COLONEQUALS";
		case RDPTokenType.DOUBLECOLON: 
			return "DOUBLECOLON";
		case RDPTokenType.DOUBLEPERCENT: 
			return "DOUBLEPERCENT";
		case RDPTokenType.IDENTIFIER: 
			return "IDENTIFIER";
		case RDPTokenType.LINE_END:
			return "LINE_END";
		case RDPTokenType.SINGLECOLON:
			return "SINGLECOLON";
		case RDPTokenType.EOF: 
			return "EOF";
		case RDPTokenType.ERROR: 
			return "ERROR";
		default:
			return "" + tokenType;
		}
	}
}
