package picnic;

import util.Animation;

public  class Food {

	public int xPos;
	public Animation drop;
	
	public Food(int xPos, int endYPos) {
		this.xPos = xPos;
		// for now, simple animation from a to b
		drop = new Animation(
				new double[] {0, endYPos},
				new int[] {500, 0},
				new double[][] {Animation.LINEAR, Animation.HOLD},
				0, Animation.NO_LOOP, true
				);
	}
	
}
