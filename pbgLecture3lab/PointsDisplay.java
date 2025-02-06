package pbgLecture3lab;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class PointsDisplay extends Canvas {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;

    public PointsDisplay() {
        setSize(WIDTH, HEIGHT);
    }

    public PointsDisplay(int width, int height) {
        setSize(width, height);
    }

    public void draw(Graphics g, String text) {
        // Get the dimensions of the screen
        int screenWidth = getWidth();
        int screenHeight = getHeight();

        // Set font and color
        g.setFont(new Font("Arial", Font.BOLD, 17));
        g.setColor(Color.WHITE);

        // Calculate the position to draw text at the center of the screen
        int textWidth = g.getFontMetrics().stringWidth(text);
        int x = (screenWidth - textWidth) / 2 + (textWidth * 2);
        int y = screenHeight / 3;

        // Draw text at the center of the screen
        g.drawString(text, x, y);
    }
}
