package com.brackeen.javagamebook.tilegame;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.util.ArrayList;
import javax.swing.ImageIcon;

import com.brackeen.javagamebook.graphics.*;
import com.brackeen.javagamebook.tilegame.sprites.*;


/**
    The ResourceManager class loads and manages tile Images and
    "host" Sprites used in the game. Game Sprites are cloned from
    "host" Sprites.
*/
public class ResourceManager {

    private ArrayList<Image> tiles;
    private int currentMap;
    private int currentWorld;
    private int numMaps; //number of maps per world
    private int numWorlds;
    private GraphicsConfiguration gc;

    // host sprites used for cloning
    private Sprite playerSprite;
    private Sprite lightningSprite;
    private Sprite goalSprite;
    private Sprite spikeSprite;
    private Sprite sawSprite;
    private Sprite spikeBloodySprite;
    private Sprite sawBloodySprite;
    private Sprite spikeInvisibleSprite;

    /**
        Creates a new ResourceManager with the specified
        GraphicsConfiguration.
    */
    public ResourceManager(GraphicsConfiguration gc) {
        this.gc = gc;
        loadTileImages();
        loadCreatureSprites();
        loadPowerUpSprites();
        numMaps = 5; //Change to add more maps to a world
        numWorlds = 3;
        currentMap = 0;
        currentWorld = 1;
    }


    /**
        Gets an image from the images/ directory.
    */
    public Image loadImage(String name) {
        String filename = "images/" + name;
        return new ImageIcon(filename).getImage();
    }


    public Image getMirrorImage(Image image) {
        return getScaledImage(image, -1, 1);
    }


    public Image getFlippedImage(Image image) {
        return getScaledImage(image, 1, -1);
    }


    private Image getScaledImage(Image image, float x, float y) {

        // set up the transform
        AffineTransform transform = new AffineTransform();
        transform.scale(x, y);
        transform.translate(
            (x-1) * image.getWidth(null) / 2,
            (y-1) * image.getHeight(null) / 2);

        // create a transparent (not translucent) image
        Image newImage = gc.createCompatibleImage(
            image.getWidth(null),
            image.getHeight(null),
            Transparency.BITMASK);

        // draw the transformed image
        Graphics2D g = (Graphics2D)newImage.getGraphics();
        g.drawImage(image, transform, null);
        g.dispose();

        return newImage;
    }


    public TileMap loadNextMap() {
        TileMap map = null;
        while (map == null) {
        	if (currentMap == numMaps) {
            	if (currentWorld == numWorlds) {
            		//Done with game, or move to final level
            		return null;
            	}
            	currentWorld++;
            	currentMap = 0;
        	}
        	else { 
        		currentMap++;
        	}
            try {
                map = loadMap(
                    "maps/map" + currentWorld + "-" + currentMap + ".txt");
            }
            catch (IOException e) {
                return null;
            }
        }

        return map;
    }


    public TileMap reloadMap() {
        try {
            return loadMap(
            	"maps/map" + currentWorld + "-" + currentMap + ".txt");
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private TileMap loadMap(String filename)
        throws IOException
    {
        ArrayList<String> lines = new ArrayList<String>();
        int width = 0;
        int height = 0;

        // read every line in the text file into the list
        BufferedReader reader = new BufferedReader(
            new FileReader(filename));
        while (true) {
            String line = reader.readLine();
            // no more lines to read
            if (line == null) {
                reader.close();
                break;
            }

            // add every line except for comments
            if (!line.startsWith("#")) {
                lines.add(line);
                width = Math.max(width, line.length());
            }
        }

        // parse the lines to create a TileEngine
        height = lines.size();
        TileMap newMap = new TileMap(width, height);
        for (int y=0; y<height; y++) {
            String line = lines.get(y);
            for (int x=0; x<line.length(); x++) {
                char ch = line.charAt(x);

                // check if the char represents tile A, B, C etc.
                int tile = ch - 'A';
                if (tile >= 0 && tile < tiles.size()) {
                    newMap.setTile(x, y, tiles.get(tile));
                }

                // check if the char represents a sprite
                else if (ch == 'o') {
                    addSprite(newMap, lightningSprite, x, y);
                }
                else if (ch == '*') {
                    addSprite(newMap, goalSprite, x, y);
                }
                else if (ch == '1') {
                    addSprite(newMap, sawSprite, x, y);
                }
                else if (ch == '2') {
                    addSprite(newMap, spikeSprite, x, y);
                }
                else if (ch == '@') {
                	Sprite player = (Sprite)playerSprite.clone();
                    player.setX(TileMapRenderer.tilesToPixels(x));
                    player.setY(TileMapRenderer.tilesToPixels(y));
                    newMap.setPlayer(player);
                }
            }
        }

        /* add the player to the map
        Sprite player = (Sprite)playerSprite.clone();
        player.setX(TileMapRenderer.tilesToPixels(3));
        player.setY(0);
        newMap.setPlayer(player);
        */

        return newMap;
    }


    private void addSprite(TileMap map,
        Sprite hostSprite, int tileX, int tileY)
    {
        if (hostSprite != null) {
            // clone the sprite from the "host"
            Sprite sprite = (Sprite)hostSprite.clone();

            // center the sprite
            sprite.setX(
                TileMapRenderer.tilesToPixels(tileX) +
                (TileMapRenderer.tilesToPixels(1) -
                sprite.getWidth()) / 2);

            // bottom-justify the sprite
            sprite.setY(
                TileMapRenderer.tilesToPixels(tileY + 1) -
                sprite.getHeight());

            // add it to the map
            map.addSprite(sprite);
        }
    }
    
    /**
     * Returns the current world
     * @return current world
     */
    
    public int getWorld() {
    	return currentWorld;
    }


    // -----------------------------------------------------------
    // code for loading sprites and images
    // -----------------------------------------------------------


    public void loadTileImages() {
        // keep looking for tile A,B,C, etc. this makes it
        // easy to drop new tiles in the images/ directory
        tiles = new ArrayList<Image>();
        char ch = 'A';
        while (true) {
            String name = "tile_" + ch + ".png";
            File file = new File("images/" + name);
            if (!file.exists()) {
                break;
            }
            tiles.add(loadImage(name));
            ch++;
        }
    }


    public void loadCreatureSprites() {

        Image[][] images = new Image[4][];

        // load left-facing images
        images[0] = new Image[] {
            loadImage("player1.png"),
            loadImage("player2.png"),
            loadImage("player3.png"),
            loadImage("lightning1.png"), //Add second lightning sprite
            loadImage("spike1.png"),
            loadImage("spike2.png"),
            loadImage("spikeBloody1.png"),
            loadImage("spikeBloody2.png"),
            loadImage("spikeInvisible.png"),
            loadImage("saw1.png"),
            loadImage("saw2.png"),
            loadImage("sawBloody1.png"),
            loadImage("sawBloody2.png"),
            loadImage("goal.png")
        };

        images[1] = new Image[images[0].length];
        images[2] = new Image[images[0].length];
        images[3] = new Image[images[0].length];
        for (int i=0; i<images[0].length; i++) {
            // right-facing images
            images[1][i] = getMirrorImage(images[0][i]);
            // left-facing "dead" images
            images[2][i] = getFlippedImage(images[0][i]);
            // right-facing "dead" images
            images[3][i] = getFlippedImage(images[1][i]);
        }
        // Player sprite/animations
        Animation[] playerAnim = new Animation[8]; //Array of 8 animations
        for (int i=0; i<4; i++) {
            playerAnim[i] = createPlayerAnim(images[i][0], images[i][0]);
        }
        //ducking sprites
        playerAnim[4] = createPlayerAnim(images[0][2], images[0][2]);
        playerAnim[5] = createPlayerAnim(images[1][2], images[1][2]);
        //walking sprites
        playerAnim[6] = createPlayerAnim(images[0][0], images[0][1]);
        playerAnim[7] = createPlayerAnim(images[1][0], images[1][1]);
        playerSprite = new Player(playerAnim[0], playerAnim[1], playerAnim[2], playerAnim[3], 
        		playerAnim[4], playerAnim[5], playerAnim[6], playerAnim[7]);
        
        //Lightning/door sprites
        Animation lightningAnim = createPowerUpAnim(images[0][3], images[0][3]); //Change this later to add animation
        lightningSprite = new PowerUp.Lightning(lightningAnim);
        Animation goalAnim = createPowerUpAnim(images[0][13], images[0][13]);
        goalSprite = new PowerUp.Goal(goalAnim);
        
        //Spike sprites
        Animation[] spikeAnim = new Animation[9];    
        //Up
        spikeAnim[0] = createSpikeAnim(images[0][4]);
        //Down
        spikeAnim[1] = createSpikeAnim(images[2][4]);
        //Left
        spikeAnim[2] = createSpikeAnim(images[0][5]);
        //Right
        spikeAnim[3] = createSpikeAnim(images[2][5]);
        //Bloody sprites
        spikeAnim[4] = createSpikeAnim(images[0][6]);
        spikeAnim[5] = createSpikeAnim(images[2][6]);
        spikeAnim[6] = createSpikeAnim(images[0][7]);
        spikeAnim[7] = createSpikeAnim(images[2][7]);
        //Invisible
        spikeAnim[7] = createSpikeAnim(images[0][8]);
        spikeSprite = new Spike(spikeAnim[0], spikeAnim[1],spikeAnim[2], spikeAnim[3]);
        spikeBloodySprite = new Spike(spikeAnim[4], spikeAnim[5],spikeAnim[6], spikeAnim[7]);
        spikeInvisibleSprite = new Spike(spikeAnim[8]);
        
        //Saw sprites
        Animation[] sawAnim = new Animation[2];
        //Normal
        sawAnim[0] = createSawAnim(images[0][9], images[0][10]);
        //Bloody
        sawAnim[1] = createSawAnim(images[0][11], images[0][12]);
        sawSprite = new Saw(sawAnim[0]);
        sawBloodySprite = new Saw(sawAnim[1]);
    }


    private Animation createPlayerAnim(Image player1, Image player2) {
        Animation anim = new Animation();
        anim.addFrame(player2, 150);
        anim.addFrame(player1, 150);
        return anim;
    }
    
    private Animation createPowerUpAnim(Image img1, Image img2) {
        Animation anim = new Animation();
        anim.addFrame(img1, 150);
        anim.addFrame(img2, 150);
        return anim;
    }


    private Animation createSawAnim(Image img1, Image img2) {
        Animation anim = new Animation();
        anim.addFrame(img1, 25);
        anim.addFrame(img2, 25);
        return anim;
    }
    
    private Animation createSpikeAnim(Image img) {
    	Animation anim = new Animation();
        anim.addFrame(img, 2000);
        return anim;
    }


    private void loadPowerUpSprites() {
        // create "goal" sprite
        Animation anim = new Animation();
        anim.addFrame(loadImage("goal.png"), 150);
        goalSprite = new PowerUp.Goal(anim);

        // create "lightning" sprite
        anim = new Animation();
        anim.addFrame(loadImage("lightning1.png"), 100);
        anim.addFrame(loadImage("lightning2.png"), 100);
        anim.addFrame(loadImage("lightning3.png"), 100);
        anim.addFrame(loadImage("lightning4.png"), 100);
        lightningSprite = new PowerUp.Lightning(anim);
    }
}
