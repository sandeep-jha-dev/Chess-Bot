package chess.engine;

public class Move {
	public int startRow, startCol, endRow, endCol;
	public boolean isCastling = false;
	public boolean isEnPassant = false;
	public boolean isPromotion = false;
	
	public Move(int sr, int sc, int er, int ec) {
		this.startRow = sr;
		this.startCol = sc;
		this.endRow = er;
		this.endCol = ec;
	}
	public boolean equals(int sr, int sc, int er, int ec) {
		return (startRow == sr && startCol == sc && endRow == er && endCol == ec);
	}
}