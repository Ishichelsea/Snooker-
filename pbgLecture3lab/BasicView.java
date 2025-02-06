package pbgLecture3lab;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

public class BasicView extends JComponent {
	/* Author: Michael Fairbank
	 * Creation Date: 2016-01-28
	 * Significant changes applied:
	 */
	// background colour
	public static final Color BG_COLOR = Color.BLACK;

	private BasicPhysicsEngine game;

	public BasicView(BasicPhysicsEngine game) {
		this.game = game;
	}
	
	@Override
	public void paintComponent(Graphics g0) {
		BasicPhysicsEngine game;
		synchronized(this) {
			game=this.game;
		}
		Graphics2D g = (Graphics2D) g0;
		// paint the background
		g.setColor(BG_COLOR);
		g.fillRect(0, 0, getWidth(), getHeight());
		for (BasicParticle p : game.particles) {
			p.draw(g);
            drawSpeed(g, p);

}
		for (AnchoredBarrier b : game.barriers)
			b.draw(g);
        for (AnchoredBarrier b : game.pockets)
            b.draw(g);

        game.PointsDisplay.draw(g, "Points: " + game.score);
    
	}
	// Method to display the speed of each particle

    private void drawSpeed(Graphics2D g, BasicParticle p) {

        int x = BasicPhysicsEngine.convertWorldXtoScreenX(p.getPos().x);

        int y = BasicPhysicsEngine.convertWorldYtoScreenY(p.getPos().y) + 20; // Offset below the particle

        String speedText = String.format("Speed: %.1f m/s", p.getSpeed());

        g.setColor(Color.WHITE);

        g.drawString(speedText, x, y);

    }

	@Override
	public Dimension getPreferredSize() {
		return BasicPhysicsEngine.FRAME_SIZE;
	}
	
	public synchronized void updateGame(BasicPhysicsEngine game) {
		this.game=game;
	}
}
