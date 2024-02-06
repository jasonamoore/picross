package state;

import engine.Engine;
import resource.bank.ImageBank;
import state.element.Button;
import state.element.Icon;

public class TitleState extends State {
	
	public TitleState() {
		Icon titleIcon = new Icon(ImageBank.title);
		titleIcon.setBounds(50, 50, 300, 150);
		Button play = new Button(100, 250, 200, 100) {
			public void onRelease(int mbutton) {
				super.onRelease(mbutton);
				Engine.getEngine().openState(
						new TestScrollState(1000, 600)
					);
			}
			public void render(java.awt.Graphics g) {
				g.setColor(java.awt.Color.YELLOW);
				g.fillRect(getDisplayX(), getDisplayY(), width, height);
			}
		};
		add(titleIcon);
		add(play);
	}

	@Override
	public void focus(int status) {
		// TODO Auto-generated method stub
		
	}

}
