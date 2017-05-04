package com.brackeen.javagamebook.tilegame;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import javax.sound.midi.Sequence;
import javax.sound.sampled.AudioFormat;
import com.brackeen.javagamebook.graphics.*;
import com.brackeen.javagamebook.sound.*;
import com.brackeen.javagamebook.input.*;
import com.brackeen.javagamebook.test.GameCore;
import com.brackeen.javagamebook.tilegame.sprites.*;

/**
    GameManager manages all parts of the game.
 */
public class GameManager extends GameCore {

	public static void main(String[] args) {
		new GameManager().run();
	}

	public void init() {
		super.init();

		// set up input manager
		initInput();
		//Load menu

		// start resource manager
		resourceManager = new ResourceManager(
				screen.getFullScreenWindow().getGraphicsConfiguration());

		// load resources
		renderer = new TileMapRenderer();

		// load first map
		map = resourceManager.loadFirstMap();
		blankMap = resourceManager.loadBlankMap();

		// load sounds
		soundManager = new SoundManager(PLAYBACK_FORMAT);
		zapSound = soundManager.getSound("sounds/zap.wav");
		dieSound = soundManager.getSound("sounds/die.wav");

		// start music
		midiPlayer = new MidiPlayer();
		Sequence sequence =
				midiPlayer.getSequence("sounds/title.mid");
		midiPlayer.play(sequence, true);

	}


	/**
        Closes any resources used by the GameManager.
	 */
	public void stop() {
		super.stop();
		midiPlayer.close();
		soundManager.close();
	}

	private void initInput() {
		moveLeft = new GameAction("moveLeft");
		moveRight = new GameAction("moveRight");
		jump = new GameAction("jump",
				GameAction.DETECT_INITAL_PRESS_ONLY);
		exit = new GameAction("exit",
				GameAction.DETECT_INITAL_PRESS_ONLY);
		duck = new GameAction("duck",GameAction.NORMAL);
		passGoal = new GameAction("passGoal", GameAction.NORMAL);
		reset = new GameAction("reset", GameAction.DETECT_INITAL_PRESS_ONLY);
		pause = new GameAction("pause", GameAction.DETECT_INITAL_PRESS_ONLY);
		stopMusic = new GameAction("stopMusic",GameAction.DETECT_INITAL_PRESS_ONLY);
		help = new GameAction("help",GameAction.DETECT_INITAL_PRESS_ONLY);
		inputManager = new InputManager(
				screen.getFullScreenWindow());
		inputManager.setCursor(InputManager.INVISIBLE_CURSOR);
		inputManager.mapToKey(moveLeft, KeyEvent.VK_LEFT);
		inputManager.mapToKey(moveRight, KeyEvent.VK_RIGHT);
		inputManager.mapToKey(jump, KeyEvent.VK_SPACE);
		inputManager.mapToKey(exit, KeyEvent.VK_ESCAPE);
		inputManager.mapToKey(duck, KeyEvent.VK_DOWN);
		inputManager.mapToKey(passGoal, KeyEvent.VK_UP);
		inputManager.mapToKey(reset, KeyEvent.VK_R);
		inputManager.mapToKey(pause, KeyEvent.VK_P);
		inputManager.mapToKey(stopMusic, KeyEvent.VK_M);
		inputManager.mapToKey(help, KeyEvent.VK_H);
	}

	private void checkInput(long elapsedTime) {
		if(stopMusic.isPressed()) {
			musicPaused=!musicPaused;
			midiPlayer.setPaused(musicPaused);
		}

		if (state == STATE.MENU) {
			if (exit.isPressed()) {
				stop();
			}
			if (jump.isPressed()) {
				if(firstStart){
					renderer.setDrawIcon("0");
				}
				firstStart=false;
				state = STATE.GAME;
				Sequence sequence =
						midiPlayer.getSequence("sounds/stage" + resourceManager.getWorld() + ".mid");
				midiPlayer.play(sequence, true); 
			}
			if (help.isPressed()) {
				state = STATE.INSTRUCTION;
			}

		}

		else if (state == STATE.INSTRUCTION) {
			if (exit.isPressed() || jump.isPressed() || help.isPressed()) {
				state = STATE.MENU;
			}
		}

		else if (state == STATE.GAME) {

			if (exit.isPressed()) {
				state = STATE.MENU;
				Sequence sequence =
						midiPlayer.getSequence("sounds/title.mid");
				midiPlayer.play(sequence, true);
			}
			if (pause.isPressed()) {
				paused = !paused;
				//inputManager.resetAllGameActions();
				//pauseMenu.setVisible(paused);
			}
			if (!paused) {
				Player player = (Player)map.getPlayer();
				if (player.isAlive()) {
					float velocityX = 0;
					if (moveLeft.isPressed()) {
						velocityX-=player.getMaxSpeed();
					}
					if (moveRight.isPressed()) {
						velocityX+=player.getMaxSpeed();
					}
					if (jump.isPressed()) {
						player.jump(false);
					}
					if (duck.isPressed()) {
						player.duck(true);
					}
					else {
						player.duck(false);
					}
					if (reset.isPressed()) {
						player.setState(Player.STATE_DYING);
					}
					player.setVelocityX(velocityX);
				}
			}
		}
	}

	/**
    Updates Animation, position, and velocity of all Sprites
    in the current map.
	 */
	public void update(long elapsedTime) {
		Player player = (Player)map.getPlayer();
		if (player.getState() == Player.STATE_DEAD) {
			map = resourceManager.reloadMap(gotPowerUp);
			return;
		}
		// get keyboard/mouse input
		checkInput(elapsedTime);
		if (state == STATE.GAME && !paused) {
			updateWorld(player);
			updatePlayer(player, elapsedTime);
			player.update(elapsedTime);
			// update other sprites
			Iterator<Sprite> i = map.getSprites();
			while (i.hasNext()) {
				Sprite sprite = (Sprite)i.next();
				sprite.update(elapsedTime);
			}
		}
	}

	public void draw(Graphics2D g) {
		if (state == STATE.GAME) {
			renderer.setBackground(
					resourceManager.loadImage("bg" + resourceManager.getWorld() + ".png"));
			renderer.draw(g, map,
					screen.getWidth(), screen.getHeight(), true);
		}
		else if (state == STATE.MENU) {
			Image img = resourceManager.loadImage("startScreen.png");
			renderer.setBackground(img);
			renderer.draw(g, blankMap,
					screen.getWidth(), screen.getHeight(), false);
		}
		else if (state == STATE.INSTRUCTION) {
			Image img = resourceManager.loadImage("instructions.png");
			renderer.setBackground(img);
			renderer.draw(g, blankMap,
					screen.getWidth(), screen.getHeight(), false);
		}
	}

	/**
        Gets the current map.
	 */
	public TileMap getMap() {
		return map;
	}

	/**
        Gets the tile that a Sprites collides with. Only the
        Sprite's X or Y should be changed, not both. Returns null
        if no collision is detected.
	 */
	public Point getTileCollision(Sprite sprite, float newX, float newY) {
		float fromX = Math.min(sprite.getX(), newX);
		float fromY = Math.min(sprite.getY(), newY);
		float toX = Math.max(sprite.getX(), newX);
		float toY = Math.max(sprite.getY(), newY);
		// get the tile locations
		int fromTileX = TileMapRenderer.pixelsToTiles(fromX);
		int fromTileY = TileMapRenderer.pixelsToTiles(fromY);
		int toTileX = TileMapRenderer.pixelsToTiles(
				toX + sprite.getWidth() - 1);
		int toTileY = TileMapRenderer.pixelsToTiles(
				toY + sprite.getHeight() - 1);
		// check each tile for a collision
		for (int x=fromTileX; x<=toTileX; x++) {
			for (int y=fromTileY; y<=toTileY; y++) {
				if (x < 0 || x >= map.getWidth() ||
						map.getTile(x, y) != null)
				{
					// collision found, return the tile
					pointCache.setLocation(x, y);
					return pointCache;
				}
			}
		}
		// no collision found
		return null;
	}

	/**
        Checks if two Sprites collide with one another. Returns
        false if the two Sprites are the same. Returns false if
        one of the Sprites is a Creature that is not alive.
	 */
	public boolean isCollision(Sprite s1, Sprite s2) {
		// if the Sprites are the same, return false
		if (s1 == s2) {
			return false;
		}
		// get the pixel location of the Sprites
		int s1x = Math.round(s1.getX());
		int s1y = Math.round(s1.getY());
		int s2x = Math.round(s2.getX());
		int s2y = Math.round(s2.getY());
		// check if the two sprites' boundaries intersect
		return (s1x < s2x + s2.getWidth() &&
				s2x < s1x + s1.getWidth() &&
				s1y < s2y + s2.getHeight() &&
				s2y < s1y + s1.getHeight());
	}

	/**
        Gets the Sprite that collides with the specified Sprite,
        or null if no Sprite collides with the specified Sprite.
	 */
	public Sprite getSpriteCollision(Sprite sprite) {
		// run through the list of Sprites
		Iterator<Sprite> i = (Iterator<Sprite>)map.getSprites();
		while (i.hasNext()) {
			Sprite otherSprite = (Sprite)i.next();
			if (isCollision(sprite, otherSprite)) {
				// collision found, return the Sprite
				return otherSprite;
			}
		}
		// no collision found
		return null;
	}

	private void updateWorld(Player player) {
		int currentWorld = resourceManager.getWorld();
		if (currentWorld == 2) {
			player.setCanWallJump(true);
		}
		if (currentWorld == 3) {
			player.setCanWallJump(true);
			player.setCanDoubleJump(true);
		}
		if (currentWorld != world) {
			gotPowerUp = false;
			world = currentWorld;
			//Change music
			Sequence sequence =
					midiPlayer.getSequence("sounds/stage" + currentWorld + ".mid");
			midiPlayer.play(sequence, true);   
		}
	}

	/**
        Updates the player, and checks collisions.
	 */
	private void updatePlayer(Player player, long elapsedTime) {
		// apply gravity
		player.setVelocityY(player.getVelocityY() + GRAVITY * elapsedTime);
		// change x
		float dx = player.getVelocityX();
		float oldX = player.getX();
		float newX = oldX + dx * elapsedTime;
		Point tile =
				getTileCollision(player, newX, player.getY());
		if (tile == null) {
			player.setX(newX);
		}
		else {
			// line up with the tile boundary
			if (dx > 0) {
				player.setX(
						TileMapRenderer.tilesToPixels(tile.x) -
						player.getWidth());
			}
			else if (dx < 0) {
				player.setX(
						TileMapRenderer.tilesToPixels(tile.x + 1));
			}
			player.collideHorizontal();  
		}
		if (player instanceof Player) {
			checkPlayerCollision((Player)player);
		}
		// change y
		float dy = player.getVelocityY();
		float oldY = player.getY();
		float newY = oldY + dy * elapsedTime;
		tile = getTileCollision(player, player.getX(), newY);
		if (tile == null) {
			player.setY(newY);
		}
		else {
			// line up with the tile boundary
			if (dy > 0) {
				player.setY(
						TileMapRenderer.tilesToPixels(tile.y) -
						player.getHeight());
			}
			else if (dy < 0) {
				player.setY(
						TileMapRenderer.tilesToPixels(tile.y + 1));
			}
			player.collideVertical();
		}
		if (player instanceof Player) {
			checkPlayerCollision((Player)player);
		}
	}

	/**
        Checks for Player collision with other Sprites
	 */
	public void checkPlayerCollision(Player player)
	{
		if (!player.isAlive()) {
			return;
		}
		// check for player collision with other sprites
		Sprite collisionSprite = getSpriteCollision(player);
		if (collisionSprite instanceof Spike) {
			player.setState(Player.STATE_DYING);
			soundManager.play(dieSound);
		}
		if (collisionSprite instanceof Saw) {
			player.setState(Player.STATE_DYING);
			soundManager.play(dieSound);
		}
		if (collisionSprite instanceof Pole) {
			//do nothing
		}
		if (collisionSprite instanceof PowerUp) {
			acquirePowerUp(player,(PowerUp)collisionSprite);
		}
	}

	/**
        Gives the player the specified power up and removes it
        from the map.
	 */
	public void acquirePowerUp(Player player, PowerUp powerUp) {
		if (powerUp instanceof PowerUp.Lightning) {
			map.removeSprite(powerUp);
			gotPowerUp = true;
			soundManager.play(zapSound);
		}
		else if (powerUp instanceof PowerUp.Goal) {
			if(passGoal.isPressed()){
				map.removeSprite(powerUp);
				
                if(resourceManager.getMap()==5 && (gotPowerUp||resourceManager.getWorld()==3)){
                    renderer.setDrawIcon(Integer.toString(resourceManager.getWorld()));
                    
				}
				 
				map = resourceManager.loadNextMap(gotPowerUp);
			}
		}
	}

	private static final AudioFormat PLAYBACK_FORMAT = new AudioFormat(44100, 16, 1, true, false);
	public static final float GRAVITY = 0.002f;
	private Point pointCache = new Point();
	private TileMap map;
	private MidiPlayer midiPlayer;
	private SoundManager soundManager;
	private ResourceManager resourceManager;
	private int world = 1;
	private boolean paused;
	private boolean musicPaused=false;
	private boolean gotPowerUp = false;
	private boolean firstStart = true;
	private Sound zapSound;
	private Sound dieSound;
	private InputManager inputManager;
	private TileMapRenderer renderer;
	private GameAction moveLeft;
	private GameAction moveRight;
	private GameAction jump;
	private GameAction exit;
	private GameAction duck;
	private GameAction passGoal;
	private GameAction reset;
	private GameAction pause;
	private GameAction stopMusic;
	private GameAction help;
	private TileMap blankMap;
	private STATE state = STATE.MENU;
	private enum STATE {
		GAME,
		MENU,
		INSTRUCTION,
		PAUSED
	};

}
