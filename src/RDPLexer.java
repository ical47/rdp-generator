import java.io.IOException;
import java.io.Reader;


public class RDPLexer implements RDPTokenType {

	private Reader reader;
	private int currChar = 0;
	private boolean reachedEOF = false;
	private int sourceIndex = -1;
	private int lineIndex = 1;
	
	public RDPLexer(Reader reader) {
		this.reader = reader;
		next();
	}
	
	public void reset() throws IOException {
		reader.reset();
		currChar = 0;
		reachedEOF = false;
		sourceIndex = -1;
		lineIndex = 1;
	}
	
	public RDPToken nextToken() {
		while(Character.isWhitespace(currChar) && currChar != '\n')
			next();
		
		if(reachedEOF) {
			return new RDPToken(EOF, "");
		} else if(currChar == -1) {
			return new RDPToken(ERROR, "");
		} else if(currChar == '\n') {
			next();
			return new RDPToken(LINE_END, "");
		} else if(Character.isJavaIdentifierStart(currChar)) {
			String contents = "" + (char)currChar;
			next();
			while(Character.isJavaIdentifierPart(currChar)) {
				contents += (char)currChar;
				next();
			}
			return new RDPToken(IDENTIFIER, contents);
		} else if(currChar == ':') {
			next();
			switch(currChar) {
			case ':':
				next();
				return new RDPToken(DOUBLECOLON, "::");
			case '=':
				next();
				return new RDPToken(COLONEQUALS, ":=");
			default:
				// probably some other character, no need to skip past it because we've landed there already
				return new RDPToken(SINGLECOLON, ":");
			}
		} else if(currChar == '%') {
			next();
			if(currChar != '%')
				error('%');
			next();
			return new RDPToken(DOUBLEPERCENT, "%%");
		} else {
			error();
			return new RDPToken(ERROR, "");
		}
	}
	
	private void next() {
		if(reachedEOF)
			return;
		
		try {
			currChar = reader.read();
			if(currChar == -1)
				reachedEOF = true;
			else if(currChar == '\n')
				lineIndex++;
			sourceIndex++;
		} catch (IOException ex) {
			ex.printStackTrace();
			currChar = -1;
		}
	}
	
	private void error(char expected) {
		String got = "" + (char)currChar;
		switch(currChar) {
		case ' ':
			got = "<space>";
			break;
		case '\n':
			got = "\\n";
			break;
		case '\t':
			got = "\\t";
			break;
		case '\r':
			got = "\\r";
			break;
		}
		throw new RuntimeException("LEXER: Error on line " + lineIndex + ", source index " + sourceIndex + 
				": expected " + expected + ", instead got " + got);
	}
	
	private void error() {
		throw new RuntimeException("LEXER: Error on line " + lineIndex + ", source index " + sourceIndex + 
				": unexpected character " + (char)currChar);
	}
	
	public int getLineIndex() {
		return lineIndex;
	}
	
	public int getSourceIndex() {
		return sourceIndex;
	}
}
