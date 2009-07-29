package com.mortalpowers.games.ontarget;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

public class Main extends BasicGame {
	static final private int gameHeight = 600;
	static final private int gameWidth = 800;
	static private ArrayList<Renderable> screenImages = new ArrayList<Renderable>();
	static boolean up, down, right, left;
	static Connection db;
	public long timer, time;
	public boolean gameOn = true;
	private int multiplier = 20;
	private int levelCount = 1;
	private float points = 0;
	public boolean quit = false;
	private String playerName = "unknown";

	class Target extends Renderable {
		public int x, y;

		public Target() {
			x = gameWidth / 2 - 25;
			y = gameHeight / 2 - 25;
		}

		public void render(Graphics g) {
			g.drawRect(x, y, 50, 50);
		}
	}

	class Message extends Renderable {
		public String msg;
		public int x, y;

		public Message(String mes, int x, int y) {
			msg = mes;
			this.x = x;
			this.y = y;
		}

		public void render(Graphics g) {
			g.drawString(msg, x, y);
		}

	}

	class InputBox extends Renderable {
		public String value;

		public void render(Graphics g) {
			g.drawRect(300, 400, 150, 30);
			g.drawString("Name", 250, 405);
			g.drawString(playerName, 305, 405);
		}
	}

	abstract class Renderable {
		public abstract void render(Graphics g);
	}

	public Main() {
		super("OnTarget");
		createDB();

		// TODO Auto-generated constructor stub

	}

	private void addScore(String name, int level, float score) {
		try {
			File f = new File("highscores.db");
			if (f.exists()) {
				Class.forName("org.sqlite.JDBC");

				db = DriverManager.getConnection("jdbc:sqlite:highscores.db");
				PreparedStatement prep = db
						.prepareStatement("INSERT INTO scores (name,level,score) VALUES (?, ?, ?);");

				prep.setString(1, playerName);
				prep.setInt(2, levelCount);
				prep.setInt(3, Math.round(points));
				prep.addBatch();

				db.setAutoCommit(false);
				prep.executeBatch();
				db.setAutoCommit(true);
				db.close();
			}
		} catch (Exception e) {

		}
	}

	private String getScoreString() {
		String result = "Top Players:\n";
		try {
			File f = new File("highscores.db");
			if (f.exists()) {
				Class.forName("org.sqlite.JDBC");

				db = DriverManager.getConnection("jdbc:sqlite:highscores.db");
				Statement stat = db.createStatement();
				ResultSet rs = stat
						.executeQuery("SELECT name,level,score FROM scores ORDER BY score DESC LIMIT 8;");
				while (rs.next()) {
					result += rs.getString("name") + " with "
							+ rs.getString("score") + " points\n";
				}
				rs.close();
				db.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Returning " + result);
		return result;
	}

	private void createDB() {
		try {
			File f = new File("highscores.db");
			if (f.exists()) {
				Class.forName("org.sqlite.JDBC");

				db = DriverManager.getConnection("jdbc:sqlite:highscores.db");
				Statement stat = db.createStatement();
				ResultSet rs = stat
						.executeQuery("SELECT name,level,score  FROM scores;");
				while (rs.next()) {
					System.out.println("name = " + rs.getString("name"));
					System.out.println("score = " + rs.getString("score"));
				}
				rs.close();
				db.close();
			} else {

				Class.forName("org.sqlite.JDBC");

				db = DriverManager.getConnection("jdbc:sqlite:highscores.db");
				Statement stat = db.createStatement();
				stat.executeUpdate("drop table if exists scores;");
				stat.executeUpdate("CREATE TABLE scores (name, level, score int);");

				db.close();

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {

			AppGameContainer container = new AppGameContainer(new Main(),
					gameHeight, gameHeight, false);
			container.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Target target;
	private Message score;
	private Message level;

	/**
	 * Run once after graphics inited. Load images, etc. here.
	 */
	@Override
	public void init(GameContainer container) throws SlickException {
		// TODO Auto-generated method stub
		setup();
	}

	public void setup() {
		screenImages.clear();

		target = new Target();
		score = new Message("0 points", 10, 35);
		level = new Message("Level 1", 10, 50);

		screenImages.add(target);
		screenImages.add(score);
		screenImages.add(level);

		gameOn = true;
		multiplier = 20;
		levelCount = 1;
		points = 0;
	}

	@Override
	/**
	 * Run once per frame.
	 */
	public void update(GameContainer container, int delta)
			throws SlickException {
		if (quit) {
			container.exit();
		}
		if (gameOn) {
			timer += delta;
			time += delta;
			points += (float) delta / 10f;
			score.msg = getPointString();
			int adjust = multiplier / 2;
			while (timer > 15) {
				timer -= 15;

				target.x += Math.random() * multiplier - adjust;
				target.y -= Math.random() * multiplier - adjust;
				if (right) {
					target.x += 5;
				}
				if (down) {
					target.y += 5;
				}
				if (up) {
					target.y -= 5;
				}
				if (left) {
					target.x -= 5;
				}
			}
			while (time > 10000) {
				time -= 10000;

				nextLevel();

			}

			if (target.x < -25 || target.x > gameWidth || target.y < -25
					|| target.y > gameHeight) {
				screenImages.remove(target);
				gameOn = false;
				endGame(container);

			}

		}
	}

	private String getPointString() {
		// TODO Auto-generated method stub
		return Math.round(points) + " points";
	}

	private void nextLevel() {
		multiplier *= 1.2;
		levelCount++;
		level.msg = "Level " + levelCount;

	}

	private void endGame(GameContainer c) {
		screenImages.add(new Message("Final Level: " + levelCount,
				gameWidth / 4, gameHeight / 4));
		screenImages.add(new Message("Final Score: " + getPointString(),
				gameWidth / 4, gameHeight / 4 + 20));
		screenImages.add(new InputBox());
		screenImages.remove(score);
		screenImages.remove(level);

		Message scores = new Message("Loading scores.", gameWidth / 4,
				gameHeight / 4 + 60);
		screenImages.add(scores);
		scores.msg = getScoreString();
	}

	@Override
	/** ALso run once per frame.
	 * 
	 */
	public void render(GameContainer container, Graphics g)
			throws SlickException {
		for (int i = 0; i < screenImages.size(); i++) {
			screenImages.get(i).render(g);
		}

	}

	public void keyPressed(int key, char c) {
		// System.out.println("Someone pressed " + key);

		switch (key) {

		case Input.KEY_RIGHT:
			right = true;
			break;
		case Input.KEY_DOWN:
			down = true;
			break;
		case Input.KEY_LEFT:
			left = true;
			break;
		case Input.KEY_UP:
			up = true;
			break;
		case Input.KEY_ESCAPE:
			quit = true;
			break;
		case Input.KEY_BACK:
			if (playerName.length() > 0) {
				playerName = playerName.substring(0, playerName.length() - 1);
			}
			break;
		case Input.KEY_ENTER:
			if (!gameOn) {
				addScore(playerName, levelCount, points);
				setup();
			}
		default:
			if (Input.getKeyName(key).length() == 1) {
				if (playerName.equals("unknown")) {
					playerName = "";
				}
				// System.out.println("Adding char '" + c + "' (" + key
				// + ") to the string.");
				playerName += c;
			}
		}

	}

	public void keyReleased(int key, char c) {
		switch (key) {
		case Input.KEY_RIGHT:
			right = false;
			break;
		case Input.KEY_DOWN:
			down = false;
			break;
		case Input.KEY_LEFT:
			left = false;
			break;
		case Input.KEY_UP:
			up = false;
			break;
		case Input.KEY_SPACE:
			setup();
			break;
		}
	}
}
