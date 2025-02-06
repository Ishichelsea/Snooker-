package pbgLecture3lab;

import pbgLecture3lab.BasicParticle;
import pbgLecture3lab.BasicPhysicsEngine;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import static pbgLecture3lab.BasicPhysicsEngine.convertScreenXtoWorldX;
import static pbgLecture3lab.BasicPhysicsEngine.convertScreenYtoWorldY;

public class CustomMouseListener implements MouseListener, MouseMotionListener {
    private final BasicPhysicsEngine game;

    public CustomMouseListener(BasicPhysicsEngine game) {
        this.game = game;
    }

    private Vect2D getMousePos(MouseEvent e) {
        double mouseX = convertScreenXtoWorldX(e.getX());
        double mouseY = convertScreenYtoWorldY(e.getY());

        return new Vect2D(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {
        Vect2D mouseReleasePos = getMousePos(e);
        for (BasicParticle p : game.particles) {
            if (p.col == Color.WHITE) {
                Vect2D cueBallCenter = p.getPos();
                // Determine the direction of the spin from the cue ball's center to the mouse release position
                Vect2D spinDirection = Vect2D.minus(mouseReleasePos, cueBallCenter).normalise();
                double sideSpinMagnitude = 0.5; 
                Vect2D sideSpin = spinDirection.mult(sideSpinMagnitude);
                p.setSideSpin(sideSpin);
                // Apply the velocity from the dragging action
                Vect2D diff = Vect2D.minus(p.getVelocityLine().getEndPos(), p.getVelocityLine().getStartPos());
                double speed = diff.mag() * 5;
                p.updateVelocityLine(p.getPos());
                p.getVelocityLine().setColor(Color.BLACK);
                p.setVel(p.getVel().add(new Vect2D(-diff.x, -diff.y)).mult(speed));
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        this.handleMouseMovement(e);
    }

    private void handleMouseMovement(MouseEvent e) {
        Vect2D mousePos = this.getMousePos(e);

        for (BasicParticle p : game.particles) {
            if (p.col == Color.WHITE) {
                p.getVelocityLine().setColor(Color.GREEN);
                p.updateVelocityLine(mousePos);
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {}
}
