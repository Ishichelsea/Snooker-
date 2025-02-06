package pbgLecture3lab;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;


public class BasicPhysicsEngine {
	/* Author: Michael Fairbank
	 * Creation Date: 2016-01-28
	 * Significant changes applied:
	 */
	
	// frame dimensions
	public static final int SCREEN_HEIGHT = 680;
	public static final int SCREEN_WIDTH = 640;
	public static final Dimension FRAME_SIZE = new Dimension(
			SCREEN_WIDTH, SCREEN_HEIGHT);
	public static final double WORLD_WIDTH=10;//metres
	public static final double WORLD_HEIGHT=SCREEN_HEIGHT*(WORLD_WIDTH/SCREEN_WIDTH);// meters - keeps world dimensions in same aspect ratio as screen dimensions, so that circles get transformed into circles as opposed to ovals
	public static final double GRAVITY=0.0;  // gravity off

	// sleep time between two drawn frames in milliseconds 
	public static final int DELAY = 20;
	public static final int NUM_EULER_UPDATES_PER_SCREEN_REFRESH=10;
	// estimate for time between two frames in seconds 
	public static final double DELTA_T = DELAY / 1000.0 / NUM_EULER_UPDATES_PER_SCREEN_REFRESH ;
	
	
	public static int convertWorldXtoScreenX(double worldX) {
		return (int) (worldX/WORLD_WIDTH*SCREEN_WIDTH);
	}
	public static int convertWorldYtoScreenY(double worldY) {
		// minus sign in here is because screen coordinates are upside down.
		return (int) (SCREEN_HEIGHT-(worldY/WORLD_HEIGHT*SCREEN_HEIGHT));
	}
	public static int convertWorldLengthToScreenLength(double worldLength) {
		return (int) (worldLength/WORLD_WIDTH*SCREEN_WIDTH);
	}
    public static double convertScreenXtoWorldX(int screenX) {
        // to get this to work you need to program the inverse function to convertWorldXtoScreenX
        // this means rearranging the equation z=(worldX/WORLD_WIDTH*SCREEN_WIDTH) to make worldX the subject,
        // and then returning worldX
        return (screenX * WORLD_WIDTH) / SCREEN_WIDTH;
    }
    public static double convertScreenYtoWorldY(int screenY) {
        // to get this to work you need to program the inverse function to convertWorldYtoScreenY
        // this means rearranging the equation z= (SCREEN_HEIGHT-(worldY/WORLD_HEIGHT*SCREEN_HEIGHT)) to make
        // worldY the subject, and then returning worldY
        return -1 * ((screenY - SCREEN_HEIGHT) * WORLD_HEIGHT) / SCREEN_HEIGHT;
    }
	
	
	
	public List<BasicParticle> particles;
	public List<AnchoredBarrier> barriers;
    public List<AnchoredBarrier> pockets;
    private Vect2D cueStartPos;
	public int score;
    public PointsDisplay PointsDisplay;

	public static enum LayoutMode {CONVEX_ARENA, CONCAVE_ARENA, CONVEX_ARENA_WITH_CURVE, PINBALL_ARENA, RECTANGLE, SNOOKER_TABLE};

    private void makeSnookerBalls(double ballRadius) {
        double ballMass = 2;
        double ballMidX = ballRadius + WORLD_WIDTH / 2 - 2.1;

        this.cueStartPos = new Vect2D(ballMidX, WORLD_HEIGHT - 9);

        // Cue
        particles.add(new BasicParticle(this.cueStartPos.x, this.cueStartPos.y, 0, 0, ballRadius, true, Color.WHITE, ballMass));

        // GRAY
        particles.add(new BasicParticle(ballMidX, WORLD_HEIGHT - 1.5, 0, 0, ballRadius, true, Color.GRAY, ballMass));

        // Reds
        for (int i = 0; i <= 5; ++i) {
            double offsetY = (i * 2 * ballRadius) + (WORLD_HEIGHT - 4);
            double startX = 0;

            if (i % 2 == 0) {
                startX = ballMidX - ((double) i / 2 - 0.5) * (2 * ballRadius);
            } else {
                startX = ballMidX - ((double) (i - 1) / 2) * (2 * ballRadius);
            }

            for (int j = 0; j < i; j++) {
                particles.add(
                        new BasicParticle(
                                (j * 2 * ballRadius) + startX, offsetY, 0, 0, ballRadius, true, Color.RED, ballMass));
            }
        }

        // Pink
        particles.add(new BasicParticle(ballMidX, WORLD_HEIGHT - 4, 0, 0, ballRadius, true, Color.PINK, ballMass));

        // Blue
        particles.add(new BasicParticle(ballMidX, WORLD_HEIGHT - 5.75, 0, 0, ballRadius, true, Color.BLUE, ballMass));

        // Green, CYAN, Yellow
        particles.add(new BasicParticle(ballMidX - 1, WORLD_HEIGHT - 7.75, 0, 0, ballRadius, true, Color.GREEN, ballMass));
        particles.add(new BasicParticle(ballMidX, WORLD_HEIGHT - 7.75, 0, 0, ballRadius, true, Color.CYAN, ballMass));
        particles.add(new BasicParticle(ballMidX + 1, WORLD_HEIGHT - 7.75, 0, 0, ballRadius, true, Color.YELLOW, ballMass));
    }

	
	public BasicPhysicsEngine() {
		barriers = new ArrayList<AnchoredBarrier>();
		// empty particles array, so that when a new thread starts it clears current particle state:
		particles = new ArrayList<BasicParticle>();
        pockets = new ArrayList<AnchoredBarrier>();
        score = 0;
        PointsDisplay = new PointsDisplay(SCREEN_WIDTH, SCREEN_HEIGHT);

		LayoutMode layout = LayoutMode.SNOOKER_TABLE;

		double r = 0.15;

        if (layout == LayoutMode.SNOOKER_TABLE) {
            this.makeSnookerBalls(r);
        } else {
            particles.add(new BasicParticle(r + WORLD_WIDTH / 2 - 1, WORLD_HEIGHT / 2, 3.5, 5.2, r, true, Color.YELLOW, 2));
            particles.add(new BasicParticle(r + WORLD_WIDTH / 2 - 2, WORLD_HEIGHT / 2, -3.5, 5.2, r * 2, true, Color.DARK_GRAY, 4));
            particles.add(new BasicParticle(r + WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 5.5, -5.2, r * 4, true, Color.GREEN, 8));
        }

		barriers = new ArrayList<AnchoredBarrier>();
		
		switch (layout) {
			case RECTANGLE: {
				// rectangle walls:
				// anticlockwise listing
				barriers.add(new AnchoredBarrier_StraightLine(0, 0, WORLD_WIDTH, 0, Color.WHITE));
				barriers.add(new AnchoredBarrier_StraightLine(WORLD_WIDTH, 0, WORLD_WIDTH, WORLD_HEIGHT, Color.WHITE));
				barriers.add(new AnchoredBarrier_StraightLine(WORLD_WIDTH, WORLD_HEIGHT, 0, WORLD_HEIGHT, Color.WHITE));
				barriers.add(new AnchoredBarrier_StraightLine(0, WORLD_HEIGHT, 0, 0, Color.WHITE));
				break;
			}
			case CONVEX_ARENA: {
				barriers.add(new AnchoredBarrier_StraightLine(0,WORLD_HEIGHT/3, WORLD_WIDTH/2, 0, Color.WHITE));
				barriers.add(new AnchoredBarrier_StraightLine(WORLD_WIDTH/2, 0, WORLD_WIDTH, WORLD_HEIGHT/3, Color.WHITE));
				barriers.add(new AnchoredBarrier_StraightLine(WORLD_WIDTH, WORLD_HEIGHT/3, WORLD_WIDTH, WORLD_HEIGHT, Color.WHITE));
				barriers.add(new AnchoredBarrier_StraightLine(WORLD_WIDTH, WORLD_HEIGHT, 0, WORLD_HEIGHT, Color.WHITE));
				barriers.add(new AnchoredBarrier_StraightLine(0, WORLD_HEIGHT, 0, WORLD_HEIGHT/3, Color.WHITE));
				break;
			}
			case CONCAVE_ARENA: {
				barriers.add(new AnchoredBarrier_StraightLine(0,WORLD_HEIGHT/3, WORLD_WIDTH/2, 0, Color.WHITE));
				barriers.add(new AnchoredBarrier_StraightLine(WORLD_WIDTH/2, 0, WORLD_WIDTH, WORLD_HEIGHT/3, Color.WHITE));
				barriers.add(new AnchoredBarrier_StraightLine(WORLD_WIDTH, WORLD_HEIGHT/3, WORLD_WIDTH, WORLD_HEIGHT, Color.WHITE));
				barriers.add(new AnchoredBarrier_StraightLine(WORLD_WIDTH, WORLD_HEIGHT, 0, WORLD_HEIGHT, Color.WHITE));
				barriers.add(new AnchoredBarrier_StraightLine(0, WORLD_HEIGHT, 0, WORLD_HEIGHT/3, Color.WHITE));
				double width=WORLD_HEIGHT/20;
				barriers.add(new AnchoredBarrier_StraightLine(0, WORLD_HEIGHT*2/3, WORLD_WIDTH/2, WORLD_HEIGHT*1/2, Color.WHITE,width/10));
				barriers.add(new AnchoredBarrier_StraightLine(WORLD_WIDTH/2, WORLD_HEIGHT*1/2, WORLD_WIDTH/2, WORLD_HEIGHT*1/2-width, Color.WHITE,width/10));
				barriers.add(new AnchoredBarrier_StraightLine(WORLD_WIDTH/2, WORLD_HEIGHT*1/2-width, 0, WORLD_HEIGHT*2/3-width, Color.WHITE,width/10));
                barriers.add(new AnchoredBarrier_Point(WORLD_WIDTH/2, WORLD_HEIGHT*1/2, 0.0));
                barriers.add(new AnchoredBarrier_Point(WORLD_WIDTH/2, WORLD_HEIGHT*1/2-width, 0.0));
				break;
			}
			case CONVEX_ARENA_WITH_CURVE: {
				barriers.add(new AnchoredBarrier_StraightLine(0,WORLD_HEIGHT/3, WORLD_WIDTH/2, 0, Color.WHITE));
				barriers.add(new AnchoredBarrier_StraightLine(WORLD_WIDTH/2, 0, WORLD_WIDTH, WORLD_HEIGHT/3, Color.WHITE));
				barriers.add(new AnchoredBarrier_StraightLine(WORLD_WIDTH, WORLD_HEIGHT/3, WORLD_WIDTH, WORLD_HEIGHT, Color.WHITE));
				barriers.add(new AnchoredBarrier_StraightLine(WORLD_WIDTH, WORLD_HEIGHT, 0, WORLD_HEIGHT, Color.WHITE));
				barriers.add(new AnchoredBarrier_StraightLine(0, WORLD_HEIGHT, 0, 0, Color.WHITE));
				barriers.add(new AnchoredBarrier_Curve(WORLD_WIDTH/2, WORLD_HEIGHT-WORLD_WIDTH/2, WORLD_WIDTH/2, 0.0, 180.0,true, Color.WHITE));
				break;
			}
			case PINBALL_ARENA: {
				// simple pinball board
				barriers.add(new AnchoredBarrier_StraightLine(0, 0, WORLD_WIDTH, 0, Color.WHITE));
				barriers.add(new AnchoredBarrier_StraightLine(WORLD_WIDTH, 0, WORLD_WIDTH, WORLD_HEIGHT, Color.WHITE));
				barriers.add(new AnchoredBarrier_StraightLine(WORLD_WIDTH, WORLD_HEIGHT, 0, WORLD_HEIGHT, Color.WHITE));
				barriers.add(new AnchoredBarrier_StraightLine(0, WORLD_HEIGHT, 0, 0, Color.WHITE));
				barriers.add(new AnchoredBarrier_Curve(WORLD_WIDTH/2, WORLD_HEIGHT-WORLD_WIDTH/2, WORLD_WIDTH/2, 0.0, 200.0,true, Color.WHITE));
				barriers.add(new AnchoredBarrier_Curve(WORLD_WIDTH/2, WORLD_HEIGHT*3/4, WORLD_WIDTH/15, -0.0, 360.0,false, Color.WHITE));
				barriers.add(new AnchoredBarrier_Curve(WORLD_WIDTH*1/3, WORLD_HEIGHT*1/2, WORLD_WIDTH/15, -0.0, 360.0,false, Color.WHITE));
				barriers.add(new AnchoredBarrier_Curve(WORLD_WIDTH*2/3, WORLD_HEIGHT*1/2, WORLD_WIDTH/15, -0.0, 360.0,false, Color.WHITE));
				break;
			}
			case SNOOKER_TABLE: {
				double snookerTableHeight=WORLD_HEIGHT;
				double pocketSize=0.4;
				double cushionDepth=0.3;
				double cushionLength = snookerTableHeight/2-pocketSize-cushionDepth;
				double snookerTableWidth=cushionLength+cushionDepth*2+pocketSize*2;

				createCushion(barriers, snookerTableWidth-cushionDepth/2, snookerTableHeight*0.25,0, cushionLength, cushionDepth);

                pockets.add(new AnchoredBarrier_Curve(snookerTableWidth-cushionDepth/2, snookerTableHeight*0.25 + cushionLength / 2 + 0.35, 0.25, 0, 360, true, Color.WHITE));
                pockets.add(new AnchoredBarrier_Curve(snookerTableWidth-cushionDepth/2, snookerTableHeight*0.75 + cushionLength / 2 + 0.35, 0.25, 0, 360, true, Color.WHITE));
                pockets.add(new AnchoredBarrier_Curve(snookerTableWidth-cushionDepth/2, snookerTableHeight*(-0.25) + cushionLength / 2 + 0.35, 0.25, 0, 360, true, Color.WHITE));

                pockets.add(new AnchoredBarrier_Curve(cushionDepth/2, snookerTableHeight*0.25 + cushionLength / 2 + 0.35, 0.25, 0, 360, true, Color.WHITE));
                pockets.add(new AnchoredBarrier_Curve(cushionDepth/2, snookerTableHeight*0.75 + cushionLength / 2 + 0.35, 0.25, 0, 360, true, Color.WHITE));
                pockets.add(new AnchoredBarrier_Curve(cushionDepth/2, snookerTableHeight*(-0.25) + cushionLength / 2 + 0.35, 0.25, 0, 360, true, Color.WHITE));

                createCushion(barriers, snookerTableWidth-cushionDepth/2, snookerTableHeight*0.75,0, cushionLength, cushionDepth);
				createCushion(barriers, snookerTableWidth/2, snookerTableHeight-cushionDepth/2, Math.PI/2, cushionLength, cushionDepth);
				createCushion(barriers, cushionDepth/2, snookerTableHeight*0.25,Math.PI, cushionLength, cushionDepth);
				createCushion(barriers, cushionDepth/2, snookerTableHeight*0.75,Math.PI, cushionLength, cushionDepth);
				createCushion(barriers, snookerTableWidth/2, cushionDepth/2, Math.PI*3/2, cushionLength, cushionDepth);

				break;
			}
		}
			
			
	}
	private void createCushion(List<AnchoredBarrier> barriers, double centrex, double centrey, double orientation, double cushionLength, double cushionDepth) {
		// on entry, we require centrex,centrey to be the centre of the rectangle that contains the cushion.
		Color col=Color.WHITE;
		Vect2D p1=new Vect2D(cushionDepth/2, -cushionLength/2-cushionDepth/2);
		Vect2D p2=new Vect2D(-cushionDepth/2, -cushionLength/2);
		Vect2D p3=new Vect2D(-cushionDepth/2, +cushionLength/2);
		Vect2D p4=new Vect2D(cushionDepth/2, cushionLength/2+cushionDepth/2);
		p1=p1.rotate(orientation);
		p2=p2.rotate(orientation);
		p3=p3.rotate(orientation);
		p4=p4.rotate(orientation);
		// we are being careful here to list edges in an anticlockwise manner, so that normals point inwards!
		barriers.add(new AnchoredBarrier_StraightLine(centrex+p1.x, centrey+p1.y, centrex+p2.x, centrey+p2.y, col));
        barriers.add(new AnchoredBarrier_Point(centrex+p2.x, centrey+p2.y, 0.0)); // angle bug
		barriers.add(new AnchoredBarrier_StraightLine(centrex+p2.x, centrey+p2.y, centrex+p3.x, centrey+p3.y, col));
        barriers.add(new AnchoredBarrier_Point(centrex+p3.x, centrey+p3.y, 0.0)); // angle bug
		barriers.add(new AnchoredBarrier_StraightLine(centrex+p3.x, centrey+p3.y, centrex+p4.x, centrey+p4.y, col));
		// oops this will have concave corners so will need to fix that some time! 
	}
	
	public static void main(String[] args) throws Exception {
		final BasicPhysicsEngine game = new BasicPhysicsEngine();
		final BasicView view = new BasicView(game);
		JEasyFrame frame = new JEasyFrame(view, "Basic Physics Engine");
        //		view.addMouseMotionListener(new BasicMouseListener());
        CustomMouseListener mouseListener = new CustomMouseListener(game);
        view.addMouseListener(mouseListener);
        view.addMouseMotionListener(mouseListener);
		game.startThread(view);
	}
	private void startThread(final BasicView view) throws InterruptedException {
		final BasicPhysicsEngine game=this;
		while (true) {
			for (int i=0;i<NUM_EULER_UPDATES_PER_SCREEN_REFRESH;i++) {
				game.update();
			}
			view.repaint();
			Toolkit.getDefaultToolkit().sync();
			
			try {
				Thread.sleep(DELAY);
			} catch (InterruptedException e) {
			}
		}
	}

	public void update() {
		for (BasicParticle p : particles) {
			p.update(GRAVITY, DELTA_T); // tell each particle to move
            p.setVel(p.getVel().mult(0.999));
		}

        ArrayList<Integer> particlesToRemove = new ArrayList<>();

        for (int i=0; i<this.particles.size(); ++i) {
            BasicParticle particle = this.particles.get(i);

            for (AnchoredBarrier b : this.pockets) {
                if (b.isCircleCollidingBarrier(particle.getPos(), particle.getRadius())) {
                	if (particle.col.equals(Color.WHITE)) {

                        // Reset cue ball position if potted

                        particle.setPos(this.cueStartPos);

                    } else {

                        // Update the score based on ball color

                        this.score += getScoreForBallColor(particle.col);

                        this.particles.remove(i);

                        i--; // Decrement index after removal to avoid skipping elements}
                }
            }
        }

        for (int j : particlesToRemove) {
            this.particles.remove(j);
            this.score += 1;
        }

		for (BasicParticle particle1 : particles) {
			for (AnchoredBarrier b : barriers) {
				if (b.isCircleCollidingBarrier(particle1.getPos(), particle1.getRadius())) {
					
					Vect2D bouncedVel=b.calculateVelocityAfterACollision(particle1.getPos(), particle1.getVel(),0.95);
					particle1.setVel(bouncedVel);
				}
			}
		}
		double e=0.97; // coefficient of restitution for all particle pairs
		for (int n=0;n<particles.size();n++) {
			for (int m=0;m<n;m++) {// avoids double check by requiring m<n
				BasicParticle p1 = particles.get(n);
				BasicParticle p2 = particles.get(m);
				if (p1.collidesWith(p2)) {
					BasicParticle.implementElasticCollision(p1, p2, e);
				}
			}
		}
	}
}
	public int getScoreForBallColor(java.awt.Color color) {
		if (color.equals(Color.RED)) return 1;

        else if (color.equals(Color.YELLOW)) return 2;

        else if (color.equals(Color.GREEN)) return 3;

        else if (color.equals(Color.CYAN)) return 4; 

        else if (color.equals(Color.BLUE)) return 5;

        else if (color.equals(Color.PINK)) return 6;

        else if (color.equals(Color.GRAY)) return 7;

        else return 0; // White or any other color
	}
}


