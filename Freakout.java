/*
 * File: Freakout.java
 * -------------------
 * Name: Cary Turner
 * 
 * FREAKOUT
 * 
 * Description:
 * This file is a particularly groovy reimagination of the classic computer game Breakout. This program implements all
 * of the original features of the game, but adds several special effects.
 */

import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import java.io.*;


public class Freakout extends GraphicsProgram {

/** Width and height of application window in pixels.  IMPORTANT NOTE:
  * ON SOME PLATFORMS THESE CONSTANTS MAY **NOT** ACTUALLY BE THE DIMENSIONS
  * OF THE GRAPHICS CANVAS.  Use getWidth() and getHeight() to get the 
  * dimensions of the graphics canvas. */
	public static final int APPLICATION_WIDTH = 400;
	public static final int APPLICATION_HEIGHT = 600;

/** Dimensions of game board.  IMPORTANT NOTE: ON SOME PLATFORMS THESE 
  * CONSTANTS MAY **NOT** ACTUALLY BE THE DIMENSIONS OF THE GRAPHICS
  * CANVAS.  Use getWidth() and getHeight() to get the dimensions of
  * the graphics canvas. */
	private static final int WIDTH = APPLICATION_WIDTH;
	private static final int HEIGHT = APPLICATION_HEIGHT;

/** Dimensions of the paddle */
	private static final int PADDLE_WIDTH = 60;
	private static final int PADDLE_HEIGHT = 10;

/** Offset of the paddle up from the bottom */
	private static final int PADDLE_Y_OFFSET = 30;

/** Number of bricks per row */
	private static final int NBRICKS_PER_ROW = 10;

/** Number of rows of bricks */
	private static final int NBRICK_ROWS = 6;

/** Separation between bricks */
	private static final int BRICK_SEP = 4;

/** Width of a brick */
	private static final int BRICK_WIDTH =
	  (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

/** Height of a brick */
	private static final int BRICK_HEIGHT = 8;

/** Radius of the ball in pixels */
	private static final int BALL_RADIUS = 13;

/** Offset of the top brick row from the top */
	private static final int BRICK_Y_OFFSET = 70;

/** Number of turns */
	private static final int NTURNS = 8;
	
/* Pause time between ball moves */
	private static final int DELAY = 12;
	
/* Given Y velocity of the ball */
	private static final double Y_VELOCITY = 5.0;

/* Method: run() */
/** Runs the Breakout program. */
	public void run() {
		setUpGame();
		addMouseListeners();
		waitForClick();
		playMusic(new File("LeFreak.wav"));
		runGame();
		displayResults();
	}
	
	/*
	 * Sets up the game to get ready for play. places the bricks, paddle, and ball.
	 */
	private void setUpGame() {
		setBackground(Color.BLACK);
		placeBricks();
		placePaddle();
		placeBall();
	}
	
	/*
	 * This method runs the game until the game is over (ie no bricks left or no turns left).
	 */
	private void runGame() {
		int count = 0;
		while(!gameOver()) {
			moveBall();
			checkForCollision();
			freakout();
			pause(DELAY);
			count++;
			if(count == 80) {
				setBackground(rgen.nextColor());
				count = 0;
			}
		}
	}
	
	/*
	 * Uses a for loop based on the constant NBRICK_ROWS to create the correct amount of rows by calling
	 * the buildRow method.
	 */
	private void placeBricks() {
		for (int i = 0; i < NBRICK_ROWS; i++) {
			buildRow(i, getRowColor(i));
		}
	}

	/*
	 * Builds a single row of bricks using a for loop based on the constant NBRICKS_PER_ROW. 
	 */
	private void buildRow(int i, Color color) {
		for (int j = 0; j < NBRICKS_PER_ROW; j++) {
			double x = (getWidth() - BRICK_WIDTH * NBRICKS_PER_ROW - BRICK_SEP * (NBRICKS_PER_ROW - 1)) / 2.0 
					+ (BRICK_WIDTH + BRICK_SEP) * j;
			double y = BRICK_Y_OFFSET + (BRICK_HEIGHT + BRICK_SEP) * i;
			GRect brick = new GRect(x, y, BRICK_WIDTH, BRICK_HEIGHT);
			brick.setColor(color);
			brick.setFilled(true);
			add(brick);
		}
	}
	
	/*
	 * Gets the correct color of brick based on the row number. The colors will repeat their pattern for
	 * any number of rows over 10.
	 */
	private Color getRowColor(int i) {
		Color color = Color.RED;
		int rowNum = i % 10;
		switch (rowNum) {
		case 0: case 1:
			color = Color.RED;
			break;
		case 2: case 3:
			color = Color.ORANGE;
			break;
		case 4: case 5:
			color = Color.YELLOW;
			break;
		case 6: case 7:
			color = Color.GREEN;
			break;
		case 8: case 9:
			color = Color.BLUE;
			break;
		}
		return color;
	}
	
	/*
	 * Creates the paddle and places it at the bottom-center of the screen, using the constant PADDLE_Y_OFFSET.
	 */
	private void placePaddle() {
		double x = (getWidth() - PADDLE_WIDTH) / 2.0;
		double y = getHeight() - PADDLE_Y_OFFSET - PADDLE_HEIGHT;
		paddle = new GRect(x, y, PADDLE_WIDTH, PADDLE_HEIGHT);
		paddle.setFilled(true);
		paddle.setColor(Color.WHITE);
		add(paddle);
	}
	
	/*
	 * Creates a "mouse tracker" that causes the paddle to follow the user's mouse movements
	 * 	in the horizontal direction.
	 */
	public void mouseMoved(MouseEvent e) {
		double x = e.getX();
		if(x >= getWidth() - PADDLE_WIDTH / 2.0) {
			x = getWidth() - PADDLE_WIDTH / 2.0;
		}
		if(x <= PADDLE_WIDTH / 2.0) {
			x = PADDLE_WIDTH / 2.0;
		}
		paddle.setLocation(x - PADDLE_WIDTH / 2.0, getHeight() - PADDLE_Y_OFFSET - PADDLE_HEIGHT);
	}
	
	/*
	 * Creates and places a ball of radius BALL_RADIUS, and places it in the center of the screen, giving it a 
	 * set Y-velocity using the constant Y_VELOCITY and a "random" X-velocity using the random generator.
	 */
	private void placeBall() {
		double diam = BALL_RADIUS * 2;
		ball = new GFace(diam, diam);
		ball.setLocation((getWidth() - diam) / 2.0, (getHeight() - diam) / 2.0);
		add(ball);
		vy = Y_VELOCITY;
		vx = rgen.nextDouble(2.5, 5.0);
		if(rgen.nextBoolean(.5)) vx = -vx;
	}
	
	/*
	 * Moves the ball by vx in the horizontal direction and vy in the vertical direction. If the ball
	 * hits the top or either side wall, its velocity will be reversed in the direction from which it came.
	 */
	private void moveBall() {
		ball.move(vx, vy);
		if(ball.getX() <= 0) {
			vx = Math.abs(vx);
		}
		if (ball.getX() >= getWidth() - BALL_RADIUS * 2) {
			vx = -1 * Math.abs(vx);
		}
		if(ball.getY() <= 0) {
			vy = -vy;
		}
	}
	
	/*
	 * Checks for a collision with the bottom wall, paddle or bricks.
	 */
	private void checkForCollision() {
		collideWithBottom();
		collideWithBrick();
		collideWithPaddle();
	}
	
	/*
	 * Activated if the ball hits the bottom of the screen. Remaining number of turns decremented and 
	 * new ball placed in the middle of the screen with new "random" x-velocity.
	 */
	private void collideWithBottom() {
		if (ball.getY() > getHeight() - ball.getHeight()) {
			remainingTurns --;
			remove(ball);
			placeBall();
			if (remainingTurns != 0) {
				waitForClick();
			}
		}
	}
	
	/*
	 * Activated if the ball collides with a brick. If a brick is hit by the ball, the brick will be removed
	 * and the ball's y-velocity will be reversed (ie it will "bounce" in the opposite direction). The program
	 * plays a laser noise each time a brick is hit by the ball.
	 */
	private void collideWithBrick() {
		GObject collObject = getCollidingObject();
		AudioClip bounceClip = MediaTools.loadAudioClip("Laser.WAV");
		if(collObject != paddle && collObject != null) {
			bounceClip.play();
			remove(collObject);
			vy = -vy;
			brickCount--;
		}
	}
	
	/*
	 * Activated if the ball collides with the paddle. If the ball hits the top of the paddle,
	 * the ball will "bounce" back in the same direction (if it collides with the same side of the
	 * paddle as the ball is coming from), or will bounce in the opposite direction (if it collides
	 * with the side opposite the ball's incoming direction. If it hits the bottom edge of the paddle,
	 * the ball will continue downward and to the bottom of the screen.
	 */
	private void collideWithPaddle() {
		GObject collObject = getCollidingObject();
		if(collObject == paddle) {
			if((vx < 0 && ball.getX() > paddle.getX() + PADDLE_WIDTH / 2.0) || 
					vx > 0 && ball.getX() + 2 * BALL_RADIUS < paddle.getX() + PADDLE_WIDTH / 2.0 ) {
				vx = -vx;
			}
			if(ball.getY() > getHeight() - PADDLE_Y_OFFSET - paddle.getHeight() / 2.0 - BALL_RADIUS * 2.0) {
				vy = Math.abs(vy);
			}
			else {
				vy = -1 * Math.abs(vy);
			}
		}
	}
	
	/*
	 * Checks each corner of the ball for a collision and returns the colliding object at the point which
	 * is in collision.
	 */
	private GObject getCollidingObject() {
		GObject collider = getElementAt(ball.getX(), ball.getY());
		if(collider != null) return collider;
		GObject collider2 = getElementAt(ball.getX() + BALL_RADIUS * 2, ball.getY());
		if(collider2 != null) return collider2;
		GObject collider3 = getElementAt(ball.getX(), ball.getY() + BALL_RADIUS * 2);
		if (collider3 != null) return collider3;
		GObject collider4 = getElementAt(ball.getX() + BALL_RADIUS * 2, ball.getY() + BALL_RADIUS * 2);
		if (collider4 != null) return collider4;
		return null;
		
	}
	
	/*
	 * The program enters "Freakout" mode, causing flashing lights, a "FREAKOUT" banner across the screen,
	 * and a very satisfying "oooooohhhh yeahhhh" sound effect. Freakout mode is activated by hitting the
	 * top of the screen with the ball, and lasts as long as the ball stays above the level of the lowest bricks.
	 */
	private void freakout() {
		GLabel breakoutLabel = new GLabel("FREAKOUT!");
		breakoutLabel.setFont("Default-bold-52");
		breakoutLabel.setColor(Color.WHITE);
		if(ball.getY() <= 0) {
			if(brickCount >= 8) {
				AudioClip ohYeah = MediaTools.loadAudioClip("OhYeah.wav");
				ohYeah.play();
			}
			while(ball.getY() < BRICK_Y_OFFSET + NBRICK_ROWS * BRICK_HEIGHT) {
				setBackground(rgen.nextColor());
				moveBall();
				checkForCollision();
				add(breakoutLabel, (getWidth() - breakoutLabel.getWidth()) / 2.0, (getHeight() 
						+ breakoutLabel.getAscent()) / 2.0);
				pause(DELAY);
				remove(breakoutLabel);
			}
		}
	}
	
	/*
	 * Returns whether or not the game is over. The game is over when either the player runs out of turns
	 * or all the bricks are gone.
	 */
	private boolean gameOver() {
		return remainingTurns == 0 || brickCount == 0;
	}
	
	/*
	 * Plays the background music, setting a real "freaky" vibe.
	 */
	private void playMusic(File filepath) {
		try {
		    AudioInputStream song = AudioSystem.getAudioInputStream(filepath);
			AudioFormat format = song.getFormat();
		    DataLine.Info info = new DataLine.Info(Clip.class, format);
		    Clip clip = (Clip) AudioSystem.getLine(info);
		    clip.open(song);
		    clip.start();
		}
		catch (Exception e) {
		}
	}
	
	/*
	 * Displays the results of the game to the user. If the user wins, they will be immediately told to
	 * "get back to work" by Dwight Schrute, Assistant to the Regional Manager. If the user loses, they will
	 * get two big ol' thumbs down.
	 */
	private void displayResults() {
		if(brickCount == 0) {
			GImage winImage = new GImage("win.jpg");
			double imageScale = getWidth() / 477.0;
			winImage.scale(imageScale);
			add(winImage, (getWidth() - winImage.getWidth()) / 2.0, (getHeight() - winImage.getHeight()) / 2.0);
		} else {
			GImage loseImage = new GImage("lose.jpg");
			double imageScale = getWidth() / 386.0;
			loseImage.scale(imageScale);
			add(loseImage, (getWidth() - loseImage.getWidth()) / 2.0, (getHeight() - loseImage.getHeight()) / 2.0);
		}
	}

	/* private instance variables */
	private GFace ball;
	private GRect paddle;
	private double vx;
	private double vy;
	private int brickCount = NBRICKS_PER_ROW * NBRICK_ROWS;
	private int remainingTurns = NTURNS;
	private RandomGenerator rgen = RandomGenerator.getInstance();
}

