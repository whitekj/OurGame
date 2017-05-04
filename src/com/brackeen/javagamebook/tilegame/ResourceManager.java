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
        currentMap = 1;
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
    
    public TileMap loadFirstMap() {
    	TileMap map = null;
        try {
            map = loadMap("maps/map" + currentWorld + "-" + currentMap + ".txt", true);
        }
        catch (IOException e) {
            return null;
        }
        return map;
    }
    
    public TileMap loadNextMap(boolean gotPowerUp) {
        TileMap map = null;
        while (map == null) {
        	if (currentMap == numMaps) {
            	if (currentWorld == numWorlds) {
            		//Done with game
            		System.exit(0);
            	}
            	else {
            		currentMap = 1;
            		if (gotPowerUp) {
            			currentWorld++;  	
            		}
            	}
        	}
        	else { 
        		currentMap++;
        	}
            try {
                map = loadMap(
                    "maps/map" + currentWorld + "-" + currentMap + ".txt", !gotPowerUp);
            }
            catch (IOException e) {
                return null;
            }
        }
        return map;
    }


    public TileMap reloadMap(boolean gotPowerUp) {
        try {
            return loadMap(
            	"maps/map" + currentWorld + "-" + currentMap + ".txt", !gotPowerUp);
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public TileMap loadBlankMap() {
    	try {
            return loadMap(
            	"maps/blank.txt", false);
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private TileMap loadMap(String filename, boolean showPowerUp)
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
                if (ch == 'I') {
                    newMap.setTile(x, y, tiles.get(currentWorld-1));
                }
                else if (ch == '!') {
                	if (showPowerUp) {
                		addSprite(newMap, lightningSprite, x, y);
                	}
                }
                else if (ch == '*') {
                    addSprite(newMap, goalSprite, x, y);
                }
                else if (ch == 'P') {
                    addSprite(newMap, pit, x, y);
                }
                else if (ch == '6') {
                    addSprite(newMap, sawPole, x, y);
                }
                else if (ch == '7') {
                    addSprite(newMap, sawPole2, x, y);
                }
                else if (ch == '@') {
                	Sprite player = (Sprite)playerSprite.clone();
                    player.setX(TileMapRenderer.tilesToPixels(x));
                    player.setY(TileMapRenderer.tilesToPixels(y));
                    newMap.setPlayer(player);
                }
                else if (currentWorld!=2) {
                	if (ch == '^') {
                        addSprite(newMap, spikeUp, x, y);
                    }
                    else if (ch == 'V') {
                        addSprite(newMap, spikeDown, x, y);
                    }
                    else if (ch == '<') {
                        addSprite(newMap, spikeLeft, x, y);
                    }
                    else if (ch == '>') {
                        addSprite(newMap, spikeRight, x, y);
                    }
                    else if (ch == '1') {
                        addSprite(newMap, sawFull, x, y);
                    }
                    else if (ch == '2') {
                        addSprite(newMap, sawHalfUp, x, y);
                    }
                    else if (ch == '3') {
                        addSprite(newMap, sawHalfDown, x, y);
                    }
                    else if (ch == '4') {
                        addSprite(newMap, sawHalfLeft, x, y);
                    }
                    else if (ch == '5') {
                        addSprite(newMap, sawHalfRight, x, y);
                    }
                }
                else {
                	if (ch == '^') {
                        addSprite(newMap, spikeUpBloody, x, y);
                    }
                    else if (ch == 'V') {
                        addSprite(newMap, spikeDownBloody, x, y);
                    }
                    else if (ch == '<') {
                        addSprite(newMap, spikeLeftBloody, x, y);
                    }
                    else if (ch == '>') {
                        addSprite(newMap, spikeRightBloody, x, y);
                    }
                    else if (ch == '1') {
                        addSprite(newMap, sawFullBloody, x, y);
                    }
                    else if (ch == '2') {
                        addSprite(newMap, sawHalfUpBloody, x, y);
                    }
                    else if (ch == '3') {
                        addSprite(newMap, sawHalfDownBloody, x, y);
                    }
                    else if (ch == '4') {
                        addSprite(newMap, sawHalfLeftBloody, x, y);
                    }
                    else if (ch == '5') {
                        addSprite(newMap, sawHalfRightBloody, x, y);
                    }
                }
            }
        }
        return newMap;
    }


    private void addSprite(TileMap map,
        Sprite hostSprite, int tileX, int tileY) {
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
     */
    
    public int getWorld() {
    	return currentWorld;
    }
    
    /**
     * Returns the current map
     */
    
    public int getMap(){
        return currentMap;
    }


    // -----------------------------------------------------------
    // code for loading sprites and images
    // -----------------------------------------------------------


    public void loadTileImages() {
        tiles = new ArrayList<Image>();
        int n = 1;
        while (true) {
            String name = "terrain" + n + ".png";
            File file = new File("images/" + name);
            if (!file.exists()) {
                break;
            }
            tiles.add(loadImage(name));
            n++;
        }
    }


    public void loadCreatureSprites() {

        Image[][] images = new Image[4][];

        // load left-facing images
        images[0] = new Image[] {
            loadImage("player1.png"),
            loadImage("player2.png"),
            loadImage("player3.png"),
            loadImage("spike1.png"),
            loadImage("spike2.png"), //Right-facing version
            loadImage("spikeBloody1.png"),
            loadImage("spikeBloody2.png"),
            loadImage("pit.png"), 
            loadImage("saw1.png"), //Full saw
            loadImage("saw2.png"),
            loadImage("saw3.png"),
            loadImage("saw4.png"),
            loadImage("saw5.png"),
            loadImage("saw6.png"),
            loadImage("saw7.png"),
            loadImage("saw8.png"),
            loadImage("bsaw1.png"), //Bloody full saw
            loadImage("bsaw2.png"),
            loadImage("bsaw3.png"),
            loadImage("bsaw4.png"),
            loadImage("bsaw5.png"),
            loadImage("bsaw6.png"),
            loadImage("bsaw7.png"),
            loadImage("bsaw8.png"),
            loadImage("sawHalf1.png"), //Half saw
            loadImage("sawHalf2.png"), 
            loadImage("sawHalfBloody1.png"),
            loadImage("sawHalfBloody2.png"),
            loadImage("sawPole.png"),
            loadImage("goal.png"),
            loadImage("lightning.png")
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
        
        int n=3; //Used to increment slot of images array
        //Spike sprites   
        spikeUp = new Spike(createStaticAnim(images[0][n]));
        spikeDown = new Spike(createStaticAnim(images[2][n++]));
        spikeLeft = new Spike(createStaticAnim(images[0][n]));
        spikeRight = new Spike(createStaticAnim(images[1][n++]));
        spikeUpBloody = new Spike(createStaticAnim(images[0][n]));
        spikeDownBloody = new Spike(createStaticAnim(images[2][n++]));
        spikeLeftBloody = new Spike(createStaticAnim(images[0][n]));
        spikeRightBloody = new Spike(createStaticAnim(images[1][n++]));
        Animation invisAnim = createStaticAnim(images[0][n++]);
        pit = new Spike(invisAnim);
        //Full Saw sprites
        sawFull = new Saw(createSawAnim(images[0][n++], images[0][n++], 
        		images[0][n++], images[0][n++], images[0][n++], images[0][n++], 
        		images[0][n++], images[0][n++]));
        sawFullBloody = new Saw(createSawAnim(images[0][n++], images[0][n++], 
        		images[0][n++], images[0][n++], images[0][n++], images[0][n++], 
        		images[0][n++], images[0][n++]));
        //Half Saw sprites
        sawHalfUp = new Saw(createStaticAnim(images[0][n]));
        sawHalfDown = new Saw(createStaticAnim(images[2][n++]));
        sawHalfLeft = new Saw(createStaticAnim(images[0][n]));
        sawHalfRight = new Saw(createStaticAnim(images[1][n++]));
        sawHalfUpBloody = new Saw(createStaticAnim(images[0][n]));
        sawHalfDownBloody = new Saw(createStaticAnim(images[2][n++]));
        sawHalfLeftBloody = new Saw(createStaticAnim(images[0][n]));
        sawHalfRightBloody = new Saw(createStaticAnim(images[1][n++]));
        //Saw pole
        sawPole = new Pole(createStaticAnim(images[0][n]));
        sawPole2 = new Pole(createStaticAnim(images[2][n++]));
        //Door sprites
        Animation goalAnim = createPowerUpAnim(images[0][n++]);
        goalSprite = new PowerUp.Goal(goalAnim);
        //Lightning sprites
        Animation lightningAnim = createPowerUpAnim(images[0][n]); 
        lightningSprite = new PowerUp.Lightning(lightningAnim);
        
    }


    private Animation createPlayerAnim(Image player1, Image player2) {
        Animation anim = new Animation();
        anim.addFrame(player2, 150);
        anim.addFrame(player1, 150);
        return anim;
    }
    
    private Animation createPowerUpAnim(Image img) {
        Animation anim = new Animation();
        anim.addFrame(img, 2000);
        return anim;
    }

    private Animation createSawAnim(Image img1, Image img2, Image img3, Image img4, 
    		Image img5, Image img6, Image img7, Image img8) {
        Animation anim = new Animation();
        int frames = 50;
        anim.addFrame(img1, frames);
        anim.addFrame(img2, frames);
        anim.addFrame(img3, frames);
        anim.addFrame(img4, frames);
        anim.addFrame(img5, frames);
        anim.addFrame(img6, frames);
        anim.addFrame(img7, frames);
        anim.addFrame(img8, frames);
        return anim;
    }
    
    private Animation createStaticAnim(Image img) {
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
        anim.addFrame(loadImage("lightning.png"), 1000);
        lightningSprite = new PowerUp.Lightning(anim);
    }
    private ArrayList<Image> tiles;
    private int currentMap;
    private int currentWorld;
    private int numMaps; //number of maps per world
    private int numWorlds;
    private GraphicsConfiguration gc;
    private Sprite playerSprite;
    private Sprite lightningSprite;
    private Sprite goalSprite;
    private Sprite spikeUp;
    private Sprite spikeDown;
    private Sprite spikeLeft;
    private Sprite spikeRight;
    private Sprite spikeUpBloody;
    private Sprite spikeDownBloody;
    private Sprite spikeLeftBloody;
    private Sprite spikeRightBloody;
    private Sprite pit;
    private Sprite sawFull;
    private Sprite sawFullBloody;
    private Sprite sawHalfUp;
    private Sprite sawHalfDown;
    private Sprite sawHalfLeft;
    private Sprite sawHalfRight;
    private Sprite sawHalfUpBloody;
    private Sprite sawHalfDownBloody;
    private Sprite sawHalfLeftBloody;
    private Sprite sawHalfRightBloody;
    private Sprite sawPole;
    private Sprite sawPole2;
}
