package picnix.puzzle;

import engine.Engine;
import state.PuzzleState;
import state.element.Container;

public class FoodBar extends Container {

	public static final int HEIGHT = 100;
	
	// sparsely filled array where index corresponds to food id of the widget
	private FoodWidget[] widgets;
	
	public FoodBar(int[] foodList) {
		super(0, Engine.SCREEN_HEIGHT - HEIGHT,
				Engine.SCREEN_WIDTH, HEIGHT,
				calculateInnerWidth(foodList), HEIGHT);
		// create widgets
		widgets = new FoodWidget[PuzzleState.NUM_FOOD_TYPES];
		int widgetNum = 0;
		for (int i = 0; i < foodList.length; i++) {
			if (foodList[i] > 0) {
				widgets[i] = new FoodWidget(calculateWidgetX(widgetNum), i, foodList[i]);
				add(widgets[i]);
				widgetNum++;
			}
		}
	}

	public void incrementWidget(int foodId) {
		widgets[foodId].increment();
	}

	private int calculateWidgetX(int widgetNum) {
		int x = FoodWidget.MARGIN + (FoodWidget.SIZE + FoodWidget.MARGIN) * widgetNum;
		int centeringOffset = Math.max(0, (Engine.SCREEN_WIDTH - getInnerWidth()) / 2);
		return x + centeringOffset;
	}

	private static int calculateInnerWidth(int[] foodList) {
		int realSize = 0;
		for (int i = 0; i < foodList.length; i++)
			if (foodList[i] > 0)
				realSize++;
		return FoodWidget.MARGIN + (FoodWidget.SIZE + FoodWidget.MARGIN) * realSize;
	}
	
}