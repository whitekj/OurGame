package com.brackeen.javagamebook.tilegame;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Iterator;

import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
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

        // start resource manager
        resourceManager = new ResourceManager(
        screen.getFullScreenWindow().getGraphicsConfiguration());

        // load resources
        renderer = new TileMapRenderer();

        // load first map
        map = resourceManager.loadNextMap();

        // load sounds
        soundManager = new SoundManager(PLAYBACK_FORMAT);
        //prizeSound = soundManager.getSound("sounds/prize.wav");
        //boopSound = soundManager.getSound("sounds/boop2.wav");

        // start music
        midiPlayer = new MidiPlayer();
        Sequence sequence =
            midiPlayer.getSequence("sounds/stage1.mid");
        midiPlayer.play(sequence, true);
        toggleDrumPlayback();
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
    }


    private void checkInput(long elapsedTime) {

        if (exit.isPressed()) {
            stop();
        }
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


    public void draw(Graphics2D g) {
    	renderer.setBackground(
                resourceManager.loadImage("bg" + resourceManager.getWorld() + ".png"));
        renderer.draw(g, map,
            screen.getWidth(), screen.getHeight());
    }


    /**
        Gets the current map.
    */
    public TileMap getMap() {
        return map;
    }


    /**
        Turns on/off drum playback in the midi music (track 1).
    */
    public void toggleDrumPlayback() {
        Sequencer sequencer = midiPlayer.getSequencer();
        if (sequencer != null) {
            sequencer.setTrackMute(DRUM_TRACK,
                !sequencer.getTrackMute(DRUM_TRACK));
        }
    }


    /**
        Gets the tile that a Sprites collides with. Only the
        Sprite's X or Y should be changed, not both. Returns null
        if no collision is detected.
    */
    public Point getTileCollision(Sprite sprite,
        float newX, float newY)
    {
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


    /**
        Updates Animation, position, and velocity of all Sprites
        in the current map.
    */
    public void update(long elapsedTime) {
        Player player = (Player)map.getPlayer();
        if (player.getState() == Player.STATE_DEAD) {
            map = resourceManager.reloadMap();
            return;
        }
        // get keyboard/mouse input
        checkInput(elapsedTime);
        updateWorld(player);
        // update player
        updatePlayer(player, elapsedTime);
        player.update(elapsedTime);
    }
    
    private void updateWorld(Player player) {
    	int currentWorld = resourceManager.getWorld();
    	//Update abilities (must be changed to require power-up)
        if (currentWorld == 2) {
    		player.setCanWallJump(true);
    	}
    	if (currentWorld == 3) {
    		player.setCanWallJump(true);
    		player.setCanDoubleJump(true);
    	}
        if (currentWorld != world) {
        	world = currentWorld;
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
        }
    	if (collisionSprite instanceof Saw) {
    		player.setState(Player.STATE_DYING);
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
            // do something here, like give the player points
            map.removeSprite(powerUp);
            //soundManager.play(prizeSound);
            if (resourceManager.getWorld() == 1) {
            	player.setCanWallJump(true);
            }
            if (resourceManager.getWorld() == 2) {
            	player.setCanDoubleJump(true);
            }
            if (resourceManager.getWorld() == 3) {
            	//Open boss access?
            }
        }
        else if (powerUp instanceof PowerUp.Goal) {
            // advance to next map
            if(passGoal.isPressed()){
                map.removeSprite(powerUp);
                //soundManager.play(prizeSound, new EchoFilter(2000, .7f), false);
                map = resourceManager.loadNextMap();
            }
        }
    }
    
    private static final AudioFormat PLAYBACK_FORMAT = new AudioFormat(44100, 16, 1, true, false);
    private static final int DRUM_TRACK = 1;
    public static final float GRAVITY = 0.002f;
    private Point pointCache = new Point();
    private TileMap map;
    private MidiPlayer midiPlayer;
    private SoundManager soundManager;
    private ResourceManager resourceManager;
    private int world;
    //private Sound prizeSound;
    //private Sound boopSound;
    private InputManager inputManager;
    private TileMapRenderer renderer;
    private GameAction moveLeft;
    private GameAction moveRight;
    private GameAction jump;
    private GameAction exit;
    private GameAction duck;
    private GameAction passGoal;
    private GameAction reset;
    
    
}
