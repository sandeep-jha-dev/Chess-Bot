package chess.engine;

public class TTEntry {
    public long zobristKey;
    public int depth;
    public int score;
    public int flag; 
    // Flags for Alpha-Beta bounds
    public static final int EXACT = 0; // We searched the whole branch perfectly
    public static final int ALPHA = 1; // It was a terrible move (Upper Bound)
    public static final int BETA = 2;  // It was too good, opponent will veto (Lower Bound)

    public TTEntry(long zobristKey, int depth, int score, int flag) {
        this.zobristKey = zobristKey;
        this.depth = depth;
        this.score = score;
        this.flag = flag;
    }
}