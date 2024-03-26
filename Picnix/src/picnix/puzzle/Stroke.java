package picnix.puzzle;

public class Stroke {
	
	public static int STROKE_BANK_SIZE = 50;
	private static Stroke[] strokeBelt = new Stroke[STROKE_BANK_SIZE];
	
	private static int beltHead = 0;
	
	private int numChanged;
	private int[][] changed;
	
	private boolean mistaken;
	private int[] mistake;
	
	private int layerId;

	public static final int ROW = 0;
	public static final int COL = 1;
	public static final int MARK = 2;
	
	private static final int MAX_CHANGES = 500;
	
	private Stroke(int layerId) {
		this.layerId = layerId;
		changed = new int[10][3];
		mistake = new int[3];
	}
	
	public static Stroke newStroke(int layerId) {
		if (strokeBelt[beltHead] == null)
			strokeBelt[beltHead] = new Stroke(layerId);
		else
			strokeBelt[beltHead].numChanged = 0;
		Stroke newStroke = strokeBelt[beltHead];
		beltHead = (beltHead + 1) % STROKE_BANK_SIZE;
		return newStroke;
	}
	
	public int size() {
		return numChanged;
	}

	public boolean isEmpty() {
		return numChanged == 0 && !mistaken;
	}
	
	public int getLayerId() {
		return layerId;
	}
	
	public boolean hasMistake() {
		return mistaken;
	}
	
	public int getMistakeRow() {
		return mistake[ROW];
	}
	
	public int getMistakeCol() {
		return mistake[COL];
	}
	
	public int getMistakeMark() {
		return mistake[MARK];
	}
	
	public int[] getRecentChange(int lookback) {
		if (lookback >= numChanged)
			return null;
		else
			return changed[numChanged - lookback - 1];
	}
	
	public int[] getChange(int index) {
		return changed[index];
	}
	
	public void addChange(int row, int col, int prevType, boolean miss) {
		if (miss) {
			// don't save change, but save its location
			mistaken = true;
			mistake[ROW] = row;
			mistake[COL] = col;
			mistake[MARK] = prevType;
		}
		else {
			if (numChanged == changed.length)
				if (!upsize()) return;
			changed[numChanged][ROW] = row;
			changed[numChanged][COL] = col;
			changed[numChanged][MARK] = prevType;
			numChanged++;
		}
	}
	
	private boolean upsize() {
		if (numChanged >= MAX_CHANGES)
			return false;
		int[][] newChanged = new int[changed.length * 2][3];
		for (int i = 0; i < changed.length; i++)
			newChanged[i] = changed[i];
		changed = newChanged;
		return true;
	}
	
}
