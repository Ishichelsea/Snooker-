package pbgLecture3lab;

import java.awt.*;

public abstract class AnchoredBarrier {
    /* Author: Michael Fairbank
	 * Creation Date: 2016-01-28
	 * Significant changes applied:
	 */
	public abstract Vect2D calculateVelocityAfterACollision(Vect2D pos, Vect2D vel,double e);
	public abstract boolean isCircleCollidingBarrier(Vect2D circleCentre, double radius);
	public abstract void draw(Graphics2D g);
    public abstract void setEndPos(Vect2D pos);
    public abstract void setStartPos(Vect2D pos);
    public abstract Vect2D getEndPos();
    public abstract Vect2D getStartPos();
    public abstract void setColor(Color col);
}
