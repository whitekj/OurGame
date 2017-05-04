package com.brackeen.javagamebook.tilegame;

import java.awt.*;
import java.util.Iterator;

import javax.swing.ImageIcon;

import com.brackeen.javagamebook.graphics.Sprite;
import com.brackeen.javagamebook.tilegame.sprites.Pole;

/**
    The TileMapRenderer class draws a TileMap on the screen.
    It draws all tiles, sprites, and an optional background image
    centered around the position of the player.

    <p>If the width of background image is smaller the width of
    the tile map, the background image will appear to move
    slowly, creating a parallax background effect.

    <p>Also, three static methods are provided to convert pixels
    to tile positions, and vice-versa.

    <p>This TileMapRender uses a tile size of 64.
 */
public class TileMapRenderer {

	/**
        Converts a pixel position to a tile position.
	 */
	public static int pixelsToTiles(float pixels) {
		return pixelsToTiles(Math.round(pixels));
	}


	/**
        Converts a pixel position to a tile position.
	 */
	public static int pixelsToTiles(int pixels) {
		return pixels >> TILE_SIZE_BITS;
	}


	/**
        Converts a tile position to a pixel position.
	 */
	public static int tilesToPixels(int numTiles) {
		return numTiles << TILE_SIZE_BITS;
	}


	/**
        Sets the background to draw.
	 */
	public void setBackground(Image background) {
		this.background = background;
	}


	/**
        Draws the specified TileMap.
	 */
	public void draw(Graphics2D g, TileMap map,
			int screenWidth, int screenHeight, boolean drawPlayer)
	{
		Sprite player = map.getPlayer();
		int mapWidth = tilesToPixels(map.getWidth());

		//new
		int mapHeight = tilesToPixels(map.getHeight());
		//end new

		// get the scrolling position of the map
		// based on player's position
		int offsetX = screenWidth / 2 -
				Math.round(player.getX()) - TILE_SIZE;
		offsetX = Math.min(offsetX, 0);
		offsetX = Math.max(offsetX, screenWidth - mapWidth);

		//new 
		int offSetY = screenHeight / 2 - 
				Math.round(player.getY()) - TILE_SIZE;
		offSetY= Math.min(offSetY, 0);
		offSetY= Math.max(offSetY, screenHeight - mapHeight);
		//end new

		// get the y offset to draw all sprites and tiles
		int offsetY = screenHeight -
				tilesToPixels(map.getHeight());

		// draw black background, if needed
		if (background == null ||
				screenHeight > background.getHeight(null))
		{
			g.setColor(Color.black);
			g.fillRect(0, 0, screenWidth, screenHeight);
		}

		// draw parallax background image
		if (background != null) {
			int x = offsetX *
					(screenWidth - background.getWidth(null)) /
					(screenWidth - mapWidth);
			//int y = screenHeight - background.getHeight(null);

			//new
			int y = offSetY *
					(screenHeight - background.getHeight(null)) /
					(screenHeight - mapHeight);
			//endnew

			g.drawImage(background, x, y, null);
		}

		
		long currentTime = System.currentTimeMillis();
        if((currentTime-eTime1)<timesToRun[index]){
            drawGif(g, screenWidth, screenHeight);
        }else{
		 
		
			//All code for drawing sprites, tiles and player surrounded in else statement so not drawn over gif while gif being displayed
			// draw the visible tiles
			int firstTileX = pixelsToTiles(-offsetX);
			int lastTileX = firstTileX +
					pixelsToTiles(screenWidth) + 1;
			for (int y=0; y<map.getHeight(); y++) {
				for (int x=firstTileX; x <= lastTileX; x++) {
					Image image = map.getTile(x, y);
					if (image != null) {
						g.drawImage(image,
								tilesToPixels(x) + offsetX,
								tilesToPixels(y) + offSetY,
								null);
					}
				}
			}

			//draw poles first 
			Iterator z = map.getSprites();
			while (z.hasNext()) {
				Sprite s = (Sprite)z.next();
				if(s instanceof Pole){
					//System.out.println("I Found a pole");
					int x = Math.round(s.getX()) + offsetX;
					int y = Math.round(s.getY()) + offSetY;
					g.drawImage(s.getImage(), x, y, null);
				}
			}

			//draw other sprites
			Iterator i = map.getSprites();
			while (i.hasNext()) {
				Sprite sprite = (Sprite)i.next();
				if (!(sprite instanceof Pole)) {
					int x = Math.round(sprite.getX()) + offsetX;
					int y = Math.round(sprite.getY()) + offSetY;
					g.drawImage(sprite.getImage(), x, y, null);
				}
			}

			//draw player
			if (drawPlayer) {
			g.drawImage(player.getImage(),
					Math.round(player.getX()) + offsetX,
					Math.round(player.getY()) + offSetY,
					null);
			}
		}
	}

	/**
	 * Draws image
	 */

	public void drawImg(Graphics2D g, Image img, int screenWidth, int screenHeight) {
		g.drawImage(img, screenWidth, screenHeight, null);
		if (background == null || screenHeight > background.getHeight(null)) {
			g.setColor(Color.black);
			g.fillRect(0, 0, screenWidth, screenHeight);
		}
		if (background != null) {
			g.drawImage(background, screenWidth, screenHeight, null);
		}
	}

	

     public void setDrawIcon(String numScene){
        icon = new ImageIcon("images/cutscene" +numScene+ ".gif").getImage();
        eTime1=System.currentTimeMillis();
        index=Integer.parseInt(numScene);
    }
     
    private void drawGif(Graphics2D g, int screenWidth, int screenHeight){
        g.drawImage(icon, 0, 0, screenWidth, screenHeight, null);
    }

	 

	private static final int TILE_SIZE = 64;
	private static final int TILE_SIZE_BITS = 6;
	private Image background;
	private long eTime1;
    private int index;
    private int[] timesToRun = {11300, 8458, 7924, 11500};
    private Image icon;
}