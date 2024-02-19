package picnic;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Particle {

	// static stuff
	
	private static final int MAX_PARTICLES = 100;
	private static Particle[] particleBelt = new Particle[MAX_PARTICLES];
	private static int beltHead;
	
	public static void generateParticle(BufferedImage image, double initX, double initY, int velocityTime, double initXv, double initYv, double xForce, double yForce, int lifespan) {
		if (particleBelt[beltHead] == null)
			particleBelt[beltHead] = new Particle();
		particleBelt[beltHead].mutateAll(image, initX, initY, velocityTime, initXv, initYv, xForce, yForce, lifespan);
		beltHead = (beltHead + 1) % MAX_PARTICLES;
	}
	
	public static void renderParticles(Graphics g) {
		long now = System.currentTimeMillis();
		for (int i = 0; i < MAX_PARTICLES; i++) {
			Particle p = particleBelt[i];
			if (p != null && now - p.birthTime < p.lifespan) {
				int elapsed = (int) (now - p.birthTime);
				int px = (int) (p.initX + Math.min(p.velocityTime, elapsed) * p.initXv + Math.pow(elapsed * p.xForce, 2) * Math.signum(p.xForce));
				int py = (int) (p.initY + Math.min(p.velocityTime, elapsed) * p.initYv + Math.pow(elapsed * p.yForce, 2) * Math.signum(p.yForce));
				g.drawImage(p.image, px, py, null);
			}
		}
	}
	
	// non-static stuff

	private BufferedImage image;
	
	private double initX, initY;
	private double initXv, initYv;
	private double xForce, yForce;
	
	private int lifespan;
	private long birthTime;
	private int velocityTime;

	// to prevent outside initialization
	private Particle() {}
	
	private void mutateAll(BufferedImage image, double initX, double initY, int velocityTime, double initXv, double initYv, double xForce, double yForce, int lifespan) {
		this.image = image;
		this.initX = initX;
		this.initY = initY;
		this.velocityTime = velocityTime;
		this.initXv = initXv / 1000.0;
		this.initYv = initYv / 1000.0;
		this.xForce = xForce / 1000.0;
		this.yForce = yForce / 1000.0;
		this.lifespan = lifespan;
		this.birthTime = System.currentTimeMillis();
	}
	
}
