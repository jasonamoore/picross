package picnic;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Particle {

	// static stuff

	public static final double GRAVITY = 20;
	public static final double FRICTION = 0.95;
	
	protected static final int MAX_PARTICLES = 100;
	protected static Particle[] particleBelt = new Particle[MAX_PARTICLES];
	protected static int beltHead;
	
	public static void generateParticle(BufferedImage image, double initX, double initY,  double initXv, double initYv, double velDamp, double xForce, double yForce, int lifespan) {
		if (particleBelt[beltHead] == null)
			particleBelt[beltHead] = new Particle();
		particleBelt[beltHead].mutateAll(image, initX, initY, initXv, initYv, velDamp, xForce, yForce, lifespan);
		beltHead = (beltHead + 1) % MAX_PARTICLES;
	}
	
	public static void renderParticles(Graphics g) {
		long now = System.currentTimeMillis();
		for (int i = 0; i < MAX_PARTICLES; i++) {
			Particle p = particleBelt[i];
			if (p != null && now - p.birthTime < p.lifespan) {
				int elapsed = (int) (now - p.birthTime);
				double dampening = elapsed * Math.pow(p.velDamp, elapsed / 500.0);
				int px = (int) (p.initX + p.initXv * dampening + Math.pow(elapsed * p.xForce, 2) * Math.signum(p.xForce));
				int py = (int) (p.initY + p.initYv * dampening + Math.pow(elapsed * p.yForce, 2) * Math.signum(p.yForce));
				g.drawImage(p.getImage(), px, py, null);
			}
		}
	}
	
	// non-static stuff

	private BufferedImage image;
	
	private double initX, initY;
	private double initXv, initYv;
	private double velDamp;
	private double xForce, yForce;
	
	private int lifespan;
	private long birthTime;

	// to prevent outside initialization
	protected Particle() {}
	
	protected BufferedImage getImage() {
		return image;
	}
	
	protected void mutateAll(BufferedImage image, double initX, double initY, double initXv, double initYv, double velDamp, double xForce, double yForce, int lifespan) {
		this.image = image;
		this.initX = initX;
		this.initY = initY;
		this.initXv = initXv / 1000.0;
		this.initYv = initYv / 1000.0;
		this.velDamp = velDamp;
		this.xForce = xForce / 1000.0;
		this.yForce = yForce / 1000.0;
		this.lifespan = lifespan;
		this.birthTime = System.currentTimeMillis();
	}
	
}
