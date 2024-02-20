package state;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import engine.Engine;
import engine.StateManager;
import engine.Transition;
import resource.bank.ImageBank;
import resource.bank.Palette;
import state.element.Icon;
import state.element.TiledButton;
import util.Animation;

public class TitleState extends State {
	
	private Animation smoothRot;
	
	public TitleState() {
		smoothRot = new Animation(0, 2 * Math.PI, 10000, Animation.LINEAR, Animation.CONTINUE, true);
		Icon titleIcon = new Icon(ImageBank.title);
		titleIcon.setBounds(Engine.getScreenCenterX(350), 50, 350, 125);
		TiledButton play = new TiledButton(Engine.getScreenCenterX(200), 250, 200, 100) {
			public void onRelease(int mbutton) {
				super.onRelease(mbutton);
				if (beingHovered())
					playButtonClicked();
			}
		};
		play.setAllTileMaps(ImageBank.bluebutton, ImageBank.bluebuttonclick, ImageBank.buttondisabled);
		add(titleIcon);
		add(play);
	}

	@Override
	public void focus(int status) {
		// TODO Auto-generated method stub
	}
	
	private void playButtonClicked() {
		Engine.getEngine().getStateManager().transitionToState(new WorldSelectState(smoothRot.getValue()), Transition.FADE, 1000, 1000, NEWLY_OPENED);
	}
			
	@Override
	public void render(Graphics g) {
		double rot = smoothRot.getValue();
		g.setColor(Palette.SKY);
		g.fillRect(0, 0, Engine.SCREEN_WIDTH, 150);
		g.setColor(Palette.PERIWINKLE);
		g.fillRect(0, 150, Engine.SCREEN_WIDTH, Engine.SCREEN_HEIGHT - 150);
		Graphics2D gg = (Graphics2D) g;
		AffineTransform oldTrans = gg.getTransform();
		gg.setClip(0, 150, Engine.SCREEN_WIDTH, Engine.SCREEN_HEIGHT - 150);
		gg.translate(Engine.SCREEN_WIDTH / 2, 75 + Engine.SCREEN_HEIGHT / 2);
        gg.scale(0.75, 0.35);
        gg.rotate(-rot);
		gg.translate(-Engine.SCREEN_WIDTH / 2, -Engine.SCREEN_HEIGHT / 2);
		gg.drawImage(ImageBank.island, 0, 0, null);
		gg.setClip(null);
		gg.setTransform(oldTrans);
		// render normal stuff
		super.render(g);
	}

}
