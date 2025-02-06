
package pbgLecture3lab;

import java.awt.Color;
import java.awt.Graphics2D;

public class BasicParticle {
	/* Author: Michael Fairbank
	 * Creation Date: 2016-01-28
	 * Significant changes applied:
	 */
	public final int SCREEN_RADIUS;

	private Vect2D pos;
	private Vect2D vel;
	private final double radius;
	private final double mass;
	public final Color col;
	private Vect2D sideSpin;
	
	private final boolean improvedEuler;
    private final AnchoredBarrier velocityLine;

    public BasicParticle(double sx, double sy, double vx, double vy, double radius, boolean improvedEuler, Color col, double mass) {
		setPos(new Vect2D(sx,sy));
		setVel(new Vect2D(vx,vy));
		this.radius=radius;
		this.mass=mass;
		this.improvedEuler=improvedEuler;
		this.SCREEN_RADIUS=Math.max(BasicPhysicsEngine.convertWorldLengthToScreenLength(radius),1);
		this.col=col;

        if (col == Color.WHITE) {
            this.velocityLine = new AnchoredBarrier_StraightLine(sx, sy, sx, sy, Color.BLACK);
        } else {
            this.velocityLine = null;
        }
     // Initialize the side spin with zero vector
        this.sideSpin = new Vect2D(0, 0);
	}

    public void updateVelocityLine(Vect2D endPos) {
        if (this.velocityLine != null) {
            this.velocityLine.setEndPos(endPos);
            this.velocityLine.setStartPos(this.getPos());
        }
    }

    public AnchoredBarrier getVelocityLine() {
        return this.velocityLine;
    }
    public void update(double gravity, double deltaT) {
        double velocity_lower_threshold = 0.099;

        Vect2D currentVel = this.getVel();
        if ((Math.abs(currentVel.x) <= velocity_lower_threshold) || (Math.abs(currentVel.y) <= velocity_lower_threshold)) {
            this.setVel(new Vect2D(0, 0));
        }



		Vect2D acc=new Vect2D(0,-gravity);
		if (isImprovedEuler()) {
			Vect2D pos2=getPos().addScaled(getVel(), deltaT);// in theory this could be used,e.g. if acc2 depends on pos - but in this constant gravity field it will not be relevant
			Vect2D vel2=getVel().addScaled(acc, deltaT);
			Vect2D velAv=vel2.add(getVel()).mult(0.5);
			Vect2D acc2=new Vect2D(0,-gravity);//same as acc in this simple example of constant acceleration, but that won't generally be true
			Vect2D accAv=acc2.add(acc).mult(0.5);
			setPos(getPos().addScaled(velAv, deltaT));
			setVel(getVel().addScaled(accAv, deltaT));
		} else {
			// basic Euler
			setPos(getPos().addScaled(getVel(), deltaT));
			setVel(getVel().addScaled(acc, deltaT));
		}
		// Apply side spin after existing velocity updates if the ball is the cue ball

	    if (this.col == Color.WHITE) {
	        this.vel = this.vel.add(sideSpin); // Add the side spin to the velocity
	        // Reset the side spin to zero after each update to simulate a single effect per shot
	        this.sideSpin = new Vect2D(0, 0);

	    }
	}


	public void draw(Graphics2D g) {
		int x = BasicPhysicsEngine.convertWorldXtoScreenX(getPos().x);
		int y = BasicPhysicsEngine.convertWorldYtoScreenY(getPos().y);
		g.setColor(col);
		g.fillOval(x - SCREEN_RADIUS, y - SCREEN_RADIUS, 2 * SCREEN_RADIUS, 2 * SCREEN_RADIUS);

        if (this.velocityLine != null) {
            this.velocityLine.draw(g);
        }
	}

	public double getRadius() {
		return radius;
	}

	public Vect2D getPos() {
		return pos;
	}

	public void setPos(Vect2D pos) {
		this.pos = pos;
	}

	public Vect2D getVel() {
		return vel;
	}

	public void setVel(Vect2D vel) {
		this.vel = vel;
	}
	public double getSpeed() {
	    
	    return vel.mag(); // 'mag()' method should return the magnitude of the velocity vector
	}

	public boolean collidesWith(BasicParticle p2) {
		Vect2D vecFrom1to2 = Vect2D.minus(p2.getPos(), getPos());
		boolean movingTowardsEachOther = Vect2D.minus(p2.getVel(), getVel()).scalarProduct(vecFrom1to2)<0;
		return vecFrom1to2.mag()<getRadius()+p2.getRadius() && movingTowardsEachOther;
	}

	public static void implementElasticCollision(BasicParticle p1, BasicParticle p2, double e) {
		if (!p1.collidesWith(p2)) throw new IllegalArgumentException();
        Vect2D ab = new Vect2D(p2.getPos().x - p1.getPos().x, p2.getPos().y - p1.getPos().y);
        Vect2D n = ab.normalise();
        double jb = (e + 1) * (p1.getVel().scalarProduct(n) - p2.getVel().scalarProduct(n)) / ((1 / p1.mass) + (1 / p2.mass));
        Vect2D vb = p2.getVel().add(n.mult(jb/p2.mass));
        Vect2D va = Vect2D.minus(p1.getVel(), n.mult(jb/p1.mass));

        p1.setVel(va);
        p2.setVel(vb);
	}

	public boolean isImprovedEuler() {
		return improvedEuler;
	}

	public void setSideSpin(Vect2D sideSpin) {
		this.sideSpin = sideSpin;
		
	}


}
