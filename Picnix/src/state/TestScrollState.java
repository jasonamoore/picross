package state;

import java.awt.Color;

import engine.Engine;
import puzzle.Puzzle;
import state.element.Button;

public class TestScrollState extends ScrollableState {
	
	public static Color randCol() {
		return new Color(
				(int) (Math.random() * 255),
				(int) (Math.random() * 255),
				(int) (Math.random() * 255)
			);
	}
	
	public TestScrollState(int innerWidth, int innerHeight) {
		super(innerWidth, innerHeight);
		for (int i = 0; i < 10; i++) {
			Button b = new Button(i * 100, 20, 80, 300) {
				@Override
				public void onRelease() {
					super.onRelease();
					if (!hovering) return;
					PuzzleState ps = new PuzzleState(new Puzzle(Puzzle.genPuzzle(7, 7)));
					Engine.getEngine().openState(ps);
				}
				@Override
				public void render(java.awt.Graphics g) {
					g.setColor(java.awt.Color.YELLOW);
					if (clicking)
						g.setColor(java.awt.Color.ORANGE);
					g.fillRect(getDisplayX(), getDisplayY(), width, height);
				}
			};
			scrollContainer.add(b);
		}
		Button back = new Button(5, 5, 30, 30) {
			@Override
			public void onRelease() {
				super.onRelease();
				if (!hovering) return;
				Engine.getEngine().exitTopState();
			}
			@Override
			public void render(java.awt.Graphics g) {
				g.setColor(java.awt.Color.BLACK);
				g.fillRect(getDisplayX(), getDisplayY(), width, height);
			}
		};
		back.setZ(20);
		add(back);
	}

	@Override
	public void focus(int status) {
		// TODO Auto-generated method stub
		
	}

}
