package picnix.puzzle;

import java.awt.Color;
import java.awt.Graphics;

import engine.Input;
import resource.bank.Palette;
import state.PuzzleState;
import state.element.Element;

public class FoodWidget extends Element {
	
	public static int SIZE = 50;
	public static int MARGIN = 25;
	
	private int foodId;
	private int amount;

	public FoodWidget(int x, int foodId, int amount) {
		super(x, MARGIN, SIZE, SIZE);
		this.foodId = foodId;
		this.amount = amount;
	}
	
	@Override
	public void onClick(int mbutton) {
		super.onClick(mbutton);
		if (mbutton == Input.LEFT_CLICK && amount > 0) {
			PuzzleState ps = (PuzzleState) state;
			ps.dragNewFood(this);
			decrement();
		}
	}
	
	@Override
	public void onRelease(int mbutton) {
		super.onRelease(mbutton);
		if (mbutton == Input.LEFT_CLICK) {
			PuzzleState ps = (PuzzleState) state;
			ps.tryDropNewFood();
		}
	}
	
	public int getFoodId() {
		return foodId;
	}
	
	public void increment() {
		amount++;
		//setEnabled(amount > 0);
	}
	
	private void decrement() {
		amount--;
		//setEnabled(amount > 0);
	}
	
	public int getAmount() {
		return amount;
	}

	@Override
	public void render(Graphics g) {
		g.setColor(Palette.PURPLE);
		g.fillRect(getDisplayX(), getDisplayY(), getWidth(), getHeight());
		g.setColor(Color.WHITE);
		g.drawString(
				PuzzleState.getFoodById(foodId)[0] + "x" + 
						PuzzleState.getFoodById(foodId)[1], getDisplayX(), getDisplayY() + 10);
		g.drawString(amount + "", getDisplayX(), getDisplayY() + 30);
	}
	
}