package state.particle;

import java.awt.image.BufferedImage;

import util.Animation;

public class AnimatedParticle extends Particle {

	// static stuff
	
	public static void generateParticle(BufferedImage[] sheet, int frameSpeed, double initX, double initY, int velocityTime, double initXv, double initYv, double xForce, double yForce, int lifespan) {
		if (particleBelt[beltHead] == null)
			particleBelt[beltHead] = new AnimatedParticle();
		AnimatedParticle ap = (AnimatedParticle) particleBelt[beltHead];
		ap.mutateAll(sheet, frameSpeed, initX, initY, velocityTime, initXv, initYv, xForce, yForce, lifespan);
		beltHead = (beltHead + 1) % MAX_PARTICLES;
	}
	
	// non-static stuff

	private BufferedImage[] sheet;
	private Animation anim;
	
	private AnimatedParticle() {
		super();
	}
	
	protected BufferedImage getImage() {
		return sheet[Math.min(anim.getIntValue(), sheet.length)];
	}
	
	private void mutateAll(BufferedImage[] sheet, int frameSpeed, double initX, double initY, int velocityTime, double initXv, double initYv, double xForce, double yForce, int lifespan) {
		super.mutateAll(null, initX, initY, velocityTime, initXv, initYv, xForce, yForce, lifespan);
		this.sheet = sheet;
		anim = new Animation(0, sheet.length, frameSpeed * sheet.length, Animation.LINEAR, Animation.CONTINUE, true);
	}
	
}
