package picnic;

public class Stroke {
	
	private int numChanged;
	private int[][] changed;
	
	private int layerId;

	public static final int ROW = 0;
	public static final int COL = 1;
	public static final int MARK = 2;
	
	private static final int MAX_CHANGES = 10000;
	
	public Stroke(int layid) {
		this.layerId = layid;
		changed = new int[10][3];
	}
	
	public int size() {
		return numChanged;
	}
	
	public int getLayerId() {
		return layerId;
	}
	
	public int[] getRecentChange(int lookback) {
		if (lookback >= numChanged)
			return null;
		else
			return changed[numChanged - lookback];
	}
	
	public int[] getChange(int index) {
		return changed[index];
	}
	
	public void addChange(int row, int col, int prevType) {
		if (numChanged == changed.length)
			if (!upsize()) return;
		changed[numChanged++] = new int[] {row, col, prevType};
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
