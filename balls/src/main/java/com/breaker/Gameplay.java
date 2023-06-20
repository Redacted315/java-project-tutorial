package com.breaker;

import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.LineEvent.Type;

import javax.swing.JPanel;
import javax.swing.Timer;

public class Gameplay extends JPanel implements KeyListener, ActionListener {
	private boolean play = false;
	private int score = 0;
	
	private int totalBricks = 21;
	
	private Timer timer;
	private int delay = 8;
	
	private int playerX = 310;
	
	private int ballposX = 120;
	private int ballposY = 350;
	private int ballXdir = -2;
	private int ballYdir = -4;

	boolean doSound = true;
	File failSound = new File("src\\main\\java\\com\\breaker\\sfx\\bad-to-the-bone.wav");
	File winSound = new File("src\\main\\java\\com\\breaker\\sfx\\final-fantasy-v-music-victory-fanfare.wav");


	private MapGenerator map;
	
	public Gameplay() {
		map = new MapGenerator(3, 7);
		addKeyListener(this);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		timer = new Timer(delay, this);
		timer.start();
	}
	public void paint(Graphics g) {
		// background
		g.setColor(Color.black);
		g.fillRect(1,1,692,592);
		
		// map
		map.draw((Graphics2D)g);
		
		// borders
		g.setColor(Color.yellow);
		g.fillRect(0,0,3,592);
		g.fillRect(0,0,692,3);
		g.fillRect(681,0,3,592);
		
		// score
		g.setColor(Color.white);
		g.setFont(new Font("sans-serif", Font.BOLD, 25));
		g.drawString(""+score, 590, 30);

		// paddle
		g.setColor(Color.green);
		g.fillRect(playerX, 550, 100, 8);
		
		// ball
		g.setColor(Color.yellow);
		g.fillOval(ballposX, ballposY, 20, 20);
		
		// check win
		if ( totalBricks <= 0 ) {
			play = false;
			ballXdir = 0;
			ballYdir = 0;
			g.setColor(Color.green);
			g.setFont(new Font("sans-serif", Font.BOLD, 35));
			g.drawString("Congrats!", 260, 300);
			
			g.setFont(new Font("sans-serif", Font.BOLD, 25));
			g.drawString("Press Enter to play again", 205, 350);

			if ( doSound ) {
				try {
					playClip(winSound);
					doSound = false;
				} catch (IOException | UnsupportedAudioFileException | LineUnavailableException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}
		}
		
		// check ball in bounds
		if ( ballposY > 570 ) {
			play = false;
			ballXdir = 0;
			ballYdir = 0;
			
			g.setColor(Color.red);
			g.setFont(new Font("sans-serif", Font.BOLD, 35));
			g.drawString("Game Over", 260, 300);
			
			g.setFont(new Font("sans-serif", Font.BOLD, 25));
			g.drawString("Press Enter to Restart", 220, 350);			
			if ( doSound ) {
				try {
					playClip(failSound);
					doSound = false;
				} catch (IOException | UnsupportedAudioFileException | LineUnavailableException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}
		}
		g.dispose();
	}
	private static void playClip(File clipFile) throws IOException, UnsupportedAudioFileException, LineUnavailableException, InterruptedException {
		class AudioListener implements LineListener {
			private boolean done = false;
			@Override public synchronized void update(LineEvent event) {
				Type eventType = event.getType();
				if (eventType == Type.STOP || eventType == Type.CLOSE) {
					done = true;
					notifyAll();
				}
			}
			public synchronized void waitUntilDone() throws InterruptedException {
				while (!done) { wait(); }
			}
			}
			AudioListener listener = new AudioListener();
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(clipFile);
			try {
			Clip clip = AudioSystem.getClip();
			clip.addLineListener(listener);
			clip.open(audioInputStream);
				try {
					clip.start();
					listener.waitUntilDone();
				} finally {
					clip.close();
				}
			} finally {
				audioInputStream.close();
			}
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		timer.start();
		if ( play ) {
			if (new Rectangle(ballposX, ballposY, 20, 20).intersects(new Rectangle(playerX, 550, 100, 8))) {
				ballYdir = -ballYdir;
			}
			
			A: for (int i = 0; i < map.map.length; i++) {
				for (int j = 0; j < map.map[0].length; j++) {
					if (map.map[i][j] > 0 ) {
						int brickX = j* map.brickWidth + 80;
						int brickY = i* map.brickHeight + 50;
						int brickWidth = map.brickWidth;
						int brickHeight = map.brickHeight;
						Rectangle rect = new Rectangle(brickX, brickY, brickWidth, brickHeight);
						Rectangle ballRect = new Rectangle(ballposX, ballposY, 20, 20);
						Rectangle brickRect = rect;
						
						if ( ballRect.intersects(brickRect)) {
							map.setBrickValue(0, i, j);
							totalBricks--;
							score += 5;
							
							if ( ballposX + 19 <= brickRect.x || ballposX + 1 >= brickRect.x + brickRect.width) {
								ballXdir = -ballXdir;
							}else {
								ballYdir = -ballYdir;
							}
							break A;
						}
					}
				}
			}
			ballposX += ballXdir;
			ballposY += ballYdir;
			if ( ballposX < 0 ) {
				ballXdir = -ballXdir;
			}
			if ( ballposY < 0 ) {
				ballYdir = -ballYdir;
			}
			if ( ballposX > 670 ) {
				ballXdir = -ballXdir;
			}
		}
		repaint();
	}

	@Override
	public void keyTyped(KeyEvent e) {}
	@Override
	public void keyReleased(KeyEvent e) {}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			if (playerX >= 600) {
				playerX = 600;
			}else {
				moveRight();
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			if (playerX < 10) {
				playerX = 10;
			}else {
				moveLeft();
			}
		}
		
		if ( e.getKeyCode() == KeyEvent.VK_ENTER) {
			if ( ! play ) {
				play = true;
				doSound = true;
				ballposX = 120;
				ballposY = 350;
				ballXdir = -2;
				ballYdir = -4;
				playerX = 310;
				score = 0;
				totalBricks = 21;
				map = new MapGenerator(3,7);
				repaint();
			}
		}
	}
	public void moveRight() {
		play = true;
		playerX += 20;
	}
	public void moveLeft() {
		play = true;
		playerX -= 20;
	}

}
