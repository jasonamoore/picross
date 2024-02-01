package state;

import java.awt.Color;

import engine.Engine;
import puzzle.Puzzle;
import state.element.Button;

public class TestScrollState extends ScrollableState {

	private Button[] games;
	
	public static Color randCol() {
		return new Color(
				(int) (Math.random() * 255),
				(int) (Math.random() * 255),
				(int) (Math.random() * 255)
			);
	}
	
	public TestScrollState(int innerWidth, int innerHeight) {
		super(innerWidth, innerHeight);
		games = new Button[10];
		for (int i = 0; i < games.length; i++) {
			Button b = new Button(randCol()) {
				@Override
				public void onClick() {
					super.onClick();
					backgroundColor = backgroundColor.darker();
				}

				@Override
				public void onRelease() {
					super.onRelease();
					if (!hovering) return;
					PuzzleState ps = new PuzzleState(new Puzzle(Puzzle.genPuzzle(7, 7)));
					Engine.getEngine().openState(ps);
				}
			};
			b.setBounds(i * 100, 20, 80, 300);
			games[i] = b;
			scrollContainer.add(b);
			Button back = new Button(Color.BLACK) {
				@Override
				public void onRelease() {
					super.onRelease();
					Engine.getEngine().exitTopState();
				}
			};
			back.setBounds(5, 5, 30, 30);
			back.setZ(20);
			add(back);
		}
	}

	@Override
	public void focus(int status) {
		// TODO Auto-generated method stub
		
	}

}
